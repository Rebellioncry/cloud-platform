package org.lyz.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lyz.auth.dto.LoginRequest;
import org.lyz.auth.dto.LoginResponse;
import org.lyz.auth.dto.UserInfo;
import org.lyz.auth.service.LoginService;
import org.lyz.common.core.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录、登出、获取用户信息接口")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Operation(summary = "用户登录", description = "使用用户名密码登录系统，返回Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(loginService.login(request));
    }

    @Operation(summary = "用户登出", description = "退出当前登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        loginService.logout();
        return Result.success();
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的基本信息和权限菜单")
    @GetMapping("/userinfo")
    public Result<UserInfo> getUserInfo() {
        return Result.success(loginService.getUserInfo());
    }
}
