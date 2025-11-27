using System.ComponentModel.DataAnnotations;

namespace BlazorClient.Shared;

public class UpdateMessageRequest
{
    [Required, MinLength(1), MaxLength(2000)]
    public string Text { get; set; } = string.Empty;
}

