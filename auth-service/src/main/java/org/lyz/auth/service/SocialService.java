package org.lyz.auth.service;

import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;

public interface SocialService {
    String getAuthorizationUrl(String platform);
    AuthResponse<?> callback(String platform, AuthCallback callback);
}
