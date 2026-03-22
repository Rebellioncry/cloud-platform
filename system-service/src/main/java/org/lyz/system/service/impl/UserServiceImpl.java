package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.UserDTO;
import org.lyz.common.core.entity.SysUser;
import org.lyz.system.entity.SysUserRole;
import org.lyz.system.mapper.SysUserMapper;
import org.lyz.system.mapper.SysUserRoleMapper;
import org.lyz.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<SysUser> list() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            wrapper.eq(SysUser::getTenantId, Long.parseLong(tenantId));
        }
        return userMapper.selectList(wrapper);
    }

    @Override
    public UserDTO getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toDTO(user);
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
               .eq(SysUser::getTenantId, Long.parseLong(tenantId));
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = toEntity(dto);
        user.setTenantId(Long.parseLong(tenantId));
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
    public void delete(Long id) {
        userMapper.deleteById(id);
        userMapper.deleteUserRoles(id);
    }

    @Override
    public void resetPassword(Long id, String password) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id)
               .set(SysUser::getPassword, password);
        userMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userMapper.deleteUserRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            saveUserRoles(userId, roleIds);
        }
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        List<SysUserRole> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
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
        return user;
    }
}
