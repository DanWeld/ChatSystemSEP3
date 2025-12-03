package com.example.chat.grpc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMembership;
import com.example.chat.domain.ChatRoomType;
import com.example.chat.domain.GroupChatRoom;
import com.example.chat.domain.MembershipRole;
import com.example.chat.domain.User;
import com.example.chat.repositories.ChatRoomMembershipRepository;
import com.example.chat.repositories.ChatRoomRepository;
import com.example.chat.repositories.GroupChatRoomRepository;
import com.example.chat.repositories.PrivateChatRoomRepository;
import com.example.chat.repositories.UserRepository;

import io.grpc.stub.StreamObserver;

@ExtendWith(MockitoExtension.class)
class GroupChatServiceImplTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private GroupChatRoomRepository groupChatRoomRepository;

    @Mock
    private ChatRoomMembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupChatServiceImpl service;

    private User owner;
    private User member1;
    private User member2;
    private ChatRoom chatRoom;
    private GroupChatRoom groupChatRoom;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setUsername("Dani");
        owner.setPasswordHash("hash1");
        owner.setCreatedAt(Instant.now());

        member1 = new User();
        member1.setId(2);
        member1.setUsername("Jwan");
        member1.setPasswordHash("hash2");
        member1.setCreatedAt(Instant.now());

        member2 = new User();
        member2.setId(3);
        member2.setUsername("Kelsang");
        member2.setPasswordHash("hash3");
        member2.setCreatedAt(Instant.now());

        chatRoom = new ChatRoom();
        chatRoom.setId(100);
        chatRoom.setRoomType(ChatRoomType.GROUP);
        chatRoom.setOwner(owner);
        chatRoom.setCreatedAt(Instant.now());

        groupChatRoom = new GroupChatRoom();
        groupChatRoom.setChatRoomId(100);
        groupChatRoom.setName("Test Group");
        groupChatRoom.setDescription("Test Description");
    }

    @Test
    void testCreateGroupChat_Success() {
        CreateGroupChatRequest request = CreateGroupChatRequest.newBuilder()
            .setOwnerId(1)
            .setName("Test Group")
            .setDescription("Test Description")
            .addMemberIds(2)
            .addMemberIds(3)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<CreateGroupChatResponse> responseObserver = mock(StreamObserver.class);

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(userRepository.findById(2)).thenReturn(Optional.of(member1));
        when(userRepository.findById(3)).thenReturn(Optional.of(member2));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom cr = invocation.getArgument(0);
            cr.setId(100);
            return cr;
        });
        when(groupChatRoomRepository.save(any(GroupChatRoom.class))).thenAnswer(invocation -> {
            GroupChatRoom gcr = invocation.getArgument(0);
            gcr.setChatRoomId(100);
            return gcr;
        });
        when(membershipRepository.save(any(ChatRoomMembership.class))).thenAnswer(invocation -> {
            ChatRoomMembership m = invocation.getArgument(0);
            return m;
        });

        service.createGroupChat(request, responseObserver);

        verify(userRepository, times(1)).findById(1);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(groupChatRoomRepository, times(1)).save(any(GroupChatRoom.class));
        verify(membershipRepository, times(3)).save(any(ChatRoomMembership.class)); // owner + 2 members
        verify(responseObserver, times(1)).onNext(any(CreateGroupChatResponse.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testCreateGroupChat_OwnerNotFound() {
        CreateGroupChatRequest request = CreateGroupChatRequest.newBuilder()
            .setOwnerId(999)
            .setName("Test Group")
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<CreateGroupChatResponse> responseObserver = mock(StreamObserver.class);

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        service.createGroupChat(request, responseObserver);

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
        verify(responseObserver, times(1)).onError(any());
    }

    @Test
    void testAddMember_Success() {
        AddMemberRequest request = AddMemberRequest.newBuilder()
            .setChatRoomId(100)
            .setRequesterId(1)
            .setUserId(2)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<com.google.protobuf.Empty> responseObserver = mock(StreamObserver.class);

        ChatRoomMembership ownerMembership = new ChatRoomMembership();
        ownerMembership.setChatRoom(chatRoom);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(MembershipRole.OWNER);

        when(chatRoomRepository.findById(100)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(userRepository.findById(2)).thenReturn(Optional.of(member1));
        when(membershipRepository.findByChatRoomAndUser(chatRoom, owner)).thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.existsByChatRoomAndUser(chatRoom, member1)).thenReturn(false);
        when(membershipRepository.save(any(ChatRoomMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.addMember(request, responseObserver);

        verify(membershipRepository, times(1)).save(any(ChatRoomMembership.class));
        verify(responseObserver, times(1)).onNext(any());
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testAddMember_InsufficientPermissions() {
        AddMemberRequest request = AddMemberRequest.newBuilder()
            .setChatRoomId(100)
            .setRequesterId(2)
            .setUserId(3)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<com.google.protobuf.Empty> responseObserver = mock(StreamObserver.class);

        ChatRoomMembership memberMembership = new ChatRoomMembership();
        memberMembership.setChatRoom(chatRoom);
        memberMembership.setUser(member1);
        memberMembership.setRole(MembershipRole.MEMBER);

        when(chatRoomRepository.findById(100)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(2)).thenReturn(Optional.of(member1));
        when(membershipRepository.findByChatRoomAndUser(chatRoom, member1)).thenReturn(Optional.of(memberMembership));

        service.addMember(request, responseObserver);

        verify(membershipRepository, never()).save(any());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
        verify(responseObserver, times(1)).onError(any());
    }

    @Test
    void testListMembers_Success() {
        ListMembersRequest request = ListMembersRequest.newBuilder()
            .setChatRoomId(100)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<ListMembersResponse> responseObserver = mock(StreamObserver.class);

        ChatRoomMembership ownerMembership = new ChatRoomMembership();
        ownerMembership.setChatRoom(chatRoom);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(MembershipRole.OWNER);

        ChatRoomMembership memberMembership = new ChatRoomMembership();
        memberMembership.setChatRoom(chatRoom);
        memberMembership.setUser(member1);
        memberMembership.setRole(MembershipRole.MEMBER);

        List<ChatRoomMembership> memberships = new ArrayList<>();
        memberships.add(ownerMembership);
        memberships.add(memberMembership);

        when(chatRoomRepository.findById(100)).thenReturn(Optional.of(chatRoom));
        when(membershipRepository.findByChatRoom(chatRoom)).thenReturn(memberships);

        service.listMembers(request, responseObserver);

        verify(responseObserver, times(1)).onNext(any(ListMembersResponse.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testListUserChatRooms_Success() {
        ListUserChatRoomsRequest request = ListUserChatRoomsRequest.newBuilder()
            .setUserId(1)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<ListUserChatRoomsResponse> responseObserver = mock(StreamObserver.class);

        ChatRoomMembership membership = new ChatRoomMembership();
        membership.setChatRoom(chatRoom);
        membership.setUser(owner);
        membership.setRole(MembershipRole.OWNER);

        List<ChatRoomMembership> memberships = new ArrayList<>();
        memberships.add(membership);

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(membershipRepository.findByUser(owner)).thenReturn(memberships);
        when(groupChatRoomRepository.findByChatRoomId(100)).thenReturn(Optional.of(groupChatRoom));

        service.listUserChatRooms(request, responseObserver);

        verify(responseObserver, times(1)).onNext(any(ListUserChatRoomsResponse.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }
}

