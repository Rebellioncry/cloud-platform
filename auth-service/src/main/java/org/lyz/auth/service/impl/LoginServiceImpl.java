package org.lyz.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.auth.dto.CodeLoginRequest;
import org.lyz.auth.dto.CodeRequest;
import org.lyz.auth.dto.LoginRequest;
import org.lyz.auth.dto.LoginResponse;
import org.lyz.auth.dto.MenuTree;
import org.lyz.auth.dto.UserInfo;
import org.lyz.auth.entity.SysMenu;
import org.lyz.auth.entity.SysRoleMenu;
import org.lyz.auth.mapper.SysMenuMapper;
import org.lyz.auth.mapper.SysRoleMenuMapper;
import org.lyz.auth.mapper.SysUserMapper;
import org.lyz.auth.service.LoginService;
import org.lyz.auth.service.VerificationService;
import org.lyz.common.core.entity.SysUser;
import org.lyz.common.core.constant.SecurityConstants;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final SysUserMapper sysUserMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final VerificationService verificationService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String tenantId = request.getTenantId();

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername())
               .eq(SysUser::getStatus, 1);

        if (tenantId != null && !tenantId.isEmpty()) {
            wrapper.eq(SysUser::getTenantId, tenantId);
        }
        
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null && (tenantId == null || tenantId.isEmpty())) {
            wrapper.clear();
            wrapper.eq(SysUser::getUsername, request.getUsername())
                   .eq(SysUser::getStatus, 1);
            user = sysUserMapper.selectOne(wrapper);
        }

        if (user == null) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        String resolvedTenantId = user.getTenantId();
        if (resolvedTenantId == null || resolvedTenantId.isEmpty()) {
            resolvedTenantId = "1";
        }
        TenantContext.setTenantId(resolvedTenantId);

        if (!bcryptCheck(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        long expireTime = System.currentTimeMillis() + SecurityConstants.JWT_EXPIRATION;

        writeSessionData(user.getId());

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
    public void sendCode(CodeRequest request) {
        String target = verificationService.resolveTarget(request.getType(), request.getMobile(), request.getEmail());
        verificationService.sendCode(request.getType(), target);
    }

    @Override
    public LoginResponse codeLogin(CodeLoginRequest request) {
        String tenantId = request.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "1";
        }
        TenantContext.setTenantId(tenantId);

        String target = verificationService.resolveTarget(request.getType(), request.getMobile(), request.getEmail());
        verificationService.verifyCode(request.getType(), target, request.getCode());

        SysUser user = findUserByTarget(request.getType(), target, tenantId);

        if (user == null) {
            user = createAutoUser(request.getType(), target, tenantId);
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        long expireTime = System.currentTimeMillis() + SecurityConstants.JWT_EXPIRATION;

        writeSessionData(user.getId());

        log.info("验证码登录成功: {}", user.getUsername());

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
    public UserInfo getUserInfo() {
        String userId = StpUtil.getLoginIdAsString();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);

        return UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .roles(roleCodes)
                .menus(getUserMenus())
                .build();
    }

    private List<MenuTree> getUserMenus() {
        String userId = StpUtil.getLoginIdAsString();

        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);
        boolean superAdmin = roleCodes != null && roleCodes.contains("SUPER_ADMIN");

        if (superAdmin) {
            List<SysMenu> menus = sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getStatus, 1)
                            .orderByAsc(SysMenu::getOrderNum));
            return buildMenuTree(menus, "0");
        }

        List<String> menuIds = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .select(SysMenu::getId)
                        .inSql(SysMenu::getId,
                                "SELECT menu_id FROM sys_role_menu WHERE role_id IN (" +
                                        "SELECT role_id FROM sys_user_role WHERE user_id = '" + userId + "')")
        ).stream().map(SysMenu::getId).collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysMenu> menus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
                        .in(SysMenu::getId, menuIds)
                        .orderByAsc(SysMenu::getOrderNum)
        );

        return buildMenuTree(menus, "0");
    }

    private List<MenuTree> buildMenuTree(List<SysMenu> allMenus, String parentId) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> MenuTree.builder()
                        .id(m.getId())
                        .parentId(m.getParentId())
                        .menuName(m.getMenuName())
                        .path(m.getPath())
                        .component(m.getComponent())
                        .icon(m.getIcon())
                        .perms(m.getPerms())
                        .menuType(m.getMenuType())
                        .children(buildMenuTree(allMenus, m.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private boolean bcryptCheck(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    private void writeSessionData(String userId) {
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);
        SysUser user = sysUserMapper.selectById(userId);
        StpUtil.getSession().set("roleCodes", roleCodes);
        if (user != null) {
            StpUtil.getSession().set("userId", userId);
            StpUtil.getSession().set("username", user.getUsername());
            StpUtil.getSession().set("tenantId", user.getTenantId());
        }
    }

    private SysUser findUserByTarget(String type, String target, String tenantId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getTenantId, tenantId);
        if ("sms".equalsIgnoreCase(type)) {
            wrapper.eq(SysUser::getMobile, target);
        } else if ("email".equalsIgnoreCase(type)) {
            wrapper.eq(SysUser::getEmail, target);
        }
        return sysUserMapper.selectOne(wrapper);
    }

    private SysUser createAutoUser(String type, String target, String tenantId) {
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setStatus(1);
        if ("sms".equalsIgnoreCase(type)) {
            user.setUsername(target);
            user.setMobile(target);
            user.setNickname("手机用户");
        } else {
            user.setUsername(target);
            user.setEmail(target);
            user.setNickname("邮箱用户");
        }
        sysUserMapper.insert(user);
        log.info("验证码登录自动创建用户: {}", user.getUsername());
        return user;
    }
}
