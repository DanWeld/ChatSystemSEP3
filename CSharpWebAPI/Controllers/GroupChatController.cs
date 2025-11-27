using System.Security.Claims;
using CSharpWebAPI.ApiContracts;
using CSharpWebAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CSharpWebAPI.Controllers;

[ApiController]
[Authorize]
[Route("api/[controller]")]
public class GroupChatController : ControllerBase
{
    private readonly IGroupChatService _groupChatService;

    public GroupChatController(IGroupChatService groupChatService)
    {
        _groupChatService = groupChatService;
    }

    private int GetCurrentUserId()
    {
        var userIdClaim = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrEmpty(userIdClaim) || !int.TryParse(userIdClaim, out int userId))
        {
            throw new UnauthorizedAccessException("User ID not found in token.");
        }
        return userId;
    }

    [HttpPost]
    public async Task<ActionResult<ChatRoomDto>> CreateGroupChat([FromBody] CreateGroupChatRequest request)
    {
        if (!ModelState.IsValid)
        {
            return ValidationProblem(ModelState);
        }

        try
        {
            var ownerId = GetCurrentUserId();
            var room = await _groupChatService.CreateGroupChatAsync(ownerId, request.Name, request.Description, request.MemberIds);
            return Ok(room);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message });
        }
    }

    [HttpPost("{chatRoomId}/members")]
    public async Task<IActionResult> AddMember(int chatRoomId, [FromBody] AddMemberRequest request)
    {
        try
        {
            var requesterId = GetCurrentUserId();
            await _groupChatService.AddMemberAsync(chatRoomId, requesterId, request.UserId);
            return Ok();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message });
        }
    }

    [HttpDelete("{chatRoomId}/members/{userId}")]
    public async Task<IActionResult> RemoveMember(int chatRoomId, int userId)
    {
        try
        {
            var requesterId = GetCurrentUserId();
            await _groupChatService.RemoveMemberAsync(chatRoomId, requesterId, userId);
            return NoContent();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message });
        }
    }

    [HttpPost("{chatRoomId}/members/{userId}/promote")]
    public async Task<IActionResult> PromoteMember(int chatRoomId, int userId)
    {
        try
        {
            var requesterId = GetCurrentUserId();
            await _groupChatService.PromoteMemberAsync(chatRoomId, requesterId, userId);
            return Ok();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message });
        }
    }

    [HttpGet("{chatRoomId}/members")]
    public async Task<ActionResult<List<ChatRoomMemberDto>>> ListMembers(int chatRoomId)
    {
        try
        {
            var members = await _groupChatService.ListMembersAsync(chatRoomId);
            return Ok(members);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message });
        }
    }

    [HttpGet("my-chats")]
    public async Task<ActionResult<List<ChatRoomDto>>> ListMyChatRooms()
    {
        try
        {
            var userId = GetCurrentUserId();
            var rooms = await _groupChatService.ListUserChatRoomsAsync(userId);
            return Ok(rooms);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message });
        }
    }

    [HttpGet("private/{userId2}")]
    public async Task<ActionResult<ChatRoomDto>> GetPrivateChatRoom(int userId2)
    {
        try
        {
            var userId1 = GetCurrentUserId();
            var room = await _groupChatService.GetPrivateChatRoomAsync(userId1, userId2);
            return Ok(room);
        }
        catch (InvalidOperationException ex)
        {
            return StatusCode(500, new { message = ex.Message, innerException = ex.InnerException?.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An error occurred: " + ex.Message, stackTrace = ex.StackTrace });
        }
    }
}

public class AddMemberRequest
{
    public int UserId { get; set; }
}

