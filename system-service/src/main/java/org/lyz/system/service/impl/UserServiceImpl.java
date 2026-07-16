package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.SecurityUtils;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.UserDTO;
import org.lyz.common.core.entity.SysUser;
import org.lyz.common.core.result.PageResult;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.entity.SysUserRole;
import org.lyz.system.mapper.SysTenantMapper;
import org.lyz.system.mapper.SysUserMapper;
import org.lyz.system.mapper.SysUserRoleMapper;
import org.lyz.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysTenantMapper tenantMapper;

    @Override
    public PageResult<UserDTO> list(int page, int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (!SecurityUtils.isSuperAdmin()) {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                wrapper.eq(SysUser::getTenantId, tenantId);
            }
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> result = userMapper.selectPage(pageParam, wrapper);

        List<SysUser> users = result.getRecords();
        Set<String> tenantIds = users.stream().map(SysUser::getTenantId).collect(Collectors.toSet());
        Map<String, String> tenantNameMap = Map.of();
        if (!tenantIds.isEmpty()) {
            List<SysTenant> tenants = tenantMapper.selectBatchIds(tenantIds);
            tenantNameMap = tenants.stream().collect(Collectors.toMap(SysTenant::getId, SysTenant::getTenantName));
        }
        final Map<String, String> tnMap = tenantNameMap;

        Set<String> userIds = users.stream().map(SysUser::getId).collect(Collectors.toSet());
        Map<String, List<String>> userRoleMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.in(SysUserRole::getUserId, userIds);
            List<SysUserRole> allUserRoles = userRoleMapper.selectList(roleWrapper);
            for (SysUserRole ur : allUserRoles) {
                userRoleMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(ur.getRoleId());
            }
        }

        List<UserDTO> dtoList = users.stream().map(u -> {
            UserDTO dto = toDTO(u);
            dto.setTenantName(tnMap.getOrDefault(u.getTenantId(), ""));
            dto.setRoleIds(userRoleMap.getOrDefault(u.getId(), new ArrayList<>()));
            return dto;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), page, size, dtoList);
    }

    @Override
    public UserDTO getById(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null && !tenantId.equals(user.getTenantId())) {
            throw new BusinessException("无权访问该用户");
        }
        UserDTO dto = toDTO(user);
        LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysUserRole::getUserId, id);
        List<SysUserRole> userRoles = userRoleMapper.selectList(roleWrapper);
        dto.setRoleIds(userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserDTO dto) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = "1";
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername())
               .eq(SysUser::getTenantId, tenantId);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = toEntity(dto);
        user.setTenantId(tenantId);
        userMapper.insert(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            saveUserRoles(user.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if ("2".equals(dto.getId())) {
            throw new BusinessException("不允许修改超级管理员");
        }
        SysUser user = toEntity(dto);
        userMapper.updateById(user);

        if (dto.getRoleIds() != null) {
            userMapper.deleteUserRoles(dto.getId());
            if (!dto.getRoleIds().isEmpty()) {
                saveUserRoles(dto.getId(), dto.getRoleIds());
            }
        }
    }

    @Override
    public void delete(String id) {
        if ("2".equals(id)) {
            throw new BusinessException("不允许删除超级管理员");
        }
        userMapper.deleteById(id);
        userMapper.deleteUserRoles(id);
    }

    @Override
    public void resetPassword(String id, String password) {
        if ("2".equals(id)) {
            throw new BusinessException("不允许重置超级管理员密码");
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id)
               .set(SysUser::getPassword, password);
        userMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(String userId, List<String> roleIds) {
        if ("2".equals(userId)) {
            throw new BusinessException("不允许修改超级管理员角色");
        }
        userMapper.deleteUserRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            saveUserRoles(userId, roleIds);
        }
    }

    private void saveUserRoles(String userId, List<String> roleIds) {
        List<SysUserRole> userRoles = new ArrayList<>();
        for (String roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        userRoleMapper.insertBatchSomeColumn(userRoles);
    }

    private UserDTO toDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setTenantId(user.getTenantId());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }

    private SysUser toEntity(UserDTO dto) {
        SysUser user = new SysUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(dto.getPassword());
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        if (dto.getTenantId() != null) {
            user.setTenantId(dto.getTenantId());
        }
        return user;
    }
}
