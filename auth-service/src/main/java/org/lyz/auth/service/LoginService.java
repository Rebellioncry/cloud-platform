package org.lyz.auth.service;

import org.lyz.auth.dto.CodeLoginRequest;
import org.lyz.auth.dto.CodeRequest;
import org.lyz.auth.dto.LoginRequest;
import org.lyz.auth.dto.LoginResponse;
import org.lyz.auth.dto.UserInfo;

public interface LoginService {
    LoginResponse login(LoginRequest request);
    void sendCode(CodeRequest request);
    LoginResponse codeLogin(CodeLoginRequest request);
    void logout();
    UserInfo getUserInfo();
}
