package org.lyz.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthDingTalkRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.lyz.auth.entity.SysSocial;
import org.lyz.common.core.entity.SysUser;
import org.lyz.auth.mapper.SysSocialMapper;
import org.lyz.auth.mapper.SysUserMapper;
import org.lyz.auth.service.SocialService;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SocialServiceImpl implements SocialService {

    private final StringRedisTemplate redisTemplate;
    private final SysUserMapper sysUserMapper;
    private final SysSocialMapper sysSocialMapper;
    private final Map<String, AuthRequest> authRequestMap = new HashMap<>();

    public SocialServiceImpl(StringRedisTemplate redisTemplate, SysUserMapper sysUserMapper, SysSocialMapper sysSocialMapper) {
        this.redisTemplate = redisTemplate;
        this.sysUserMapper = sysUserMapper;
        this.sysSocialMapper = sysSocialMapper;
    }

    @Value("${justauth.clients.wetchat.client-id:}")
    private String wechatClientId;
    @Value("${justauth.clients.wetchat.client-secret:}")
    private String wechatClientSecret;
    @Value("${justauth.clients.wetchat.redirect-uri:}")
    private String wechatRedirectUri;

    @Value("${justauth.clients.qq.client-id:}")
    private String qqClientId;
    @Value("${justauth.clients.qq.client-secret:}")
    private String qqClientSecret;
    @Value("${justauth.clients.qq.redirect-uri:}")
    private String qqRedirectUri;

    @Value("${justauth.clients.dingtalk.client-id:}")
    private String dingtalkClientId;
    @Value("${justauth.clients.dingtalk.client-secret:}")
    private String dingtalkClientSecret;
    @Value("${justauth.clients.dingtalk.redirect-uri:}")
    private String dingtalkRedirectUri;

    @PostConstruct
    public void init() {
        if (wechatClientId != null && !wechatClientId.isEmpty()) {
            AuthConfig wechatConfig = AuthConfig.builder()
                    .clientId(wechatClientId)
                    .clientSecret(wechatClientSecret)
                    .redirectUri(wechatRedirectUri)
                    .build();
            authRequestMap.put("WECHAT", new AuthWeChatOpenRequest(wechatConfig));
        }

        if (qqClientId != null && !qqClientId.isEmpty()) {
            AuthConfig qqConfig = AuthConfig.builder()
                    .clientId(qqClientId)
                    .clientSecret(qqClientSecret)
                    .redirectUri(qqRedirectUri)
                    .build();
            authRequestMap.put("QQ", new AuthQqRequest(qqConfig));
        }

        if (dingtalkClientId != null && !dingtalkClientId.isEmpty()) {
            AuthConfig dingtalkConfig = AuthConfig.builder()
                    .clientId(dingtalkClientId)
                    .clientSecret(dingtalkClientSecret)
                    .redirectUri(dingtalkRedirectUri)
                    .build();
            authRequestMap.put("DINGTALK", new AuthDingTalkRequest(dingtalkConfig));
        }
    }

    @Override
    public String getAuthorizationUrl(String platform) {
        AuthRequest authRequest = authRequestMap.get(platform.toUpperCase());
        if (authRequest == null) {
            throw new BusinessException("不支持的登录方式: " + platform);
        }
        String state = AuthStateUtils.createState();
        String key = "social:state:" + state;
        redisTemplate.opsForValue().set(key, platform, Duration.ofMinutes(5));
        return authRequest.authorize(state);
    }

    @Override
    public AuthResponse<?> callback(String platform, AuthCallback callback) {
        String state = callback.getState();
        String key = "social:state:" + state;
        String cachedPlatform = redisTemplate.opsForValue().get(key);
        
        if (cachedPlatform == null) {
            throw new BusinessException("state已过期，请重新发起授权");
        }
        
        AuthRequest authRequest = authRequestMap.get(cachedPlatform.toUpperCase());
        if (authRequest == null) {
            throw new BusinessException("不支持的登录方式");
        }

        AuthResponse<AuthUser> response = authRequest.login(callback);
        
        if (response.ok()) {
            AuthUser authUser = response.getData();
            processSocialLogin(authUser, cachedPlatform);
        }
        
        redisTemplate.delete(key);
        return response;
    }

    private void processSocialLogin(AuthUser authUser, String platform) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = "1";
        }

        LambdaQueryWrapper<SysSocial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSocial::getPlatform, platform)
               .eq(SysSocial::getOpenid, authUser.getUuid())
               .eq(SysSocial::getTenantId, Long.parseLong(tenantId));
        
        SysSocial social = sysSocialMapper.selectOne(wrapper);
        
        if (social != null) {
            SysUser user = sysUserMapper.selectById(social.getUserId());
            if (user != null && user.getStatus() == 1) {
                StpUtil.login(user.getId());
                log.info("第三方登录成功(绑定用户): {}", authUser.getUsername());
                return;
            }
        }

        SysUser newUser = SysUser.builder()
                .tenantId(Long.parseLong(tenantId))
                .username(authUser.getUsername())
                .nickname(authUser.getNickname())
                .avatar(authUser.getAvatar())
                .status(1)
                .build();
        sysUserMapper.insert(newUser);

        SysSocial newSocial = SysSocial.builder()
                .userId(newUser.getId())
                .tenantId(Long.parseLong(tenantId))
                .platform(platform.toLowerCase())
                .openid(authUser.getUuid())
                .nickname(authUser.getNickname())
                .avatar(authUser.getAvatar())
                .build();
        sysSocialMapper.insert(newSocial);

        StpUtil.login(newUser.getId());
        log.info("第三方登录成功(新用户): {}", authUser.getUsername());
    }
}
