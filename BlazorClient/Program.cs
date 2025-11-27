using BlazorClient;
using BlazorClient.Services;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using Microsoft.Extensions.DependencyInjection;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");

builder.Services.AddOptions();
builder.Services.AddAuthorizationCore();

builder.Services.AddScoped<TokenStorageService>();
builder.Services.AddScoped<AuthMessageHandler>();
builder.Services.AddScoped<CustomAuthStateProvider>();
builder.Services.AddScoped<AuthenticationStateProvider>(sp => sp.GetRequiredService<CustomAuthStateProvider>());
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<ChatClientService>();
builder.Services.AddScoped<FriendApiService>();
builder.Services.AddSingleton<ToastService>();

var apiBaseUrl = builder.Configuration["ApiBaseUrl"] ?? "http://localhost:5000/";
if (!apiBaseUrl.EndsWith("/"))
{
    apiBaseUrl += "/";
}

// Unauthenticated client for auth endpoints
builder.Services.AddHttpClient("UnauthenticatedClient", client =>
{
    client.BaseAddress = new Uri(apiBaseUrl);
});

// Authenticated client for protected endpoints
builder.Services.AddHttpClient("AuthorizedClient", client =>
{
    client.BaseAddress = new Uri(apiBaseUrl);
}).AddHttpMessageHandler<AuthMessageHandler>();

// Default HttpClient (for AuthService - uses unauthenticated)
builder.Services.AddScoped(sp => sp.GetRequiredService<IHttpClientFactory>()
    .CreateClient("UnauthenticatedClient"));

// Authorized HttpClient (for other services)
builder.Services.AddScoped<HttpClient>(sp => sp.GetRequiredService<IHttpClientFactory>()
    .CreateClient("AuthorizedClient"));

await builder.Build().RunAsync();
