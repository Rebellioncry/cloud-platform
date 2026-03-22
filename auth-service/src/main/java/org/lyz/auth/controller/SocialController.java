package org.lyz.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import org.lyz.auth.service.SocialService;
import org.lyz.common.core.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
@Tag(name = "社交登录", description = "第三方社交登录接口")
public class SocialController {

    private final SocialService socialService;

    @Operation(summary = "获取授权地址", description = "获取第三方平台授权URL")
    @GetMapping("/{platform}/authorize")
    public Result<String> getAuthorizationUrl(
            @Parameter(description = "平台标识") @PathVariable String platform) {
        String url = socialService.getAuthorizationUrl(platform);
        return Result.success(url);
    }

    @Operation(summary = "授权回调", description = "第三方平台授权回调处理")
    @GetMapping("/{platform}/callback")
    public Result<AuthResponse<?>> callback(
            @Parameter(description = "平台标识") @PathVariable String platform,
            @Parameter(description = "授权码") @RequestParam String code,
            @Parameter(description = "状态参数") @RequestParam(required = false) String state) {
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        AuthResponse<?> response = socialService.callback(platform, callback);
        return Result.success(response);
    }
}
