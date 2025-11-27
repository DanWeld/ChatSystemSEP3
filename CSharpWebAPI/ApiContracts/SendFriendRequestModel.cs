using System.ComponentModel.DataAnnotations;

namespace CSharpWebAPI.ApiContracts;

public class SendFriendRequestModel
{
    [Required]
    public string Username { get; set; } = string.Empty;
}

