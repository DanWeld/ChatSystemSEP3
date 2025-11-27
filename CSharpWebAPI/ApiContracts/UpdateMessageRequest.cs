using System.ComponentModel.DataAnnotations;

namespace CSharpWebAPI.ApiContracts;

public class UpdateMessageRequest
{
    [Required, MinLength(1), MaxLength(2000)]
    public string Text { get; set; } = string.Empty;
}

