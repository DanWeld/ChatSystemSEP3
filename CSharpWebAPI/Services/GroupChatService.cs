using Chat.Grpc;
using CSharpWebAPI.ApiContracts;
using Grpc.Core;
using GrpcGroupChatService = Chat.Grpc.GroupChatService;
using GrpcChatRoomMemberDto = Chat.Grpc.ChatRoomMemberDto;
using ApiChatRoomMemberDto = CSharpWebAPI.ApiContracts.ChatRoomMemberDto;
using GrpcCreateGroupChatRequest = Chat.Grpc.CreateGroupChatRequest;
using ApiCreateGroupChatRequest = CSharpWebAPI.ApiContracts.CreateGroupChatRequest;

namespace CSharpWebAPI.Services;

public class GroupChatService : IGroupChatService
{
    private readonly GrpcGroupChatService.GroupChatServiceClient _client;

    public GroupChatService(GrpcGroupChatService.GroupChatServiceClient client)
    {
        _client = client;
    }

    public async Task<ChatRoomDto> CreateGroupChatAsync(int ownerId, string name, string? description, List<int> memberIds)
    {
        var request = new GrpcCreateGroupChatRequest
        {
            OwnerId = ownerId,
            Name = name,
            Description = description ?? string.Empty,
        };
        request.MemberIds.AddRange(memberIds);
        var response = await _client.CreateGroupChatAsync(request);

        return new ChatRoomDto
        {
            Id = response.Room.Id,
            Name = response.Room.Name
        };
    }

    public async Task AddMemberAsync(int chatRoomId, int requesterId, int userId)
    {
        await _client.AddMemberAsync(new AddMemberRequest
        {
            ChatRoomId = chatRoomId,
            RequesterId = requesterId,
            UserId = userId
        });
    }

    public async Task RemoveMemberAsync(int chatRoomId, int requesterId, int userId)
    {
        await _client.RemoveMemberAsync(new RemoveMemberRequest
        {
            ChatRoomId = chatRoomId,
            RequesterId = requesterId,
            UserId = userId
        });
    }

    public async Task PromoteMemberAsync(int chatRoomId, int requesterId, int userId)
    {
        await _client.PromoteMemberAsync(new PromoteMemberRequest
        {
            ChatRoomId = chatRoomId,
            RequesterId = requesterId,
            UserId = userId
        });
    }

    public async Task<List<ApiChatRoomMemberDto>> ListMembersAsync(int chatRoomId)
    {
        var response = await _client.ListMembersAsync(new ListMembersRequest { ChatRoomId = chatRoomId });
        return response.Members.Select(m => new ApiChatRoomMemberDto
        {
            UserId = m.UserId,
            Username = m.Username,
            Role = m.Role
        }).ToList<ApiChatRoomMemberDto>();
    }

    public async Task<List<ChatRoomDto>> ListUserChatRoomsAsync(int userId)
    {
        var response = await _client.ListUserChatRoomsAsync(new ListUserChatRoomsRequest { UserId = userId });
        return response.Rooms.Select(r => new ChatRoomDto
        {
            Id = r.Id,
            Name = r.Name
        }).ToList();
    }

    public async Task<ChatRoomDto> GetPrivateChatRoomAsync(int userId1, int userId2)
    {
        try
        {
            var response = await _client.GetPrivateChatRoomAsync(new GetPrivateChatRoomRequest
            {
                UserId1 = userId1,
                UserId2 = userId2
            });

            return new ChatRoomDto
            {
                Id = response.Room.Id,
                Name = response.Room.Name
            };
        }
        catch (Grpc.Core.RpcException ex)
        {
            throw new InvalidOperationException($"Failed to get private chat room: {ex.Status.Detail}", ex);
        }
    }
}

