using System.ComponentModel.DataAnnotations;

namespace CSharpWebAPI.ApiContracts;

public class CreateGroupChatRequest
{
    [Required]
    [StringLength(100)]
    public string Name { get; set; } = string.Empty;

    [StringLength(255)]
    public string? Description { get; set; }

    public List<int> MemberIds { get; set; } = new();
}

