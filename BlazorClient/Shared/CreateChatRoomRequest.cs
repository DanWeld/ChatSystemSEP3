using System.ComponentModel.DataAnnotations;

namespace BlazorClient.Shared;
public class CreateChatRoomRequest
{
    [Required, MinLength(3), MaxLength(100)]
    public string Name { get; set; } = string.Empty;
}


