using CSharpWebAPI.ApiContracts;

namespace CSharpWebAPI.Services;

public interface IFriendService
{
    Task<FriendRequestDto> SendRequestAsync(int requesterId, string targetUsername);
    Task<FriendRequestDto> RespondAsync(int requestId, bool accept);
    Task<IReadOnlyList<FriendRequestDto>> GetIncomingAsync(int userId);
    Task<IReadOnlyList<FriendDto>> GetFriendsAsync(int userId);
    Task RemoveFriendAsync(int userId, int friendId);
}

