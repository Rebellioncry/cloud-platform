package org.lyz.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.lyz.auth.dto.LoginRequest;
import org.lyz.auth.dto.LoginResponse;
import org.lyz.auth.dto.MenuTree;
import org.lyz.auth.dto.UserInfo;
import org.lyz.common.core.entity.SysUser;
import org.lyz.auth.mapper.SysUserMapper;
import org.lyz.auth.service.LoginService;
import org.lyz.common.core.constant.SecurityConstants;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    private final SysUserMapper sysUserMapper;

    public LoginServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String tenantId = request.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "1";
        }
        TenantContext.setTenantId(tenantId);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername())
               .eq(SysUser::getTenantId, Long.parseLong(tenantId))
               .eq(SysUser::getStatus, 1);
        
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        if (!bcryptCheck(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        long expireTime = System.currentTimeMillis() + SecurityConstants.JWT_EXPIRATION;

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .expireTime(expireTime)
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
        TenantContext.clear();
    }

    @Override
    public UserInfo getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .roles(Collections.singletonList("admin"))
                .menus(getUserMenus())
                .build();
    }

    private List<MenuTree> getUserMenus() {
        return Collections.singletonList(MenuTree.builder()
                .id(1L)
                .menuName("首页")
                .path("/dashboard")
                .component("/dashboard/index")
                .icon("HomeFilled")
                .menuType(1)
                .build());
    }

    private boolean bcryptCheck(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }
}
