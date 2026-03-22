package org.lyz.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.auth.dto.LoginRequest;
import org.lyz.auth.dto.LoginResponse;
import org.lyz.auth.dto.UserInfo;
import org.lyz.auth.service.LoginService;
import org.lyz.common.core.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(loginService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        loginService.logout();
        return Result.success();
    }

    @GetMapping("/userinfo")
    public Result<UserInfo> getUserInfo() {
        return Result.success(loginService.getUserInfo());
    }
}
