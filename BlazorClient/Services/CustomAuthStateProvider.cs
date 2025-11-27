using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using BlazorClient.Shared;
using Microsoft.AspNetCore.Components.Authorization;

namespace BlazorClient.Services;

public class CustomAuthStateProvider : AuthenticationStateProvider
{
    private readonly TokenStorageService _tokenStorage;
    private static readonly ClaimsPrincipal Anonymous = new(new ClaimsIdentity());

    public CustomAuthStateProvider(TokenStorageService tokenStorage)
    {
        _tokenStorage = tokenStorage;
    }

    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        var token = await _tokenStorage.GetTokenAsync();
        if (string.IsNullOrWhiteSpace(token))
        {
            return new AuthenticationState(Anonymous);
        }

        var user = BuildClaimsPrincipal(token);
        return new AuthenticationState(user);
    }

    public async Task SignInAsync(AuthResponse response)
    {
        await _tokenStorage.StoreAsync(response);
        var authState = new AuthenticationState(BuildClaimsPrincipal(response.AccessToken));
        NotifyAuthenticationStateChanged(Task.FromResult(authState));
    }

    public async Task SignOutAsync()
    {
        await _tokenStorage.ClearAsync();
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(Anonymous)));
    }

    private static ClaimsPrincipal BuildClaimsPrincipal(string token)
    {
        var handler = new JwtSecurityTokenHandler();
        var jwt = handler.ReadJwtToken(token);
        var identity = new ClaimsIdentity(jwt.Claims, "jwt");
        return new ClaimsPrincipal(identity);
    }
}

