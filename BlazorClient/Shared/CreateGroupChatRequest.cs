namespace BlazorClient.Shared;

public class CreateGroupChatRequest
{
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public List<int> MemberIds { get; set; } = new();
}

