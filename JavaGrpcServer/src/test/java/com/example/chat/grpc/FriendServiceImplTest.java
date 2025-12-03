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
import com.example.chat.domain.ChatRoomType;
import com.example.chat.domain.FriendRequest;
import com.example.chat.domain.FriendRequestStatus;
import com.example.chat.domain.Friendship;
import com.example.chat.domain.FriendshipId;
import com.example.chat.domain.PrivateChatRoom;
import com.example.chat.domain.User;
import com.example.chat.repositories.ChatRoomMembershipRepository;
import com.example.chat.repositories.ChatRoomRepository;
import com.example.chat.repositories.FriendRequestRepository;
import com.example.chat.repositories.FriendshipRepository;
import com.example.chat.repositories.PrivateChatRoomRepository;
import com.example.chat.repositories.UserRepository;

import io.grpc.stub.StreamObserver;

@ExtendWith(MockitoExtension.class)
class FriendServiceImplTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private PrivateChatRoomRepository privateChatRoomRepository;

    @Mock
    private ChatRoomMembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendServiceImpl service;

    private User dani;
    private User jwan;
    private User kelsang;
    private FriendRequest friendRequest;

    @BeforeEach
    void setUp() {
        dani = new User();
        dani.setId(1);
        dani.setUsername("dani");
        dani.setPasswordHash("hash1");
        dani.setCreatedAt(Instant.now());

        jwan = new User();
        jwan.setId(2);
        jwan.setUsername("jwan");
        jwan.setPasswordHash("hash2");
        jwan.setCreatedAt(Instant.now());

        kelsang = new User();
        kelsang.setId(3);
        kelsang.setUsername("kelsang");
        kelsang.setPasswordHash("hash3");
        kelsang.setCreatedAt(Instant.now());

        friendRequest = new FriendRequest();
        friendRequest.setId(100);
        friendRequest.setSender(dani);
        friendRequest.setReceiver(jwan);
        friendRequest.setStatus(FriendRequestStatus.PENDING);
        friendRequest.setCreatedAt(Instant.now());
    }

    @Test
    void testSendFriendRequest_Success() {
        SendFriendRequestRequest request = SendFriendRequestRequest.newBuilder()
            .setRequesterId(1)
            .setTargetUsername("jwan")
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendRequestDto> responseObserver = mock(StreamObserver.class);

        when(userRepository.findById(1)).thenReturn(Optional.of(dani));
        when(userRepository.findByUsername("jwan")).thenReturn(Optional.of(jwan));
        when(friendshipRepository.existsByIdUserIdAndIdFriendId(1, 2)).thenReturn(false);
        when(friendRequestRepository.findBySenderIdAndReceiverId(1, 2)).thenReturn(Optional.empty());
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> {
            FriendRequest fr = invocation.getArgument(0);
            fr.setId(100);
            fr.setCreatedAt(Instant.now());
            return fr;
        });

        service.sendFriendRequest(request, responseObserver);

        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
        verify(responseObserver, times(1)).onNext(any(FriendRequestDto.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testSendFriendRequest_UserNotFound() {
        SendFriendRequestRequest request = SendFriendRequestRequest.newBuilder()
            .setRequesterId(1)
            .setTargetUsername("nonexistent")
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendRequestDto> responseObserver = mock(StreamObserver.class);

        when(userRepository.findById(1)).thenReturn(Optional.of(dani));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        service.sendFriendRequest(request, responseObserver);

        verify(friendRequestRepository, never()).save(any());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
        verify(responseObserver, times(1)).onError(any());
    }

    @Test
    void testSendFriendRequest_AlreadyFriends() {
        SendFriendRequestRequest request = SendFriendRequestRequest.newBuilder()
            .setRequesterId(1)
            .setTargetUsername("jwan")
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendRequestDto> responseObserver = mock(StreamObserver.class);

        when(userRepository.findById(1)).thenReturn(Optional.of(dani));
        when(userRepository.findByUsername("jwan")).thenReturn(Optional.of(jwan));
        when(friendshipRepository.existsByIdUserIdAndIdFriendId(1, 2)).thenReturn(true);

        service.sendFriendRequest(request, responseObserver);

        verify(friendRequestRepository, never()).save(any());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
        verify(responseObserver, times(1)).onError(any());
    }

    @Test
    void testRespondFriendRequest_Accept() {
        RespondFriendRequestRequest request = RespondFriendRequestRequest.newBuilder()
            .setRequestId(100)
            .setAccept(true)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendRequestDto> responseObserver = mock(StreamObserver.class);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(200);
        chatRoom.setRoomType(ChatRoomType.PRIVATE);
        chatRoom.setCreatedAt(Instant.now());

        PrivateChatRoom privateChatRoom = new PrivateChatRoom();
        privateChatRoom.setId(200);
        privateChatRoom.setChatRoom(chatRoom);
        privateChatRoom.setUserA(dani);
        privateChatRoom.setUserB(jwan);

        when(friendRequestRepository.findById(100)).thenReturn(Optional.of(friendRequest));
        when(privateChatRoomRepository.findByUserAIdAndUserBId(1, 2)).thenReturn(Optional.of(privateChatRoom));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.respondFriendRequest(request, responseObserver);

        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
        verify(friendshipRepository, times(2)).save(any(Friendship.class));
        verify(responseObserver, times(1)).onNext(any(FriendRequestDto.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testRespondFriendRequest_Decline() {
        RespondFriendRequestRequest request = RespondFriendRequestRequest.newBuilder()
            .setRequestId(100)
            .setAccept(false)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendRequestDto> responseObserver = mock(StreamObserver.class);

        when(friendRequestRepository.findById(100)).thenReturn(Optional.of(friendRequest));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.respondFriendRequest(request, responseObserver);

        verify(friendRequestRepository, times(1)).save(any(FriendRequest.class));
        verify(friendshipRepository, never()).save(any());
        verify(responseObserver, times(1)).onNext(any(FriendRequestDto.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testListFriends_Success() {
        ListFriendsRequest request = ListFriendsRequest.newBuilder()
            .setUserId(1)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendListResponse> responseObserver = mock(StreamObserver.class);

        Friendship friendship = new Friendship();
        friendship.setId(new FriendshipId(1, 2));
        friendship.setFriend(jwan);

        List<Friendship> friendships = new ArrayList<>();
        friendships.add(friendship);

        when(friendshipRepository.findByIdUserId(1)).thenReturn(friendships);

        service.listFriends(request, responseObserver);

        verify(responseObserver, times(1)).onNext(any(FriendListResponse.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void testListIncomingRequests_Success() {
        ListFriendRequestsRequest request = ListFriendRequestsRequest.newBuilder()
            .setUserId(2)
            .build();

        @SuppressWarnings("unchecked")
        StreamObserver<FriendRequestListResponse> responseObserver = mock(StreamObserver.class);

        List<FriendRequest> requests = new ArrayList<>();
        requests.add(friendRequest);

        when(friendRequestRepository.findByReceiverIdAndStatus(2, FriendRequestStatus.PENDING)).thenReturn(requests);

        service.listIncomingRequests(request, responseObserver);

        verify(responseObserver, times(1)).onNext(any(FriendRequestListResponse.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }
}

