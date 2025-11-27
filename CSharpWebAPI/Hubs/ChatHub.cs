using System.Security.Claims;
using CSharpWebAPI.ApiContracts;
using Chat.Grpc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;

namespace CSharpWebAPI.Hubs;

[Authorize]
public class ChatHub : Hub
{
    private readonly ChatService.ChatServiceClient _grpc;

    public ChatHub(ChatService.ChatServiceClient grpc)
    {
        _grpc = grpc;
    }

    public async Task JoinChat(int chatRoomId)
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, chatRoomId.ToString());
    }

    public async Task LeaveChat(int chatRoomId)
    {
        await Groups.RemoveFromGroupAsync(Context.ConnectionId, chatRoomId.ToString());
    }

    public async Task SendMessage(int chatRoomId, string text)
    {
        var senderId = Context.User?.FindFirstValue(ClaimTypes.NameIdentifier);
        if (!int.TryParse(senderId, out var sender))
        {
            throw new HubException("Unauthorized");
        }

        var reply = await _grpc.SendMessageAsync(new SendMessageRequest
        {
            ChatRoomId = chatRoomId,
            SenderId = sender,
            Text = text
        });

        var msg = reply.Message;

        var dto = new MessageDto
        {
            Id = msg.Id,
            ChatRoomId = msg.ChatRoomId,
            SenderId = msg.SenderId,
            Text = msg.Text,
            SentAtUtc = DateTimeOffset.FromUnixTimeSeconds(msg.SentAtUnix).UtcDateTime,
            IsEdited = msg.IsEdited,
            IsDeleted = msg.IsDeleted
        };

        await Clients.Group(chatRoomId.ToString()).SendAsync("ReceiveMessage", dto);
    }
}
