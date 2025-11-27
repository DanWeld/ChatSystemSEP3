using System.Text.Json;
using BlazorClient.Shared;
using Microsoft.JSInterop;

namespace BlazorClient.Services;

public class TokenStorageService
{
    private const string TokenKey = "chatapp.jwt";
    private const string UserKey = "chatapp.user";
    private readonly IJSRuntime _jsRuntime;

    public TokenStorageService(IJSRuntime jsRuntime)
    {
        _jsRuntime = jsRuntime;
    }

    public async Task StoreAsync(AuthResponse response)
    {
        await _jsRuntime.InvokeVoidAsync("localStorage.setItem", TokenKey, response.AccessToken);
        var userJson = JsonSerializer.Serialize(response.User);
        await _jsRuntime.InvokeVoidAsync("localStorage.setItem", UserKey, userJson);
    }

    public async Task<string?> GetTokenAsync()
    {
        return await _jsRuntime.InvokeAsync<string?>("localStorage.getItem", TokenKey);
    }

    public async Task<UserDto?> GetUserAsync()
    {
        var json = await _jsRuntime.InvokeAsync<string?>("localStorage.getItem", UserKey);
        if (string.IsNullOrEmpty(json))
        {
            return null;
        }

        return JsonSerializer.Deserialize<UserDto>(json);
    }

    public async Task ClearAsync()
    {
        await _jsRuntime.InvokeVoidAsync("localStorage.removeItem", TokenKey);
        await _jsRuntime.InvokeVoidAsync("localStorage.removeItem", UserKey);
    }
}

