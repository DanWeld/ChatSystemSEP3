package com.example.chat.grpc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.chat.domain.ChatRoom;
import com.example.chat.grpc.ChatServiceGrpc.ChatServiceImplBase;
import com.example.chat.repositories.ChatRoomRepository;
import com.example.chat.repositories.MessageRepository;
import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

@Service
public class ChatServiceImpl extends ChatServiceImplBase {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatServiceImpl(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @Override
    public void sendMessage(SendMessageRequest request,
        StreamObserver<SendMessageResponse> responseObserver) {
        try {
            com.example.chat.domain.Message m = new com.example.chat.domain.Message();
            m.setChatRoomId(request.getChatRoomId());
            m.setSenderId(request.getSenderId());
            m.setText(request.getText());
            m = messageRepository.save(m);

            responseObserver.onNext(SendMessageResponse.newBuilder().setMessage(mapMessage(m)).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void getMessages(GetMessagesRequest request,
        StreamObserver<GetMessagesResponse> responseObserver) {
        try {
            List<com.example.chat.domain.Message> messages =
                messageRepository.findByChatRoomIdOrderByCreatedAtAsc(request.getChatRoomId());

            GetMessagesResponse.Builder resp = GetMessagesResponse.newBuilder();

            for (com.example.chat.domain.Message m : messages) {
                resp.addMessages(mapMessage(m));
            }

            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void listChatRooms(Empty request, StreamObserver<ListChatRoomsResponse> responseObserver) {
        try {
            List<ChatRoom> rooms = chatRoomRepository.findAll();
            ListChatRoomsResponse.Builder builder = ListChatRoomsResponse.newBuilder();
            for (ChatRoom room : rooms) {
                builder.addRooms(mapRoom(room));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void editMessage(EditMessageRequest request, StreamObserver<Message> responseObserver) {
        try {
            Optional<com.example.chat.domain.Message> messageOpt = messageRepository.findById(request.getMessageId());
            if (!messageOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Message not found").asRuntimeException());
                return;
            }

            com.example.chat.domain.Message message = messageOpt.get();
            if (!message.getSenderId().equals(request.getSenderId())) {
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Cannot edit another user's message")
                    .asRuntimeException());
                return;
            }
            if (message.isDeleted()) {
                responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("Cannot edit deleted message")
                    .asRuntimeException());
                return;
            }
            String newText = request.getText().trim();
            if (newText.isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Message text is required")
                    .asRuntimeException());
                return;
            }

            message.setText(newText);
            message.setEdited(true);
            message.setEditedAt(Instant.now());
            message = messageRepository.save(message);

            responseObserver.onNext(mapMessage(message));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void deleteMessage(DeleteMessageRequest request, StreamObserver<Message> responseObserver) {
        try {
            Optional<com.example.chat.domain.Message> messageOpt = messageRepository.findById(request.getMessageId());
            if (!messageOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Message not found").asRuntimeException());
                return;
            }

            com.example.chat.domain.Message message = messageOpt.get();
            if (!message.getSenderId().equals(request.getRequesterId())) {
                responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Cannot delete another user's message")
                    .asRuntimeException());
                return;
            }
            if (message.isDeleted()) {
                responseObserver.onNext(mapMessage(message));
                responseObserver.onCompleted();
                return;
            }

            message.setDeleted(true);
            message.setDeletedAt(Instant.now());
            message.setDeletedBy(request.getRequesterId());
            message.setText("");
            message = messageRepository.save(message);

            responseObserver.onNext(mapMessage(message));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void searchMessages(SearchMessagesRequest request, StreamObserver<GetMessagesResponse> responseObserver) {
        try {
            String query = request.getQuery().trim();
            if (query.isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Query text is required")
                    .asRuntimeException());
                return;
            }

            List<com.example.chat.domain.Message> messages = messageRepository
                .findByChatRoomIdAndTextContainingIgnoreCaseOrderByCreatedAtAsc(request.getChatRoomId(), query);

            GetMessagesResponse.Builder resp = GetMessagesResponse.newBuilder();
            for (com.example.chat.domain.Message message : messages) {
                resp.addMessages(mapMessage(message));
            }

            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    @Override
    public void createChatRoom(CreateChatRoomRequest request,
        StreamObserver<CreateChatRoomResponse> responseObserver) {
        try {
            String normalizedName = request.getName().trim();
            if (normalizedName.isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Chat room name is required")
                    .asRuntimeException());
                return;
            }

            ChatRoom room = new ChatRoom();
            room.setRoomType(com.example.chat.domain.ChatRoomType.GROUP);
            room = chatRoomRepository.save(room);

            responseObserver.onNext(CreateChatRoomResponse.newBuilder()
                .setRoom(mapRoom(room))
                .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    private com.example.chat.grpc.ChatRoom mapRoom(ChatRoom room) {
        String name = "Room " + room.getId();
        return com.example.chat.grpc.ChatRoom.newBuilder()
            .setId(room.getId())
            .setName(name)
            .build();
    }

    private com.example.chat.grpc.Message mapMessage(com.example.chat.domain.Message message) {
        com.example.chat.grpc.Message.Builder builder = com.example.chat.grpc.Message.newBuilder()
            .setId(message.getId())
            .setChatRoomId(message.getChatRoomId())
            .setSenderId(message.getSenderId())
            .setText(message.getText() == null ? "" : message.getText())
            .setSentAtUnix(message.getCreatedAt() != null ? message.getCreatedAt().getEpochSecond() : Instant.now().getEpochSecond())
            .setIsEdited(message.isEdited())
            .setIsDeleted(message.isDeleted());
        return builder.build();
    }
}