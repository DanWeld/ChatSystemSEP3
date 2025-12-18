package com.example.chat.grpc;

import java.time.Instant;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.chat.domain.User;
import com.example.chat.grpc.UserServiceGrpc.UserServiceImplBase;
import com.example.chat.repositories.UserRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * gRPC service implementation for user management operations.
 * This service handles user registration, authentication, and user data retrieval.
 */
@Service
public class UserServiceImpl extends UserServiceImplBase {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserServiceImpl with the specified repository.
     *
     * @param userRepository the repository for user data access
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Registers a new user in the system.
     * Validates that the username is unique and stores the password as a BCrypt hash.
     *
     * @param request the registration request containing username and password
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void registerUser(RegisterUserRequest request, StreamObserver<RegisterUserResponse> responseObserver) {
        try {
            String normalizedUsername = request.getUsername().trim().toLowerCase();
            if (userRepository.findByUsername(normalizedUsername).isPresent()) {
                responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Username already taken")
                    .asRuntimeException());
                return;
            }

            User user = new User();
            user.setUsername(normalizedUsername);
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user = userRepository.save(user);

            RegisterUserResponse response = RegisterUserResponse.newBuilder()
                .setUser(toProtoUser(user))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Authenticates a user with username and password.
     * Validates credentials and returns user information if successful.
     *
     * @param request the login request containing username and password
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            String normalizedUsername = request.getUsername().trim().toLowerCase();
            Optional<User> userOpt = userRepository.findByUsername(normalizedUsername);
            if (!userOpt.isPresent() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
                responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription("Invalid credentials")
                    .asRuntimeException());
                return;
            }

            LoginResponse response = LoginResponse.newBuilder()
                .setUser(toProtoUser(userOpt.get()))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Retrieves user information by user ID.
     *
     * @param request the request containing the user ID
     * @param responseObserver the observer to receive the response or error
     */
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (!userOpt.isPresent()) {
                responseObserver.onError(Status.NOT_FOUND
                    .withDescription("User not found")
                    .asRuntimeException());
                return;
            }

            GetUserResponse response = GetUserResponse.newBuilder()
                .setUser(toProtoUser(userOpt.get()))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        catch (Exception ex)
        {
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage())
                .withCause(ex).asRuntimeException());
        }
    }

    /**
     * Converts a domain User entity to a protobuf User message.
     *
     * @param user the domain user entity
     * @return the protobuf user message
     */
    private com.example.chat.grpc.User toProtoUser(User user)
    {
        return com.example.chat.grpc.User.newBuilder()
            .setId(user.getId())
            .setUsername(user.getUsername())
            .setCreatedAtUnix(user.getCreatedAt() != null ? user.getCreatedAt().getEpochSecond() : Instant.now().getEpochSecond())
            .build();
    }
}

