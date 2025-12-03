using System.Security.Claims;
using CSharpWebAPI.ApiContracts;
using CSharpWebAPI.Controllers;
using CSharpWebAPI.Services;
using FluentAssertions;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Moq;
using Xunit;

namespace CSharpWebAPI.Tests;

public class GroupChatControllerTests
{
    private readonly Mock<IGroupChatService> _mockService;
    private readonly GroupChatController _controller;

    public GroupChatControllerTests()
    {
        _mockService = new Mock<IGroupChatService>();
        _controller = new GroupChatController(_mockService.Object);

        // Setup default user claims
        var claims = new List<Claim>
        {
            new Claim(ClaimTypes.NameIdentifier, "1")
        };
        var identity = new ClaimsIdentity(claims, "TestAuth");
        var principal = new ClaimsPrincipal(identity);
        _controller.ControllerContext = new ControllerContext
        {
            HttpContext = new DefaultHttpContext
            {
                User = principal
            }
        };
    }

    [Fact]
    public async Task CreateGroupChat_ValidRequest_ReturnsOk()
    {
        // Arrange
        var request = new CreateGroupChatRequest
        {
            Name = "Test Group",
            Description = "Test Description",
            MemberIds = new List<int> { 2, 3 }
        };

        var expectedRoom = new ChatRoomDto
        {
            Id = 100,
            Name = "Test Group"
        };

        _mockService
            .Setup(s => s.CreateGroupChatAsync(1, request.Name, request.Description, request.MemberIds))
            .ReturnsAsync(expectedRoom);

        // Act
        var result = await _controller.CreateGroupChat(request);

        // Assert
        var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
        var room = okResult.Value.Should().BeOfType<ChatRoomDto>().Subject;
        room.Id.Should().Be(100);
        room.Name.Should().Be("Test Group");

        _mockService.Verify(s => s.CreateGroupChatAsync(1, request.Name, request.Description, request.MemberIds), Times.Once);
    }

    [Fact]
    public async Task CreateGroupChat_InvalidModel_ReturnsValidationProblem()
    {
        // Arrange
        var request = new CreateGroupChatRequest
        {
            Name = "", // Invalid: empty name
            Description = "Test Description",
            MemberIds = new List<int>()
        };

        _controller.ModelState.AddModelError("Name", "Name is required");

        // Act
        var result = await _controller.CreateGroupChat(request);

        // Assert
        result.Result.Should().BeOfType<ObjectResult>();
        _mockService.Verify(s => s.CreateGroupChatAsync(It.IsAny<int>(), It.IsAny<string>(), It.IsAny<string>(), It.IsAny<List<int>>()), Times.Never);
    }

    [Fact]
    public async Task AddMember_ValidRequest_ReturnsOk()
    {
        // Arrange
        var chatRoomId = 100;
        var request = new AddMemberRequest { UserId = 2 };

        _mockService
            .Setup(s => s.AddMemberAsync(chatRoomId, 1, request.UserId))
            .Returns(Task.CompletedTask);

        // Act
        var result = await _controller.AddMember(chatRoomId, request);

        // Assert
        result.Should().BeOfType<OkResult>();
        _mockService.Verify(s => s.AddMemberAsync(chatRoomId, 1, request.UserId), Times.Once);
    }

    [Fact]
    public async Task RemoveMember_ValidRequest_ReturnsNoContent()
    {
        // Arrange
        var chatRoomId = 100;
        var userId = 2;

        _mockService
            .Setup(s => s.RemoveMemberAsync(chatRoomId, 1, userId))
            .Returns(Task.CompletedTask);

        // Act
        var result = await _controller.RemoveMember(chatRoomId, userId);

        // Assert
        result.Should().BeOfType<NoContentResult>();
        _mockService.Verify(s => s.RemoveMemberAsync(chatRoomId, 1, userId), Times.Once);
    }

    [Fact]
    public async Task PromoteMember_ValidRequest_ReturnsOk()
    {
        // Arrange
        var chatRoomId = 100;
        var userId = 2;

        _mockService
            .Setup(s => s.PromoteMemberAsync(chatRoomId, 1, userId))
            .Returns(Task.CompletedTask);

        // Act
        var result = await _controller.PromoteMember(chatRoomId, userId);

        // Assert
        result.Should().BeOfType<OkResult>();
        _mockService.Verify(s => s.PromoteMemberAsync(chatRoomId, 1, userId), Times.Once);
    }

    [Fact]
    public async Task ListMembers_ValidRequest_ReturnsOk()
    {
        // Arrange
        var chatRoomId = 100;
        var expectedMembers = new List<ChatRoomMemberDto>
        {
            new ChatRoomMemberDto { UserId = 1, Username = "Dani", Role = "OWNER" },
            new ChatRoomMemberDto { UserId = 2, Username = "Jwan", Role = "MEMBER" }
        };

        _mockService
            .Setup(s => s.ListMembersAsync(chatRoomId))
            .ReturnsAsync(expectedMembers);

        // Act
        var result = await _controller.ListMembers(chatRoomId);

        // Assert
        var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
        var members = okResult.Value.Should().BeAssignableTo<List<ChatRoomMemberDto>>().Subject;
        members.Should().HaveCount(2);
        members[0].Username.Should().Be("Dani");
        members[1].Username.Should().Be("Jwan");

        _mockService.Verify(s => s.ListMembersAsync(chatRoomId), Times.Once);
    }

    [Fact]
    public async Task ListMyChatRooms_ValidRequest_ReturnsOk()
    {
        // Arrange
        var expectedRooms = new List<ChatRoomDto>
        {
            new ChatRoomDto { Id = 100, Name = "Group 1" },
            new ChatRoomDto { Id = 101, Name = "Group 2" }
        };

        _mockService
            .Setup(s => s.ListUserChatRoomsAsync(1))
            .ReturnsAsync(expectedRooms);

        // Act
        var result = await _controller.ListMyChatRooms();

        // Assert
        var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
        var rooms = okResult.Value.Should().BeAssignableTo<List<ChatRoomDto>>().Subject;
        rooms.Should().HaveCount(2);

        _mockService.Verify(s => s.ListUserChatRoomsAsync(1), Times.Once);
    }

    [Fact]
    public async Task GetPrivateChatRoom_ValidRequest_ReturnsOk()
    {
        // Arrange
        var userId2 = 2;
        var expectedRoom = new ChatRoomDto
        {
            Id = 200,
            Name = "Dani & Jwan"
        };

        _mockService
            .Setup(s => s.GetPrivateChatRoomAsync(1, userId2))
            .ReturnsAsync(expectedRoom);

        // Act
        var result = await _controller.GetPrivateChatRoom(userId2);

        // Assert
        var okResult = result.Result.Should().BeOfType<OkObjectResult>().Subject;
        var room = okResult.Value.Should().BeOfType<ChatRoomDto>().Subject;
        room.Id.Should().Be(200);

        _mockService.Verify(s => s.GetPrivateChatRoomAsync(1, userId2), Times.Once);
    }

    [Fact]
    public async Task GetCurrentUserId_ValidClaim_ReturnsUserId()
    {
        // Arrange
        var request = new CreateGroupChatRequest
        {
            Name = "Test Group",
            Description = "Test Description",
            MemberIds = new List<int>()
        };

        var expectedRoom = new ChatRoomDto
        {
            Id = 100,
            Name = "Test Group"
        };

        _mockService
            .Setup(s => s.CreateGroupChatAsync(1, request.Name, request.Description, request.MemberIds))
            .ReturnsAsync(expectedRoom);

        // Act
        var result = await _controller.CreateGroupChat(request);

        // Assert
        _mockService.Verify(s => s.CreateGroupChatAsync(1, It.IsAny<string>(), It.IsAny<string>(), It.IsAny<List<int>>()), Times.Once);
    }
}

