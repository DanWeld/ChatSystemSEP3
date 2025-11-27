using CSharpWebAPI.ApiContracts;

namespace CSharpWebAPI.Services;

public interface IGroupChatService
{
    Task<ChatRoomDto> CreateGroupChatAsync(int ownerId, string name, string? description, List<int> memberIds);
    Task AddMemberAsync(int chatRoomId, int requesterId, int userId);
    Task RemoveMemberAsync(int chatRoomId, int requesterId, int userId);
    Task PromoteMemberAsync(int chatRoomId, int requesterId, int userId);
    Task<List<ApiContracts.ChatRoomMemberDto>> ListMembersAsync(int chatRoomId);
    Task<List<ChatRoomDto>> ListUserChatRoomsAsync(int userId);
    Task<ChatRoomDto> GetPrivateChatRoomAsync(int userId1, int userId2);
}

