using System.Text;
using Chat.Grpc;
using CSharpWebAPI.Hubs;
using CSharpWebAPI.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddSignalR();
builder.Services.AddAuthorization();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure gRPC clients - gRPC uses HTTP/2 by default
// For .NET gRPC clients, HTTP/2 is automatically used
builder.Services.AddGrpcClient<ChatService.ChatServiceClient>(o =>
{
    o.Address = new Uri("http://localhost:6565");
});

builder.Services.AddGrpcClient<UserService.UserServiceClient>(o =>
{
    o.Address = new Uri("http://localhost:6565");
});

builder.Services.AddGrpcClient<FriendService.FriendServiceClient>(o =>
{
    o.Address = new Uri("http://localhost:6565");
});

builder.Services.AddGrpcClient<Chat.Grpc.GroupChatService.GroupChatServiceClient>(o =>
{
    o.Address = new Uri("http://localhost:6565");
});

builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddSingleton<JwtTokenService>();
builder.Services.AddScoped<IFriendService, FriendshipService>();
builder.Services.AddScoped<IGroupChatService, CSharpWebAPI.Services.GroupChatService>();

var jwtSection = builder.Configuration.GetSection("Jwt");
var signingKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSection["Key"]!));

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
}).AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = jwtSection["Issuer"],
        ValidAudience = jwtSection["Audience"],
        IssuerSigningKey = signingKey
    };

    options.Events = new JwtBearerEvents
    {
        OnMessageReceived = context =>
        {
            var accessToken = context.Request.Query["access_token"];
            var path = context.HttpContext.Request.Path;
            if (!string.IsNullOrEmpty(accessToken) && path.StartsWithSegments("/chathub"))
            {
                context.Token = accessToken;
            }
            return Task.CompletedTask;
        }
    };
});

builder.Services.AddCors(o =>
{
    o.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials()
              .SetIsOriginAllowed(origin => 
              {
                  // For development: allow all origins
                  // In production, restrict to specific origins
                  if (string.IsNullOrEmpty(origin))
                      return false;
                  
                  try
                  {
                      var uri = new Uri(origin);
                      // Allow localhost, 127.0.0.1, and common development IPs
                      return uri.Host == "localhost" 
                          || uri.Host == "127.0.0.1" 
                          || uri.Host == "10.154.216.58"
                          || uri.Host.StartsWith("192.168.")
                          || uri.Host.StartsWith("10.")
                          || uri.Host.StartsWith("172.");
                  }
                  catch
                  {
                      return false;
                  }
              });
    });
});

var app = builder.Build();

app.UseCors();
app.UseSwagger();
app.UseSwaggerUI();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.MapHub<ChatHub>("/chathub");

// Add a simple root endpoint for testing
app.MapGet("/", () => "Chat API is running! Use /api/Chat/{chatId}/messages to get messages.");

// Add a test endpoint to verify routing
app.MapGet("/api/test", () => "API routing is working!");

// Health check endpoint to test Java gRPC server connection
app.MapGet("/api/health/grpc", async (UserService.UserServiceClient userClient) =>
{
    try
    {
        // Try to call a simple gRPC method to verify connection
        var response = await userClient.GetUserAsync(new Chat.Grpc.GetUserRequest { UserId = 999999 });
        return Results.Ok(new { status = "connected", message = "gRPC server is reachable" });
    }
    catch (Grpc.Core.RpcException ex)
    {
        if (ex.StatusCode == Grpc.Core.StatusCode.NotFound)
        {
            // Server is reachable but user doesn't exist - that's fine
            return Results.Ok(new { status = "connected", message = "gRPC server is reachable (user not found is expected)" });
        }
        return Results.Problem(
            detail: $"gRPC error: {ex.StatusCode} - {ex.Status.Detail}",
            statusCode: 503
        );
    }
    catch (Exception ex)
    {
        return Results.Problem(
            detail: $"Cannot connect to gRPC server on localhost:6565. Error: {ex.Message}. Make sure the Java gRPC server is running.",
            statusCode: 503
        );
    }
});

app.Run();
