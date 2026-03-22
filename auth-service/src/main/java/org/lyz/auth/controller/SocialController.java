package org.lyz.auth.controller;

import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import org.lyz.auth.service.SocialService;
import org.lyz.common.core.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @GetMapping("/{platform}/authorize")
    public Result<String> getAuthorizationUrl(@PathVariable String platform) {
        String url = socialService.getAuthorizationUrl(platform);
        return Result.success(url);
    }

    @GetMapping("/{platform}/callback")
    public Result<AuthResponse<?>> callback(
            @PathVariable String platform,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        AuthResponse<?> response = socialService.callback(platform, callback);
        return Result.success(response);
    }
}
