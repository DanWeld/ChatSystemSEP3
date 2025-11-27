using System.Net.Http.Json;
using BlazorClient.Shared;

namespace BlazorClient.Services;

public class FriendApiService
{
    private readonly HttpClient _httpClient;

    public FriendApiService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public Task<List<FriendDto>?> GetFriendsAsync()
        => _httpClient.GetFromJsonAsync<List<FriendDto>>("api/friends");

    public Task<List<FriendRequestDto>?> GetIncomingAsync()
        => _httpClient.GetFromJsonAsync<List<FriendRequestDto>>("api/friends/requests");

    public async Task<bool> SendRequestAsync(string username)
    {
        var response = await _httpClient.PostAsJsonAsync("api/friends/requests", new SendFriendRequestModel { Username = username });
        return response.IsSuccessStatusCode;
    }

    public async Task<bool> RespondAsync(int requestId, bool accept)
    {
        var response = await _httpClient.PostAsJsonAsync($"api/friends/requests/{requestId}", new RespondFriendRequestModel { Accept = accept });
        return response.IsSuccessStatusCode;
    }

    public async Task<bool> RemoveFriendAsync(int friendId)
    {
        var response = await _httpClient.DeleteAsync($"api/friends/{friendId}");
        return response.IsSuccessStatusCode;
    }
}

