using System.Net.Http.Json;
using BlazorClient.Shared;
using System.Net;

namespace BlazorClient.Services;

public class AuthService
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly CustomAuthStateProvider _authStateProvider;

    public AuthService(IHttpClientFactory httpClientFactory, CustomAuthStateProvider authStateProvider)
    {
        _httpClientFactory = httpClientFactory;
        _authStateProvider = authStateProvider;
    }

    private HttpClient GetUnauthenticatedClient() => _httpClientFactory.CreateClient("UnauthenticatedClient");

    public async Task<bool> RegisterAsync(RegisterRequest request)
    {
        var httpClient = GetUnauthenticatedClient();
        var baseUrl = httpClient.BaseAddress?.ToString() ?? "unknown";
        
        try
        {
            Console.WriteLine($"Registering user: {request.Username}");
            Console.WriteLine($"API Base URL: {baseUrl}");
            Console.WriteLine($"Full URL: {baseUrl}api/auth/register");
            
            var response = await httpClient.PostAsJsonAsync("api/auth/register", request);
            Console.WriteLine($"Register response status: {response.StatusCode}");
            
            if (!response.IsSuccessStatusCode)
            {
                var errorContent = await response.Content.ReadAsStringAsync();
                Console.WriteLine($"Register failed: {response.StatusCode} - {errorContent}");
                throw new Exception($"Registration failed: {response.StatusCode}. {errorContent}");
            }

            var payload = await response.Content.ReadFromJsonAsync<AuthResponse>();
            if (payload == null)
            {
                Console.WriteLine("Register response was null");
                throw new Exception("Server returned an invalid response.");
            }

            await _authStateProvider.SignInAsync(payload);
            return true;
        }
        catch (HttpRequestException ex)
        {
            Console.WriteLine($"Register HTTP exception: {ex.Message}");
            Console.WriteLine($"Inner exception: {ex.InnerException?.Message}");
            Console.WriteLine($"Stack trace: {ex.StackTrace}");
            throw new Exception($"Cannot connect to the server at {baseUrl}. Please ensure the C# Web API is running and accessible. Error: {ex.Message}");
        }
        catch (TaskCanceledException ex)
        {
            Console.WriteLine($"Register timeout: {ex.Message}");
            throw new Exception("Request timed out. The server may not be responding.");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Register exception: {ex.Message}");
            throw;
        }
    }

    public async Task<bool> LoginAsync(LoginRequest request)
    {
        var httpClient = GetUnauthenticatedClient();
        var baseUrl = httpClient.BaseAddress?.ToString() ?? "unknown";
        
        try
        {
            Console.WriteLine($"Logging in user: {request.Username}");
            Console.WriteLine($"API Base URL: {baseUrl}");
            Console.WriteLine($"Full URL: {baseUrl}api/auth/login");
            
            var response = await httpClient.PostAsJsonAsync("api/auth/login", request);
            Console.WriteLine($"Login response status: {response.StatusCode}");
            
            if (!response.IsSuccessStatusCode)
            {
                var errorContent = await response.Content.ReadAsStringAsync();
                Console.WriteLine($"Login failed: {response.StatusCode} - {errorContent}");
                throw new Exception($"Login failed: {response.StatusCode}. {errorContent}");
            }

            var payload = await response.Content.ReadFromJsonAsync<AuthResponse>();
            if (payload == null)
            {
                Console.WriteLine("Login response was null");
                throw new Exception("Server returned an invalid response.");
            }

            await _authStateProvider.SignInAsync(payload);
            return true;
        }
        catch (HttpRequestException ex)
        {
            Console.WriteLine($"Login HTTP exception: {ex.Message}");
            Console.WriteLine($"Inner exception: {ex.InnerException?.Message}");
            Console.WriteLine($"Stack trace: {ex.StackTrace}");
            throw new Exception($"Cannot connect to the server at {baseUrl}. Please ensure the C# Web API is running and accessible. Error: {ex.Message}");
        }
        catch (TaskCanceledException ex)
        {
            Console.WriteLine($"Login timeout: {ex.Message}");
            throw new Exception("Request timed out. The server may not be responding.");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Login exception: {ex.Message}");
            throw;
        }
    }

    public async Task LogoutAsync()
    {
        await _authStateProvider.SignOutAsync();
    }
}

