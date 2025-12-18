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

/**
 * Main Spring Boot application class for the Chat System.
 * This class initializes and manages both the Spring Boot application context
 * and the gRPC server that exposes chat, user, friend, and group chat services.
 */
@SpringBootApplication
public class ChatApplication {

    private final ChatServiceImpl chatServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final FriendServiceImpl friendServiceImpl;
    private final GroupChatServiceImpl groupChatServiceImpl;
    private Server grpcServer;

    @Value("${grpc.port}")
    private int grpcPort;

    /**
     * Constructs a new ChatApplication with the required service implementations.
     *
     * @param chatServiceImpl the chat service implementation
     * @param userServiceImpl the user service implementation
     * @param friendServiceImpl the friend service implementation
     * @param groupChatServiceImpl the group chat service implementation
     */
    public ChatApplication(ChatServiceImpl chatServiceImpl, UserServiceImpl userServiceImpl,
            FriendServiceImpl friendServiceImpl, GroupChatServiceImpl groupChatServiceImpl) {
        this.chatServiceImpl = chatServiceImpl;
        this.userServiceImpl = userServiceImpl;
        this.friendServiceImpl = friendServiceImpl;
        this.groupChatServiceImpl = groupChatServiceImpl;
    }

    /**
     * Main entry point for the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    /**
     * Initializes and starts the gRPC server after the Spring context is initialized.
     * This method is called automatically by Spring after dependency injection.
     * The server is configured with all service implementations and starts listening
     * on the configured port.
     *
     * @throws Exception if the server fails to start
     */
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

    /**
     * Stops the gRPC server during application shutdown.
     * This method is called automatically by Spring before the application context is destroyed.
     */
    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            grpcServer.shutdown();
        }
    }
}
