using Chat.Grpc;
using CSharpWebAPI.ApiContracts;
using Google.Protobuf.WellKnownTypes;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using System.Security.Claims;
using CSharpWebAPI.Hubs;
using ApiCreateChatRoomRequest = CSharpWebAPI.ApiContracts.CreateChatRoomRequest;

namespace CSharpWebAPI.Controllers;

[ApiController]
[Authorize]
[Route("api/chatrooms")]
public class ChatController : ControllerBase
{
    private readonly ChatService.ChatServiceClient _grpc;
    private readonly IHubContext<ChatHub> _hub;

    public ChatController(ChatService.ChatServiceClient grpc, IHubContext<ChatHub> hub)
    {
        _grpc = grpc;
        _hub = hub;
    }

    [HttpGet]
    public async Task<IEnumerable<ChatRoomDto>> GetRooms()
    {
        var resp = await _grpc.ListChatRoomsAsync(new Empty());
        return resp.Rooms.Select(MapRoom);
    }

    [HttpPost]
    public async Task<ActionResult<ChatRoomDto>> CreateRoom([FromBody] ApiCreateChatRoomRequest request)
    {
        if (!ModelState.IsValid)
            return ValidationProblem(ModelState);

        try
        {
            var resp = await _grpc.CreateChatRoomAsync(new Chat.Grpc.CreateChatRoomRequest
            {
                Name = request.Name
            });
            return Ok(MapRoom(resp.Room));
        }
        catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.AlreadyExists)
        {
            return Conflict(new { message = "Chat room already exists." });
        }
    }

    [HttpGet("{chatRoomId:int}/messages")]
    public async Task<IEnumerable<MessageDto>> GetMessages(int chatRoomId)
    {
        var resp = await _grpc.GetMessagesAsync(new GetMessagesRequest { ChatRoomId = chatRoomId });

        return resp.Messages.Select(MapMessage).ToList();
    }

    [HttpPost("{chatRoomId:int}/messages")]
    public async Task<ActionResult<MessageDto>> SendMessage(int chatRoomId, [FromBody] SendMessageRequestDto request)
    {
        if (!ModelState.IsValid)
        {
            return ValidationProblem(ModelState);
        }

        var userId = GetUserId();

        try
        {
            var reply = await _grpc.SendMessageAsync(new Chat.Grpc.SendMessageRequest
            {
                ChatRoomId = chatRoomId,
                SenderId = userId,
                Text = request.Text
            });

            var dto = MapMessage(reply.Message);
            
            // Broadcast via SignalR if available
            await _hub.Clients.Group(chatRoomId.ToString()).SendAsync("ReceiveMessage", dto);
            
            return Ok(dto);
        }
        catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
        {
            return NotFound(new { message = "Chat room not found." });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = $"Failed to send message: {ex.Message}" });
        }
    }

    [HttpGet("{chatRoomId:int}/messages/search")]
    public async Task<IEnumerable<MessageDto>> SearchMessages(int chatRoomId, [FromQuery] string query)
    {
        if (string.IsNullOrWhiteSpace(query))
        {
            return Array.Empty<MessageDto>();
        }

        var resp = await _grpc.SearchMessagesAsync(new SearchMessagesRequest
        {
            ChatRoomId = chatRoomId,
            Query = query
        });

        return resp.Messages.Select(MapMessage).ToList();
    }

    [HttpPut("{chatRoomId:int}/messages/{messageId:int}")]
    public async Task<ActionResult<MessageDto>> EditMessage(int chatRoomId, int messageId,
        [FromBody] UpdateMessageRequest request)
    {
        if (!ModelState.IsValid)
        {
            return ValidationProblem(ModelState);
        }

        var userId = GetUserId();

        try
        {
            var message = await _grpc.EditMessageAsync(new EditMessageRequest
            {
                MessageId = messageId,
                SenderId = userId,
                Text = request.Text
            });

            var dto = MapMessage(message);
            await _hub.Clients.Group(chatRoomId.ToString()).SendAsync("MessageEdited", dto);
            return Ok(dto);
        }
        catch (UnauthorizedAccessException)
        {
            return Unauthorized();
        }
        catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.PermissionDenied)
        {
            return Forbid();
        }
        catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
        {
            return NotFound();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = ex.Message });
        }
    }

    [HttpDelete("{chatRoomId:int}/messages/{messageId:int}")]
    public async Task<IActionResult> DeleteMessage(int chatRoomId, int messageId)
    {
        var userId = GetUserId();

        try
        {
            var message = await _grpc.DeleteMessageAsync(new DeleteMessageRequest
            {
                MessageId = messageId,
                RequesterId = userId
            });

            var dto = MapMessage(message);
            await _hub.Clients.Group(chatRoomId.ToString()).SendAsync("MessageDeleted", dto);
            return Ok(dto);
        }
        catch (UnauthorizedAccessException)
        {
            return Unauthorized();
        }
        catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.PermissionDenied)
        {
            return Forbid();
        }
        catch (Grpc.Core.RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
        {
            return NotFound();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = ex.Message });
        }
    }

    private int GetUserId()
    {
        var userIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrWhiteSpace(userIdClaim) || !int.TryParse(userIdClaim, out var userId))
        {
            throw new UnauthorizedAccessException("User id missing.");
        }
        return userId;
    }

    private static ChatRoomDto MapRoom(ChatRoom room) => new()
    {
        Id = room.Id,
        Name = room.Name
    };

    private static MessageDto MapMessage(Message m) => new()
    {
        Id = m.Id,
        ChatRoomId = m.ChatRoomId,
        SenderId = m.SenderId,
        Text = m.Text,
        SentAtUtc = DateTimeOffset.FromUnixTimeSeconds(m.SentAtUnix).UtcDateTime,
        IsEdited = m.IsEdited,
        IsDeleted = m.IsDeleted
    };
}
