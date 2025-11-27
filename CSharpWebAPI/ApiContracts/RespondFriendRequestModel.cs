using System.ComponentModel.DataAnnotations;

namespace CSharpWebAPI.ApiContracts;

public class RespondFriendRequestModel
{
    [Required]
    public bool Accept { get; set; }
}

