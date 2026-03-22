package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.RoleDTO;
import org.lyz.system.entity.SysRole;
import org.lyz.system.entity.SysRoleMenu;
import org.lyz.system.mapper.SysRoleMapper;
import org.lyz.system.mapper.SysRoleMenuMapper;
import org.lyz.system.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysRole> list() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            wrapper.eq(SysRole::getTenantId, Long.parseLong(tenantId));
        }
        return roleMapper.selectList(wrapper);
    }

    @Override
    public RoleDTO getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return toDTO(role);
    }

    @Override
    public void create(RoleDTO dto) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = "1";
        }

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode())
               .eq(SysRole::getTenantId, Long.parseLong(tenantId));
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = toEntity(dto);
        role.setTenantId(Long.parseLong(tenantId));
        roleMapper.insert(role);

        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            saveRoleMenus(role.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("角色ID不能为空");
        }
        SysRole role = toEntity(dto);
        roleMapper.updateById(role);

        if (dto.getMenuIds() != null) {
            roleMapper.deleteRoleMenus(dto.getId());
            if (!dto.getMenuIds().isEmpty()) {
                saveRoleMenus(dto.getId(), dto.getMenuIds());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        roleMapper.deleteById(id);
        roleMapper.deleteRoleMenus(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMapper.deleteRoleMenus(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            saveRoleMenus(roleId, menuIds);
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        List<SysRoleMenu> roleMenus = new ArrayList<>();
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        }
        roleMenuMapper.insertBatchSomeColumn(roleMenus);
    }

    private RoleDTO toDTO(SysRole role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setRoleSort(role.getRoleSort());
        dto.setStatus(role.getStatus());
        dto.setDataScope(role.getDataScope());
        dto.setRemark(role.getRemark());
        return dto;
    }

    private SysRole toEntity(RoleDTO dto) {
        SysRole role = new SysRole();
        role.setId(dto.getId());
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setRoleSort(dto.getRoleSort());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setDataScope(dto.getDataScope());
        role.setRemark(dto.getRemark());
        return role;
    }
}
