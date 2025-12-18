package com.example.chat.grpc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMembership;
import com.example.chat.domain.ChatRoomType;
import com.example.chat.domain.FriendRequest;
import com.example.chat.domain.FriendRequestStatus;
import com.example.chat.domain.Friendship;
import com.example.chat.domain.FriendshipId;
import com.example.chat.domain.MembershipRole;
import com.example.chat.domain.PrivateChatRoom;
import com.example.chat.domain.User;
import com.example.chat.grpc.FriendServiceGrpc.FriendServiceImplBase;
import com.example.chat.repositories.ChatRoomMembershipRepository;
import com.example.chat.repositories.ChatRoomRepository;
import com.example.chat.repositories.FriendRequestRepository;
import com.example.chat.repositories.FriendshipRepository;
import com.example.chat.repositories.PrivateChatRoomRepository;
import com.example.chat.repositories.UserRepository;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * gRPC service implementation for friend management operations.
 * This service handles friend requests, friendships, and private chat creation
 * between friends.
 */
@Service
public class FriendServiceImpl extends FriendServiceImplBase {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PrivateChatRoomRepository privateChatRoomRepository;
    private final ChatRoomMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Constructs a new FriendServiceImpl with the specified repositories.
     *
     * @param friendRequestRepository the repository for friend request data access
     * @param friendshipRepository the repository for friendship data access
     * @param chatRoomRepository the repository for chat room data access
     * @param privateChatRoomRepository the repository for private chat room data access
     * @param membershipRepository the repository for chat room membership data access
     * @param userRepository the repository for user data access
     */
    public FriendServiceImpl(
        FriendRequestRepository friendRequestRepository,
        FriendshipRepository friendshipRepository,
        ChatRoomRepository chatRoomRepository,
        PrivateChatRoomRepository privateChatRoomRepository,
        ChatRoomMembershipRepository membershipRepository,
        UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.privateChatRoomRepository = privateChatRoomRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Sends a friend request from one user to another.
     * Validates that users exist, are not the same person, are not already friends,
     * and don't have a pending request.
     *
     * @param request the request containing requester ID and target username
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void sendFriendRequest(SendFriendRequestRequest request,
        StreamObserver<FriendRequestDto> responseObserver) {
        try {
            if (request.getRequesterId() <= 0 || request.getTargetUsername().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Missing data")
                    .asRuntimeException());
                return;
            }

            Optional<User> sender = userRepository.findById(request.getRequesterId());
            Optional<User> receiver = userRepository.findByUsername(request.getTargetUsername().trim().toLowerCase());

            if (sender.isEmpty() || receiver.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found")
                    .asRuntimeException());
                return;
            }

            if (sender.get().getId().equals(receiver.get().getId())) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Cannot add yourself")
                    .asRuntimeException());
                return;
            }

            if (friendshipRepository.existsByIdUserIdAndIdFriendId(sender.get().getId(), receiver.get().getId())) {
                responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Already friends")
                    .asRuntimeException());
                return;
            }

            if (friendRequestRepository.findBySenderIdAndReceiverId(sender.get().getId(), receiver.get().getId()).isPresent()) {
                responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Request already sent")
                    .asRuntimeException());
                return;
            }

            FriendRequest entity = new FriendRequest();
            entity.setSender(sender.get());
            entity.setReceiver(receiver.get());
            entity = friendRequestRepository.save(entity);

            responseObserver.onNext(toDto(entity));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Responds to a friend request by accepting or declining it.
     * If accepted, creates bidirectional friendship records and a private chat room.
     *
     * @param request the request containing request ID and accept/decline decision
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void respondFriendRequest(RespondFriendRequestRequest request,
        StreamObserver<FriendRequestDto> responseObserver) {
        try {
            FriendRequest fr = friendRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> Status.NOT_FOUND
                    .withDescription("Request not found")
                    .asRuntimeException());

            if (fr.getStatus() != FriendRequestStatus.PENDING) {
                responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Request already handled")
                    .asRuntimeException());
                return;
            }

            fr.setStatus(request.getAccept() ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.DECLINED);
            fr.setRespondedAt(Instant.now());
            friendRequestRepository.save(fr);

            if (fr.getStatus() == FriendRequestStatus.ACCEPTED) {
                createFriendship(fr.getSender(), fr.getReceiver());
                createFriendship(fr.getReceiver(), fr.getSender());
                ensurePrivateChat(fr.getSender(), fr.getReceiver());
            }

            responseObserver.onNext(toDto(fr));
            responseObserver.onCompleted();
        } catch (StatusRuntimeException ex) {
            responseObserver.onError(ex);
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Lists all pending incoming friend requests for a user.
     *
     * @param request the request containing the user ID
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void listIncomingRequests(ListFriendRequestsRequest request,
        StreamObserver<FriendRequestListResponse> responseObserver) {
        try {
            List<FriendRequest> pending = friendRequestRepository
                .findByReceiverIdAndStatus(request.getUserId(), FriendRequestStatus.PENDING);
            FriendRequestListResponse.Builder builder = FriendRequestListResponse.newBuilder();
            for (FriendRequest fr : pending) {
                builder.addRequests(toDto(fr));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Lists all friends for a user.
     *
     * @param request the request containing the user ID
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void listFriends(ListFriendsRequest request,
        StreamObserver<FriendListResponse> responseObserver) {
        try {
            List<Friendship> friendships = friendshipRepository.findByIdUserId(request.getUserId());
            FriendListResponse.Builder builder = FriendListResponse.newBuilder();
            for (Friendship friendship : friendships) {
                User friend = friendship.getFriend();
                builder.addFriends(FriendDto.newBuilder()
                    .setUserId(friend.getId())
                    .setUsername(friend.getUsername())
                    .build());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Removes a friendship between two users.
     * Deletes both friendship records (bidirectional).
     *
     * @param request the request containing user ID and friend ID
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void removeFriend(RemoveFriendRequest request,
        StreamObserver<com.google.protobuf.Empty> responseObserver) {
        try {
            friendshipRepository.deleteById(new FriendshipId(request.getUserId(), request.getFriendId()));
            friendshipRepository.deleteById(new FriendshipId(request.getFriendId(), request.getUserId()));
            responseObserver.onNext(com.google.protobuf.Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Creates a unidirectional friendship record.
     *
     * @param user the user who owns the friendship record
     * @param friend the friend in the relationship
     */
    private void createFriendship(User user, User friend) {
        Friendship friendship = new Friendship();
        friendship.setId(new FriendshipId(user.getId(), friend.getId()));
        friendshipRepository.save(friendship);
    }

    /**
     * Ensures a private chat room exists between two users.
     * Creates the chat room and memberships if they don't exist.
     * Users are ordered by ID to ensure consistency.
     *
     * @param userA the first user
     * @param userB the second user
     */
    private void ensurePrivateChat(User userA, User userB) {
        Optional<PrivateChatRoom> existing = privateChatRoomRepository
            .findByUserAIdAndUserBId(userA.getId(), userB.getId());
        if (!existing.isPresent()) {
            existing = privateChatRoomRepository.findByUserAIdAndUserBId(userB.getId(), userA.getId());
        }
        if (existing.isPresent()) {
            return;
        }

        // Ensure consistent ordering (smaller ID first) to match database unique constraint
        User firstUser = userA.getId() < userB.getId() ? userA : userB;
        User secondUser = userA.getId() < userB.getId() ? userB : userA;

        ChatRoom room = new ChatRoom();
        room.setRoomType(ChatRoomType.PRIVATE);
        room = chatRoomRepository.save(room);

        // Flush to ensure room ID is generated
        chatRoomRepository.flush();

        // Get the room ID and use getReference to get a managed proxy
        // This prevents Hibernate from trying to persist the ChatRoom again
        Integer roomId = room.getId();
        ChatRoom managedRoom = entityManager.getReference(ChatRoom.class, roomId);

        PrivateChatRoom privateRoom = new PrivateChatRoom();
        privateRoom.setChatRoom(managedRoom);
        privateRoom.setUserA(firstUser);
        privateRoom.setUserB(secondUser);
        // Use merge() instead of save() to handle the entity properly
        privateRoom = entityManager.merge(privateRoom);
        entityManager.flush();

        addMembership(managedRoom, userA, MembershipRole.MEMBER);
        addMembership(managedRoom, userB, MembershipRole.MEMBER);
    }

    /**
     * Adds a user to a chat room with the specified role.
     * Does nothing if the membership already exists.
     *
     * @param room the chat room
     * @param user the user to add
     * @param role the role to assign to the user
     */
    private void addMembership(ChatRoom room, User user, MembershipRole role) {
        if (membershipRepository.existsByChatRoomAndUser(room, user)) {
            return;
        }

        ChatRoomMembership membership = new ChatRoomMembership();
        membership.setChatRoom(room);
        membership.setUser(user);
        membership.setRole(role);
        membershipRepository.save(membership);
    }

    /**
     * Converts a domain FriendRequest entity to a protobuf FriendRequestDto message.
     *
     * @param fr the domain friend request entity
     * @return the protobuf friend request DTO
     */
    private FriendRequestDto toDto(FriendRequest fr) {
        return FriendRequestDto.newBuilder()
            .setId(fr.getId())
            .setSenderId(fr.getSender().getId())
            .setSenderUsername(fr.getSender().getUsername())
            .setReceiverId(fr.getReceiver().getId())
            .setStatus(fr.getStatus().name())
            .setCreatedAtUnix(fr.getCreatedAt().getEpochSecond())
            .build();
    }
}
