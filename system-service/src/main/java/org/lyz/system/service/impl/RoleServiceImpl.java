package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.SecurityUtils;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.RoleDTO;
import org.lyz.system.entity.SysRole;
import org.lyz.common.core.result.PageResult;
import org.lyz.system.entity.SysRoleMenu;
import org.lyz.system.mapper.SysRoleMapper;
import org.lyz.system.mapper.SysRoleMenuMapper;
import org.lyz.system.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public PageResult<SysRole> list(int page, int size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (!SecurityUtils.isSuperAdmin()) {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                wrapper.eq(SysRole::getTenantId, tenantId);
            }
        }
        IPage<SysRole> result = roleMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public RoleDTO getById(String id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null && !tenantId.equals(role.getTenantId())) {
            throw new BusinessException("无权访问该角色");
        }
        RoleDTO dto = toDTO(role);
        LambdaQueryWrapper<SysRoleMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.eq(SysRoleMenu::getRoleId, id);
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(menuWrapper);
        dto.setMenuIds(roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public void create(RoleDTO dto) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = "1";
        }

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode())
               .eq(SysRole::getTenantId, tenantId);
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = toEntity(dto);
        role.setTenantId(tenantId);
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
        if ("3".equals(dto.getId())) {
            throw new BusinessException("不允许修改超级管理员角色");
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
    public void delete(String id) {
        if ("3".equals(id)) {
            throw new BusinessException("不允许删除超级管理员角色");
        }
        roleMapper.deleteById(id);
        roleMapper.deleteRoleMenus(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(String roleId, List<String> menuIds) {
        if ("3".equals(roleId)) {
            throw new BusinessException("不允许修改超级管理员角色菜单");
        }
        roleMapper.deleteRoleMenus(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            saveRoleMenus(roleId, menuIds);
        }
    }

    private void saveRoleMenus(String roleId, List<String> menuIds) {
        List<SysRoleMenu> roleMenus = new ArrayList<>();
        for (String menuId : menuIds) {
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
        dto.setCreateTime(role.getCreateTime());
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
