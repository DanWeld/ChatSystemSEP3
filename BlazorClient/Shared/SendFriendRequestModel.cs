using System.ComponentModel.DataAnnotations;

namespace BlazorClient.Shared;

public class SendFriendRequestModel
{
    [Required(ErrorMessage = "Username is required.")]
    [MinLength(1, ErrorMessage = "Username cannot be empty.")]
    public string Username { get; set; } = string.Empty;
}

