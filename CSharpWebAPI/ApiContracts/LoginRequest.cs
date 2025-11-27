using System.ComponentModel.DataAnnotations;

namespace CSharpWebAPI.ApiContracts;

public class LoginRequest
{
    [Required]
    public string Username { get; set; } = default!;

    [Required]
    public string Password { get; set; } = default!;
}

