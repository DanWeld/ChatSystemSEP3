package com.example.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.chat.grpc.ChatServiceImpl;
import com.example.chat.grpc.FriendServiceImpl;
import com.example.chat.grpc.GroupChatServiceImpl;
import com.example.chat.grpc.UserServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class ChatApplication {

    private final ChatServiceImpl chatServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final FriendServiceImpl friendServiceImpl;
    private final GroupChatServiceImpl groupChatServiceImpl;
    private Server grpcServer;

    @Value("${grpc.port}")
    private int grpcPort;

    public ChatApplication(ChatServiceImpl chatServiceImpl, UserServiceImpl userServiceImpl,
            FriendServiceImpl friendServiceImpl, GroupChatServiceImpl groupChatServiceImpl) {
        this.chatServiceImpl = chatServiceImpl;
        this.userServiceImpl = userServiceImpl;
        this.friendServiceImpl = friendServiceImpl;
        this.groupChatServiceImpl = groupChatServiceImpl;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @PostConstruct
    public void startGrpcServer() throws Exception {
        System.out.println("Starting gRPC server on port " + grpcPort + "...");
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(chatServiceImpl)
                .addService(userServiceImpl)
                .addService(friendServiceImpl)
                .addService(groupChatServiceImpl)
                .build()
                .start();

        System.out.println("✓ gRPC server started successfully on port " + grpcPort);
        System.out.println("  - ChatService: available");
        System.out.println("  - UserService: available");
        System.out.println("  - FriendService: available");
        System.out.println("  - GroupChatService: available");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            if (grpcServer != null) {
                grpcServer.shutdown();
            }
        }));

        // Keep server running in a non-daemon thread
        Thread awaitThread = new Thread(() -> {
            try {
                grpcServer.awaitTermination();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            grpcServer.shutdown();
        }
    }
}
