package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.crypto.digest.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import org.lyz.common.core.context.TenantConstants;
import org.lyz.common.core.entity.SysUser;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.system.dto.TenantDTO;
import org.lyz.system.dto.UserDTO;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.entity.SysTenantPackage;
import org.lyz.system.entity.SysRole;
import org.lyz.system.entity.SysRoleMenu;
import org.lyz.system.entity.SysUserRole;
import org.lyz.system.dao.SysTenantDao;
import org.lyz.system.dao.SysTenantPackageDao;
import org.lyz.system.dao.SysUserDao;
import org.lyz.system.dao.SysRoleDao;
import org.lyz.system.dao.SysRoleMenuDao;
import org.lyz.system.dao.SysUserRoleDao;
import org.lyz.system.service.PlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService {

    private final SysTenantDao tenantDao;
    private final SysTenantPackageDao packageDao;
    private final SysUserDao userDao;
    private final SysRoleDao roleDao;
    private final SysRoleMenuDao roleMenuDao;
    private final SysUserRoleDao userRoleDao;

    @Override
    public PageResult<Map<String, Object>> listTenants(int page, int size) {
        Page<SysTenant> pageParam = new Page<>(page, size);
        IPage<SysTenant> result = tenantDao.page(pageParam, new LambdaQueryWrapper<SysTenant>().orderByDesc(SysTenant::getCreateTime));

        List<Map<String, Object>> records = result.getRecords().stream().map(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("tenantCode", t.getTenantCode());
            map.put("tenantName", t.getTenantName());
            map.put("contact", t.getContact());
            map.put("mobile", t.getMobile());
            map.put("email", t.getEmail());
            map.put("packageId", t.getPackageId());
            map.put("status", t.getStatus());
            map.put("expireTime", t.getExpireTime());
            map.put("remark", t.getRemark());
            map.put("createTime", t.getCreateTime());

            long userCount = userDao.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getTenantId, t.getId()));
            map.put("userCount", userCount);

            return map;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public Map<String, Object> getTenantDetail(String id) {
        SysTenant tenant = tenantDao.getById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", tenant.getId());
        detail.put("tenantCode", tenant.getTenantCode());
        detail.put("tenantName", tenant.getTenantName());
        detail.put("contact", tenant.getContact());
        detail.put("mobile", tenant.getMobile());
        detail.put("email", tenant.getEmail());
        detail.put("packageId", tenant.getPackageId());
        detail.put("status", tenant.getStatus());
        detail.put("expireTime", tenant.getExpireTime());
        detail.put("remark", tenant.getRemark());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTenantWithAdmin(TenantDTO tenantDto, UserDTO adminDto) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantCode, tenantDto.getTenantCode());
        if (tenantDao.count(wrapper) > 0) {
            throw new BusinessException("租户编码已存在");
        }

        SysTenant tenant = new SysTenant();
        tenant.setTenantCode(tenantDto.getTenantCode());
        tenant.setTenantName(tenantDto.getTenantName());
        tenant.setContact(tenantDto.getContact());
        tenant.setMobile(tenantDto.getMobile());
        tenant.setEmail(tenantDto.getEmail());
        tenant.setPackageId(tenantDto.getPackageId());
        tenant.setStatus(tenantDto.getStatus() != null ? tenantDto.getStatus() : 1);
        tenant.setExpireTime(tenantDto.getExpireTime());
        tenant.setRemark(tenantDto.getRemark());
        tenantDao.save(tenant);

        String roleId = createTenantAdminRole(tenant.getId(), tenantDto.getPackageId());
        createTenantAdminUser(tenant.getId(), adminDto, roleId);

        log.info("创建租户及其管理员: tenant={}, admin={}, package={}", tenant.getTenantCode(), adminDto.getUsername(), tenantDto.getPackageId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTenantAdmin(String tenantId, UserDTO adminDto) {
        SysTenant tenant = tenantDao.getById(tenantId);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        SysRole adminRole = roleDao.getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, TenantConstants.TENANT_ADMIN_ROLE_KEY)
                .eq(SysRole::getTenantId, tenantId)
                .last("LIMIT 1"));
        String roleId = adminRole != null ? adminRole.getId() : null;

        createTenantAdminUser(tenantId, adminDto, roleId);
        log.info("为租户 {} 创建管理员: {}", tenant.getTenantCode(), adminDto.getUsername());
    }

    @Override
    public void updateTenantStatus(String tenantId, Integer status) {
        SysTenant tenant = tenantDao.getById(tenantId);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        tenant.setStatus(status);
        tenantDao.updateById(tenant);
    }

    @Override
    public Map<String, Object> impersonateTenant(String tenantId) {
        SysTenant tenant = tenantDao.getById(tenantId);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }

        SysUser admin = userDao.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getStatus, 1)
                .last("LIMIT 1"));
        if (admin == null) {
            throw new BusinessException("该租户暂无管理员账号，请先创建");
        }

        StpUtil.login(admin.getId());
        String impersonateToken = StpUtil.getTokenValue();
        StpUtil.getSession().set("userId", admin.getId());
        StpUtil.getSession().set("username", admin.getUsername());
        StpUtil.getSession().set("nickname", admin.getNickname());
        StpUtil.getSession().set("tenantId", admin.getTenantId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("impersonateToken", impersonateToken);
        result.put("userId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        result.put("tenantId", admin.getTenantId());
        result.put("tenantName", tenant.getTenantName());

        log.info("平台管理员模拟登录: 目标租户={}, 目标用户={}", tenant.getTenantCode(), admin.getUsername());
        return result;
    }

    @Override
    public void returnFromImpersonate() {
        StpUtil.logout();
        log.info("平台管理员退出模拟登录");
    }

    private String createTenantAdminRole(String tenantId, String packageId) {
        SysRole adminRole = new SysRole();
        adminRole.setTenantId(tenantId);
        adminRole.setRoleCode(TenantConstants.TENANT_ADMIN_ROLE_KEY);
        adminRole.setRoleName(TenantConstants.TENANT_ADMIN_ROLE_NAME);
        adminRole.setRoleSort(1);
        adminRole.setStatus(1);
        adminRole.setDataScope(1);
        roleDao.save(adminRole);

        if (packageId != null && !packageId.isEmpty()) {
            SysTenantPackage pkg = packageDao.getById(packageId);
            if (pkg != null && pkg.getMenuIds() != null && !pkg.getMenuIds().isEmpty()) {
                List<String> menuIds = Arrays.stream(pkg.getMenuIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                if (!menuIds.isEmpty()) {
                    List<SysRoleMenu> roleMenus = new ArrayList<>();
                    for (String menuId : menuIds) {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(adminRole.getId());
                        rm.setMenuId(menuId);
                        roleMenus.add(rm);
                    }
                    roleMenuDao.insertBatchSomeColumn(roleMenus);
                }
            }
        }

        return adminRole.getId();
    }

    private void createTenantAdminUser(String tenantId, UserDTO adminDto, String roleId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, adminDto.getUsername())
               .eq(SysUser::getTenantId, tenantId);
        if (userDao.count(wrapper) > 0) {
            throw new BusinessException("该租户下用户名已存在");
        }

        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUsername(adminDto.getUsername());
        user.setPassword(adminDto.getPassword() != null ? BCrypt.hashpw(adminDto.getPassword()) : BCrypt.hashpw("123456"));
        user.setNickname(adminDto.getNickname() != null ? adminDto.getNickname() : adminDto.getUsername());
        user.setEmail(adminDto.getEmail());
        user.setMobile(adminDto.getMobile());
        user.setStatus(1);
        userDao.save(user);

        List<String> roleIds = adminDto.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            if (roleId != null) {
                roleIds = List.of(roleId);
            }
        }
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (String rid : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(rid);
                userRoles.add(ur);
            }
            userRoleDao.insertBatchSomeColumn(userRoles);
        }
    }
}
