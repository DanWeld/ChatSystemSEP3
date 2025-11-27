namespace CSharpWebAPI.ApiContracts;

public class MessageDto
{
    public int Id { get; set; }
    public int ChatRoomId { get; set; }
    public int SenderId { get; set; }
    public string Text { get; set; } = string.Empty;
    public DateTime SentAtUtc { get; set; }
    public bool IsEdited { get; set; }
    public bool IsDeleted { get; set; }
}
