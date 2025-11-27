using System.ComponentModel.DataAnnotations;

namespace BlazorClient.Shared;

public class RespondFriendRequestModel
{
    [Required]
    public bool Accept { get; set; }
}

