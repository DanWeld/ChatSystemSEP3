using BlazorClient.Shared;
using Microsoft.AspNetCore.SignalR.Client;
using Microsoft.Extensions.Configuration;

namespace BlazorClient.Services;

public class ChatClientService : IAsyncDisposable
{
    private readonly TokenStorageService _tokenStorage;
    private readonly string _hubUrl;
    private HubConnection? _connection;

    public event Action<MessageDto>? OnMessageReceived;
    public event Action<MessageDto>? OnMessageEdited;
    public event Action<MessageDto>? OnMessageDeleted;

    public ChatClientService(IConfiguration configuration, TokenStorageService tokenStorage)
    {
        _tokenStorage = tokenStorage;
        var apiBaseUrl = configuration["ApiBaseUrl"] ?? "http://localhost:5000/";
        if (!apiBaseUrl.EndsWith("/"))
        {
            apiBaseUrl += "/";
        }
        _hubUrl = $"{apiBaseUrl}chathub";
    }

    public async Task StartAsync()
    {
        if (_connection != null && _connection.State == HubConnectionState.Connected)
            return;

        if (_connection != null)
        {
            await _connection.DisposeAsync();
        }

        _connection = new HubConnectionBuilder()
            .WithUrl(_hubUrl, options =>
            {
                options.AccessTokenProvider = () => _tokenStorage.GetTokenAsync();
            })
            .WithAutomaticReconnect()
            .Build();

        _connection.On<MessageDto>("ReceiveMessage", message =>
        {
            OnMessageReceived?.Invoke(message);
        });
        _connection.On<MessageDto>("MessageEdited", message =>
        {
            OnMessageEdited?.Invoke(message);
        });
        _connection.On<MessageDto>("MessageDeleted", message =>
        {
            OnMessageDeleted?.Invoke(message);
        });

        _connection.Closed += (error) =>
        {
            Console.WriteLine($"SignalR connection closed: {error?.Message}");
            return Task.CompletedTask;
        };

        try
        {
            await _connection.StartAsync();
            Console.WriteLine("SignalR connection started successfully");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error starting SignalR connection: {ex.Message}");
            throw;
        }
    }

    public bool IsConnected => _connection?.State == HubConnectionState.Connected;

    public async Task JoinChat(int chatRoomId)
    {
        if (_connection == null) 
        {
            await StartAsync();
        }
        await _connection!.InvokeAsync("JoinChat", chatRoomId);
    }

    public async Task LeaveChat(int chatRoomId)
    {
        if (_connection == null) return;
        await _connection.InvokeAsync("LeaveChat", chatRoomId);
    }

    public async Task SendMessage(int chatRoomId, string text)
    {
        if (_connection == null) await StartAsync();
        await _connection!.InvokeAsync("SendMessage", chatRoomId, text);
    }

    public async ValueTask DisposeAsync()
    {
        if (_connection != null)
        {
            await _connection.DisposeAsync();
        }
    }
}
