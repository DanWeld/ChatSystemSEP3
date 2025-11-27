using System.Security.Claims;
using CSharpWebAPI.ApiContracts;
using CSharpWebAPI.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Grpc.Core;

namespace CSharpWebAPI.Controllers;

[ApiController]
[Authorize]
[Route("api/[controller]")]
public class FriendsController : ControllerBase
{
    private readonly IFriendService _friendService;

    public FriendsController(IFriendService friendService)
    {
        _friendService = friendService;
    }

    [HttpGet]
    public async Task<IReadOnlyList<FriendDto>> GetFriends()
    {
        var userId = GetUserId();
        return await _friendService.GetFriendsAsync(userId);
    }

    [HttpGet("requests")]
    public async Task<IReadOnlyList<FriendRequestDto>> GetRequests()
    {
        var userId = GetUserId();
        return await _friendService.GetIncomingAsync(userId);
    }

    [HttpPost("requests")]
    public async Task<ActionResult<FriendRequestDto>> SendRequest([FromBody] SendFriendRequestModel model)
    {
        if (!ModelState.IsValid) return ValidationProblem(ModelState);

        var userId = GetUserId();
        try
        {
            var dto = await _friendService.SendRequestAsync(userId, model.Username);
            return Ok(dto);
        }
        catch (RpcException ex)
        {
            return BadRequest(new { message = ex.Status.Detail });
        }
    }

    [HttpPost("requests/{id:int}")]
    public async Task<ActionResult<FriendRequestDto>> RespondRequest(int id, [FromBody] RespondFriendRequestModel model)
    {
        if (!ModelState.IsValid) return ValidationProblem(ModelState);

        try
        {
            var dto = await _friendService.RespondAsync(id, model.Accept);
            return Ok(dto);
        }
        catch (RpcException ex)
        {
            return BadRequest(new { message = ex.Status.Detail });
        }
    }

    [HttpDelete("{friendId:int}")]
    public async Task<IActionResult> RemoveFriend(int friendId)
    {
        var userId = GetUserId();
        await _friendService.RemoveFriendAsync(userId, friendId);
        return NoContent();
    }

    private int GetUserId()
    {
        var value = User.FindFirstValue(ClaimTypes.NameIdentifier);
        return int.TryParse(value, out var id) ? id : 0;
    }
}

