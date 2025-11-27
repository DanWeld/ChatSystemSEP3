namespace CSharpWebAPI.ApiContracts;

public class AuthResponse
{
    public string AccessToken { get; set; } = default!;
    public UserDto User { get; set; } = default!;
}

