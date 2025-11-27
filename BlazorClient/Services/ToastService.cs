using Microsoft.AspNetCore.Components;

namespace BlazorClient.Services;

public class ToastService
{
    public event Action<string, string>? OnShow;

    public void ShowSuccess(string message)
    {
        OnShow?.Invoke("success", message);
    }

    public void ShowError(string message)
    {
        OnShow?.Invoke("error", message);
    }

    public void ShowInfo(string message)
    {
        OnShow?.Invoke("info", message);
    }
}

