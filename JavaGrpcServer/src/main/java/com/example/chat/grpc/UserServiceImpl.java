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

@Service
public class UserServiceImpl extends UserServiceImplBase {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

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

    private com.example.chat.grpc.User toProtoUser(User user)
    {
        return com.example.chat.grpc.User.newBuilder()
            .setId(user.getId())
            .setUsername(user.getUsername())
            .setCreatedAtUnix(user.getCreatedAt() != null ? user.getCreatedAt().getEpochSecond() : Instant.now().getEpochSecond())
            .build();
    }
}

