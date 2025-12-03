using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Chat.Grpc;
using CSharpWebAPI.ApiContracts;
using CSharpWebAPI.Services;
using FluentAssertions;
using Grpc.Core;
using GrpcGroupChatService = Chat.Grpc.GroupChatService;
using Moq;
using Xunit;
using Google.Protobuf.WellKnownTypes;

namespace CSharpWebAPI.Tests;

public class GroupChatServiceTests
{
    private readonly Mock<GrpcGroupChatService.GroupChatServiceClient> _mockGrpcClient;
    private readonly CSharpWebAPI.Services.GroupChatService _service;

    public GroupChatServiceTests()
    {
        _mockGrpcClient = new Mock<GrpcGroupChatService.GroupChatServiceClient>();
        _service = new CSharpWebAPI.Services.GroupChatService(_mockGrpcClient.Object);
    }

    [Fact]
    public async Task CreateGroupChatAsync_ValidRequest_ReturnsChatRoomDto()
    {
        // Arrange
        var ownerId = 1;
        var name = "Test Group";
        var description = "Test Description";
        var memberIds = new List<int> { 2, 3 };

        var grpcResponse = new CreateGroupChatResponse
        {
            Room = new ChatRoom
            {
                Id = 100,
                Name = "Test Group"
            }
        };

        _mockGrpcClient
            .Setup(c => c.CreateGroupChatAsync(
                It.IsAny<Chat.Grpc.CreateGroupChatRequest>(),
                null,
                null,
                default))
            .Returns(new AsyncUnaryCall<CreateGroupChatResponse>(
                Task.FromResult(grpcResponse),
                Task.FromResult(new Metadata()),
                () => Status.DefaultSuccess,
                () => new Metadata(),
                () => { }));

        // Act
        var result = await _service.CreateGroupChatAsync(ownerId, name, description, memberIds);

        // Assert
        result.Should().NotBeNull();
        result.Id.Should().Be(100);
        result.Name.Should().Be("Test Group");

        _mockGrpcClient.Verify(c => c.CreateGroupChatAsync(
            It.Is<Chat.Grpc.CreateGroupChatRequest>(r =>
                r.OwnerId == ownerId &&
                r.Name == name &&
                r.Description == description &&
                r.MemberIds.Count == 2
            ),
            null,
            null,
            default
        ), Times.Once);
    }

    [Fact]
    public async Task CreateGroupChatAsync_NullDescription_HandlesGracefully()
    {
        // Arrange
        var ownerId = 1;
        var name = "Test Group";
        string? description = null;
        var memberIds = new List<int>();

        var grpcResponse = new CreateGroupChatResponse
        {
            Room = new ChatRoom
            {
                Id = 100,
                Name = "Test Group"
            }
        };

        _mockGrpcClient
            .Setup(c => c.CreateGroupChatAsync(
                It.IsAny<Chat.Grpc.CreateGroupChatRequest>(),
                null,
                null,
                default))
            .Returns(new AsyncUnaryCall<CreateGroupChatResponse>(
                Task.FromResult(grpcResponse),
                Task.FromResult(new Metadata()),
                () => Status.DefaultSuccess,
                () => new Metadata(),
                () => { }));

        // Act
        var result = await _service.CreateGroupChatAsync(ownerId, name, description, memberIds);

        // Assert
        result.Should().NotBeNull();
        _mockGrpcClient.Verify(c => c.CreateGroupChatAsync(
            It.Is<Chat.Grpc.CreateGroupChatRequest>(r => r.Description == string.Empty),
            null,
            null,
            default
        ), Times.Once);
    }

    [Fact]
    public async Task AddMemberAsync_ValidRequest_CallsGrpcClient()
    {
        // Arrange
        var chatRoomId = 100;
        var requesterId = 1;
        var userId = 2;

        _mockGrpcClient
            .Setup(c => c.AddMemberAsync(
                It.IsAny<AddMemberRequest>(),
                null,
                null,
                default))
            .Returns(new AsyncUnaryCall<Empty>(
                Task.FromResult(new Empty()),
                Task.FromResult(new Metadata()),
                () => Status.DefaultSuccess,
                () => new Metadata(),
                () => { }));

        // Act
        await _service.AddMemberAsync(chatRoomId, requesterId, userId);

        // Assert
        _mockGrpcClient.Verify(c => c.AddMemberAsync(
            It.Is<AddMemberRequest>(r =>
                r.ChatRoomId == chatRoomId &&
                r.RequesterId == requesterId &&
                r.UserId == userId
            ),
            null,
            null,
            default
        ), Times.Once);
    }

    [Fact]
    public async Task ListMembersAsync_ValidRequest_ReturnsMemberList()
    {
        // Arrange
        var chatRoomId = 100;

        var grpcResponse = new ListMembersResponse
        {
            Members =
            {
                new Chat.Grpc.ChatRoomMemberDto
                {
                    UserId = 1,
                    Username = "Dani",
                    Role = "OWNER"
                },
                new Chat.Grpc.ChatRoomMemberDto
                {
                    UserId = 2,
                    Username = "Jwan",
                    Role = "MEMBER"
                }
            }
        };

        _mockGrpcClient
            .Setup(c => c.ListMembersAsync(
                It.IsAny<ListMembersRequest>(),
                null,
                null,
                default))
            .Returns(new AsyncUnaryCall<ListMembersResponse>(
                Task.FromResult(grpcResponse),
                Task.FromResult(new Metadata()),
                () => Status.DefaultSuccess,
                () => new Metadata(),
                () => { }));

        // Act
        var result = await _service.ListMembersAsync(chatRoomId);

        // Assert
        result.Should().NotBeNull();
        result.Should().HaveCount(2);
        result[0].UserId.Should().Be(1);
        result[0].Username.Should().Be("Dani");
        result[0].Role.Should().Be("OWNER");
        result[1].UserId.Should().Be(2);
        result[1].Username.Should().Be("Jwan");
        result[1].Role.Should().Be("MEMBER");
    }

    [Fact]
    public async Task ListUserChatRoomsAsync_ValidRequest_ReturnsRoomList()
    {
        // Arrange
        var userId = 1;

        var grpcResponse = new ListUserChatRoomsResponse
        {
            Rooms =
            {
                new ChatRoom { Id = 100, Name = "Group 1" },
                new ChatRoom { Id = 101, Name = "Group 2" }
            }
        };

        _mockGrpcClient
            .Setup(c => c.ListUserChatRoomsAsync(
                It.IsAny<ListUserChatRoomsRequest>(),
                null,
                null,
                default))
            .Returns(new AsyncUnaryCall<ListUserChatRoomsResponse>(
                Task.FromResult(grpcResponse),
                Task.FromResult(new Metadata()),
                () => Status.DefaultSuccess,
                () => new Metadata(),
                () => { }));

        // Act
        var result = await _service.ListUserChatRoomsAsync(userId);

        // Assert
        result.Should().NotBeNull();
        result.Should().HaveCount(2);
        result[0].Id.Should().Be(100);
        result[0].Name.Should().Be("Group 1");
        result[1].Id.Should().Be(101);
        result[1].Name.Should().Be("Group 2");
    }

    [Fact]
    public async Task GetPrivateChatRoomAsync_GrpcException_ThrowsInvalidOperationException()
    {
        // Arrange
        var userId1 = 1;
        var userId2 = 2;

        _mockGrpcClient
            .Setup(c => c.GetPrivateChatRoomAsync(
                It.IsAny<GetPrivateChatRoomRequest>(),
                null,
                null,
                default))
            .Returns(new AsyncUnaryCall<GetPrivateChatRoomResponse>(
                Task.FromException<GetPrivateChatRoomResponse>(
                    new RpcException(Status.DefaultCancelled, "Room not found")),
                Task.FromResult(new Metadata()),
                () => Status.DefaultCancelled,
                () => new Metadata(),
                () => { }));

        // Act & Assert
        await Assert.ThrowsAsync<InvalidOperationException>(async () =>
            await _service.GetPrivateChatRoomAsync(userId1, userId2));
    }
}

