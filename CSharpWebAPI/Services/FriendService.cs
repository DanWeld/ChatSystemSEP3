using Chat.Grpc;
using CSharpWebAPI.ApiContracts;
using Grpc.Core;
using ApiFriendRequestDto = CSharpWebAPI.ApiContracts.FriendRequestDto;
using ApiFriendDto = CSharpWebAPI.ApiContracts.FriendDto;
using GrpcFriendRequestDto = Chat.Grpc.FriendRequestDto;

namespace CSharpWebAPI.Services;

public class FriendshipService : IFriendService
{
    private readonly FriendService.FriendServiceClient _client;

    public FriendshipService(FriendService.FriendServiceClient client)
    {
        _client = client;
    }

    public async Task<ApiFriendRequestDto> SendRequestAsync(int requesterId, string targetUsername)
    {
        var response = await _client.SendFriendRequestAsync(new SendFriendRequestRequest
        {
            RequesterId = requesterId,
            TargetUsername = targetUsername
        });

        return Map(response);
    }

    public async Task<ApiFriendRequestDto> RespondAsync(int requestId, bool accept)
    {
        var response = await _client.RespondFriendRequestAsync(new RespondFriendRequestRequest
        {
            RequestId = requestId,
            Accept = accept
        });

        return Map(response);
    }

    public async Task<IReadOnlyList<ApiFriendRequestDto>> GetIncomingAsync(int userId)
    {
        var response = await _client.ListIncomingRequestsAsync(new ListFriendRequestsRequest { UserId = userId });
        return response.Requests.Select(Map).ToList();
    }

    public async Task<IReadOnlyList<ApiFriendDto>> GetFriendsAsync(int userId)
    {
        var response = await _client.ListFriendsAsync(new ListFriendsRequest { UserId = userId });
        return response.Friends.Select(f => new ApiFriendDto
        {
            UserId = f.UserId,
            Username = f.Username
        }).ToList();
    }

    public async Task RemoveFriendAsync(int userId, int friendId)
    {
        await _client.RemoveFriendAsync(new RemoveFriendRequest
        {
            UserId = userId,
            FriendId = friendId
        });
    }

    private static ApiFriendRequestDto Map(GrpcFriendRequestDto request)
    {
        return new ApiFriendRequestDto
        {
            Id = request.Id,
            SenderId = request.SenderId,
            SenderUsername = request.SenderUsername,
            ReceiverId = request.ReceiverId,
            Status = request.Status,
            CreatedAtUtc = DateTimeOffset.FromUnixTimeSeconds(request.CreatedAtUnix).UtcDateTime
        };
    }
}

