namespace CSharpWebAPI.ApiContracts;

public class ChatRoomMemberDto
{
    public int UserId { get; set; }
    public string Username { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty; // OWNER, ADMIN, MEMBER
}

