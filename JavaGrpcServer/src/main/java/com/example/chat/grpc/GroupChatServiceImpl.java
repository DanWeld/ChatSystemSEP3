package com.example.chat.grpc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMembership;
import com.example.chat.domain.ChatRoomType;
import com.example.chat.domain.GroupChatRoom;
import com.example.chat.domain.MembershipRole;
import com.example.chat.domain.PrivateChatRoom;
import com.example.chat.domain.User;
import com.example.chat.grpc.GroupChatServiceGrpc.GroupChatServiceImplBase;
import com.example.chat.repositories.ChatRoomMembershipRepository;
import com.example.chat.repositories.ChatRoomRepository;
import com.example.chat.repositories.GroupChatRoomRepository;
import com.example.chat.repositories.PrivateChatRoomRepository;
import com.example.chat.repositories.UserRepository;
import com.google.protobuf.Empty;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * gRPC service implementation for group chat operations.
 * This service handles creating group chats, managing members, promoting members,
 * and retrieving private chat rooms between users.
 */
@Service
public class GroupChatServiceImpl extends GroupChatServiceImplBase {

    private final ChatRoomRepository chatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final PrivateChatRoomRepository privateChatRoomRepository;
    private final ChatRoomMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Constructs a new GroupChatServiceImpl with the specified repositories.
     *
     * @param chatRoomRepository the repository for chat room data access
     * @param groupChatRoomRepository the repository for group chat room data access
     * @param privateChatRoomRepository the repository for private chat room data access
     * @param membershipRepository the repository for chat room membership data access
     * @param userRepository the repository for user data access
     */
    public GroupChatServiceImpl(
            ChatRoomRepository chatRoomRepository,
            GroupChatRoomRepository groupChatRoomRepository,
            PrivateChatRoomRepository privateChatRoomRepository,
            ChatRoomMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.groupChatRoomRepository = groupChatRoomRepository;
        this.privateChatRoomRepository = privateChatRoomRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new group chat with the specified owner and members.
     *
     * @param request the request containing owner ID, group name, description, and member IDs
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void createGroupChat(CreateGroupChatRequest request, StreamObserver<CreateGroupChatResponse> responseObserver) {
        try {
            Optional<User> ownerOpt = userRepository.findById(request.getOwnerId());
            if (!ownerOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Owner not found").asRuntimeException());
                return;
            }

            User owner = ownerOpt.get();

            // Create ChatRoom
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomType(ChatRoomType.GROUP);
            chatRoom.setOwner(owner);
            chatRoom = chatRoomRepository.save(chatRoom);

            // Create GroupChatRoom
            GroupChatRoom groupChatRoom = new GroupChatRoom();
            groupChatRoom.setChatRoomId(chatRoom.getId());
            groupChatRoom.setName(request.getName());
            groupChatRoom.setDescription(request.getDescription());
            groupChatRoom = groupChatRoomRepository.save(groupChatRoom);

            // Add owner as OWNER
            ChatRoomMembership ownerMembership = new ChatRoomMembership();
            ownerMembership.setChatRoom(chatRoom);
            ownerMembership.setUser(owner);
            ownerMembership.setRole(MembershipRole.OWNER);
            membershipRepository.save(ownerMembership);

            // Add other members
            for (int userId : request.getMemberIdsList()) {
                if (userId == owner.getId()) {
                    continue; // Skip owner, already added
                }
                Optional<User> memberOpt = userRepository.findById(userId);
                if (memberOpt.isPresent()) {
                    ChatRoomMembership membership = new ChatRoomMembership();
                    membership.setChatRoom(chatRoom);
                    membership.setUser(memberOpt.get());
                    membership.setRole(MembershipRole.MEMBER);
                    membershipRepository.save(membership);
                }
            }

            responseObserver.onNext(CreateGroupChatResponse.newBuilder()
                    .setRoom(mapChatRoom(chatRoom, groupChatRoom.getName()))
                    .build());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Adds a member to a group chat.
     * Requires the requester to be an OWNER or ADMIN of the group.
     *
     * @param request the request containing chat room ID, requester ID, and user ID to add
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void addMember(AddMemberRequest request, StreamObserver<Empty> responseObserver) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findById(request.getChatRoomId());
            if (!roomOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Chat room not found").asRuntimeException());
                return;
            }

            ChatRoom room = roomOpt.get();
            if (room.getRoomType() != ChatRoomType.GROUP) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Not a group chat").asRuntimeException());
                return;
            }

            // Check requester has permission (OWNER or ADMIN)
            Optional<User> requesterOpt = userRepository.findById(request.getRequesterId());
            if (!requesterOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Requester not found").asRuntimeException());
                return;
            }

            Optional<ChatRoomMembership> requesterMembership = membershipRepository.findByChatRoomAndUser(room, requesterOpt.get());
            if (!requesterMembership.isPresent() ||
                    (requesterMembership.get().getRole() != MembershipRole.OWNER &&
                     requesterMembership.get().getRole() != MembershipRole.ADMIN)) {
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Insufficient permissions").asRuntimeException());
                return;
            }

            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (!userOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }

            if (membershipRepository.existsByChatRoomAndUser(room, userOpt.get())) {
                responseObserver.onError(Status.ALREADY_EXISTS.withDescription("User already a member").asRuntimeException());
                return;
            }

            ChatRoomMembership membership = new ChatRoomMembership();
            membership.setChatRoom(room);
            membership.setUser(userOpt.get());
            membership.setRole(MembershipRole.MEMBER);
            membershipRepository.save(membership);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Removes a member from a group chat.
     * Requires the requester to be an OWNER or ADMIN. Cannot remove the owner.
     *
     * @param request the request containing chat room ID, requester ID, and user ID to remove
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void removeMember(RemoveMemberRequest request, StreamObserver<Empty> responseObserver) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findById(request.getChatRoomId());
            if (!roomOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Chat room not found").asRuntimeException());
                return;
            }

            ChatRoom room = roomOpt.get();
            if (room.getRoomType() != ChatRoomType.GROUP) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Not a group chat").asRuntimeException());
                return;
            }

            Optional<User> requesterOpt = userRepository.findById(request.getRequesterId());
            if (!requesterOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Requester not found").asRuntimeException());
                return;
            }

            Optional<ChatRoomMembership> requesterMembership = membershipRepository.findByChatRoomAndUser(room, requesterOpt.get());
            if (!requesterMembership.isPresent() ||
                    (requesterMembership.get().getRole() != MembershipRole.OWNER &&
                     requesterMembership.get().getRole() != MembershipRole.ADMIN)) {
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Insufficient permissions").asRuntimeException());
                return;
            }

            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (!userOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }

            Optional<ChatRoomMembership> membership = membershipRepository.findByChatRoomAndUser(room, userOpt.get());
            if (!membership.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not a member").asRuntimeException());
                return;
            }

            // Prevent removing owner
            if (membership.get().getRole() == MembershipRole.OWNER) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Cannot remove owner").asRuntimeException());
                return;
            }

            membershipRepository.delete(membership.get());

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Promotes a member to ADMIN role.
     * Only the OWNER can promote members. Members are promoted from MEMBER to ADMIN.
     *
     * @param request the request containing chat room ID, requester ID, and user ID to promote
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void promoteMember(PromoteMemberRequest request, StreamObserver<Empty> responseObserver) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findById(request.getChatRoomId());
            if (!roomOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Chat room not found").asRuntimeException());
                return;
            }

            ChatRoom room = roomOpt.get();
            if (room.getRoomType() != ChatRoomType.GROUP) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Not a group chat").asRuntimeException());
                return;
            }

            Optional<User> requesterOpt = userRepository.findById(request.getRequesterId());
            if (!requesterOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Requester not found").asRuntimeException());
                return;
            }

            Optional<ChatRoomMembership> requesterMembership = membershipRepository.findByChatRoomAndUser(room, requesterOpt.get());
            if (!requesterMembership.isPresent() || requesterMembership.get().getRole() != MembershipRole.OWNER) {
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Only owner can promote members").asRuntimeException());
                return;
            }

            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (!userOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }

            Optional<ChatRoomMembership> membership = membershipRepository.findByChatRoomAndUser(room, userOpt.get());
            if (!membership.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not a member").asRuntimeException());
                return;
            }

            if (membership.get().getRole() == MembershipRole.MEMBER) {
                membership.get().setRole(MembershipRole.ADMIN);
                membershipRepository.save(membership.get());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Lists all members of a chat room with their roles.
     *
     * @param request the request containing the chat room ID
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void listMembers(ListMembersRequest request, StreamObserver<ListMembersResponse> responseObserver) {
        try {
            Optional<ChatRoom> roomOpt = chatRoomRepository.findById(request.getChatRoomId());
            if (!roomOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Chat room not found").asRuntimeException());
                return;
            }

            List<ChatRoomMembership> memberships = membershipRepository.findByChatRoom(roomOpt.get());
            ListMembersResponse.Builder builder = ListMembersResponse.newBuilder();

            for (ChatRoomMembership m : memberships) {
                builder.addMembers(ChatRoomMemberDto.newBuilder()
                        .setUserId(m.getUser().getId())
                        .setUsername(m.getUser().getUsername())
                        .setRole(m.getRole().name())
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Lists all chat rooms (group chats only) that a user is a member of.
     *
     * @param request the request containing the user ID
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void listUserChatRooms(ListUserChatRoomsRequest request, StreamObserver<ListUserChatRoomsResponse> responseObserver) {
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (!userOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }

            List<ChatRoomMembership> memberships = membershipRepository.findByUser(userOpt.get());
            ListUserChatRoomsResponse.Builder builder = ListUserChatRoomsResponse.newBuilder();

            for (ChatRoomMembership m : memberships) {
                ChatRoom room = m.getChatRoom();
                // Only include group chats, not private chats
                if (room.getRoomType() == ChatRoomType.GROUP) {
                    String name = getChatRoomName(room);
                    builder.addRooms(com.example.chat.grpc.ChatRoom.newBuilder()
                            .setId(room.getId())
                            .setName(name)
                            .build());
                }
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Gets or creates a private chat room between two users.
     * Creates the chat room and memberships if they don't already exist.
     *
     * @param request the request containing the two user IDs
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    @Transactional
    public void getPrivateChatRoom(GetPrivateChatRoomRequest request, StreamObserver<GetPrivateChatRoomResponse> responseObserver) {
        try {
            Optional<User> user1Opt = userRepository.findById(request.getUserId1());
            Optional<User> user2Opt = userRepository.findById(request.getUserId2());

            if (!user1Opt.isPresent() || !user2Opt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }

            PrivateChatRoom privateRoom = findOrCreatePrivateChat(user1Opt.get(), user2Opt.get());
            ChatRoom room = privateRoom.getChatRoom();
            String name = getChatRoomName(room);

            responseObserver.onNext(GetPrivateChatRoomResponse.newBuilder()
                    .setRoom(com.example.chat.grpc.ChatRoom.newBuilder()
                            .setId(room.getId())
                            .setName(name)
                            .build())
                    .build());
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String errorMsg = "Error getting private chat room: " + ex.getMessage();
            if (ex.getCause() != null) {
                errorMsg += " (Caused by: " + ex.getCause().getMessage() + ")";
            }
            // Include stack trace in detail for debugging
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            errorMsg += "\nStack trace: " + sw.toString();
            responseObserver.onError(Status.INTERNAL.withDescription(errorMsg).withCause(ex).asRuntimeException());
        }
    }

    /**
     * Finds an existing private chat room or creates a new one between two users.
     * Users are ordered by ID to ensure consistency with database constraints.
     *
     * @param userA the first user
     * @param userB the second user
     * @return the private chat room
     */
    @Transactional
    private PrivateChatRoom findOrCreatePrivateChat(User userA, User userB) {
        // Check both orders for existing room
        Optional<PrivateChatRoom> privateRoom = privateChatRoomRepository.findByUserAIdAndUserBId(
                userA.getId(), userB.getId());
        if (!privateRoom.isPresent()) {
            privateRoom = privateChatRoomRepository.findByUserAIdAndUserBId(
                    userB.getId(), userA.getId());
        }
        if (privateRoom.isPresent()) {
            return privateRoom.get();
        }

        // Ensure consistent ordering (smaller ID first) to match database unique constraint
        User firstUser = userA.getId() < userB.getId() ? userA : userB;
        User secondUser = userA.getId() < userB.getId() ? userB : userA;

        // Create ChatRoom first
        ChatRoom room = new ChatRoom();
        room.setRoomType(ChatRoomType.PRIVATE);
        room = chatRoomRepository.save(room);
        
        // Flush to ensure room ID is generated before creating PrivateChatRoom (needed for @MapsId)
        chatRoomRepository.flush();
        
        // Get the room ID and use getReference to get a managed proxy
        // This prevents Hibernate from trying to persist the ChatRoom again
        Integer roomId = room.getId();
        ChatRoom managedRoom = entityManager.getReference(ChatRoom.class, roomId);

        // Create PrivateChatRoom - @MapsId will use managedRoom.getId() automatically
        PrivateChatRoom newRoom = new PrivateChatRoom();
        newRoom.setChatRoom(managedRoom);
        newRoom.setUserA(firstUser);
        newRoom.setUserB(secondUser);
        // Use repository save which handles transactions properly
        newRoom = privateChatRoomRepository.save(newRoom);

        // Ensure both users are members (use managedRoom for consistency)
        ensureMembership(managedRoom, userA);
        ensureMembership(managedRoom, userB);

        return newRoom;
    }

    /**
     * Ensures a user is a member of a chat room.
     * Creates the membership if it doesn't exist.
     *
     * @param room the chat room
     * @param user the user
     */
    private void ensureMembership(ChatRoom room, User user) {
        if (membershipRepository.existsByChatRoomAndUser(room, user)) {
            return;
        }

        ChatRoomMembership membership = new ChatRoomMembership();
        membership.setChatRoom(room);
        membership.setUser(user);
        membership.setRole(MembershipRole.MEMBER);
        membershipRepository.save(membership);
    }

    /**
     * Maps a domain ChatRoom entity to a protobuf ChatRoom message.
     *
     * @param room the domain chat room entity
     * @param name the name to use for the chat room
     * @return the protobuf chat room message
     */
    private com.example.chat.grpc.ChatRoom mapChatRoom(ChatRoom room, String name) {
        return com.example.chat.grpc.ChatRoom.newBuilder()
                .setId(room.getId())
                .setName(name)
                .build();
    }

    /**
     * Gets the display name for a chat room.
     * For group chats, returns the group name. For private chats, returns "UserA & UserB".
     * Falls back to "Room {id}" if name cannot be determined.
     *
     * @param room the chat room
     * @return the display name
     */
    private String getChatRoomName(ChatRoom room) {
        if (room.getRoomType() == ChatRoomType.GROUP) {
            Optional<GroupChatRoom> groupRoom = groupChatRoomRepository.findByChatRoomId(room.getId());
            if (groupRoom.isPresent()) {
                return groupRoom.get().getName();
            }
        } else if (room.getRoomType() == ChatRoomType.PRIVATE) {
            Optional<PrivateChatRoom> privateRoom = privateChatRoomRepository.findById(room.getId());
            if (privateRoom.isPresent()) {
                return privateRoom.get().getUserA().getUsername() + " & " + privateRoom.get().getUserB().getUsername();
            }
        }
        return "Room " + room.getId();
    }
}
