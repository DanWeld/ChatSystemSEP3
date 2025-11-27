using Chat.Grpc;
using CSharpWebAPI.ApiContracts;
using Grpc.Core;
using ApiLoginRequest = CSharpWebAPI.ApiContracts.LoginRequest;
using ApiRegisterRequest = CSharpWebAPI.ApiContracts.RegisterRequest;

namespace CSharpWebAPI.Services;

public class AuthService : IAuthService
{
    private readonly UserService.UserServiceClient _userClient;
    private readonly JwtTokenService _jwtTokenService;

    public AuthService(UserService.UserServiceClient userClient, JwtTokenService jwtTokenService)
    {
        _userClient = userClient;
        _jwtTokenService = jwtTokenService;
    }

    public async Task<AuthResponse> RegisterAsync(ApiRegisterRequest request)
    {
        try
        {
            var grpcResponse = await _userClient.RegisterUserAsync(new RegisterUserRequest
            {
                Username = request.Username,
                Password = request.Password
            });

            return BuildAuthResponse(grpcResponse.User);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.AlreadyExists)
        {
            throw new InvalidOperationException("Username is already taken.");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.Unavailable)
        {
            throw new InvalidOperationException("Unable to connect to the server. Please ensure the Java gRPC server is running on port 6565.");
        }
        catch (RpcException ex)
        {
            throw new InvalidOperationException($"Registration failed: {ex.Status.Detail}");
        }
    }

    public async Task<AuthResponse> LoginAsync(ApiLoginRequest request)
    {
        try
        {
            var grpcResponse = await _userClient.LoginAsync(new Chat.Grpc.LoginRequest
            {
                Username = request.Username,
                Password = request.Password
            });

            return BuildAuthResponse(grpcResponse.User);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.Unauthenticated)
        {
            throw new InvalidOperationException("Invalid username or password.");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.Unavailable)
        {
            throw new InvalidOperationException("Unable to connect to the server. Please ensure the Java gRPC server is running on port 6565.");
        }
        catch (RpcException ex)
        {
            throw new InvalidOperationException($"Login failed: {ex.Status.Detail}");
        }
    }

    public async Task<UserDto?> GetUserByIdAsync(int userId)
    {
        try
        {
            var response = await _userClient.GetUserAsync(new GetUserRequest { UserId = userId });
            return MapUser(response.User);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            return null;
        }
    }

    private AuthResponse BuildAuthResponse(User user)
    {
        var dto = MapUser(user);
        var token = _jwtTokenService.GenerateToken(dto);

        return new AuthResponse
        {
            AccessToken = token,
            User = dto
        };
    }

    private static UserDto MapUser(User user)
    {
        return new UserDto
        {
            Id = user.Id,
            Username = user.Username
        };
    }
}

