package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.UserContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.RoleDTO;
import org.lyz.system.entity.SysRole;
import org.lyz.common.core.result.PageResult;
import org.lyz.system.entity.SysMenu;
import org.lyz.system.entity.SysRoleMenu;
import org.lyz.system.dao.SysMenuDao;
import org.lyz.system.dao.SysRoleDao;
import org.lyz.system.dao.SysRoleMenuDao;
import org.lyz.system.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleDao roleDao;
    private final SysRoleMenuDao roleMenuDao;
    private final SysMenuDao menuDao;

    @Override
    public PageResult<SysRole> list(int page, int size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (!UserContext.isPlatformAdmin()) {
            String tenantId = UserContext.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                wrapper.eq(SysRole::getTenantId, tenantId);
            }
        }
        IPage<SysRole> result = roleDao.page(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public RoleDTO getById(String id) {
        SysRole role = roleDao.getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        RoleDTO dto = toDTO(role);
        LambdaQueryWrapper<SysRoleMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.eq(SysRoleMenu::getRoleId, id);
        List<SysRoleMenu> roleMenus = roleMenuDao.list(menuWrapper);
        dto.setMenuIds(roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public void create(RoleDTO dto) {
        if (!UserContext.isPlatformAdmin()) {
            String tenantId = UserContext.getTenantId();
            if (tenantId == null || tenantId.isEmpty()) {
                throw new BusinessException("租户信息异常");
            }
            dto.setTenantId(tenantId);
            dto.setScope("TENANT");
            dto.setIsSystem(0);
        }
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (roleDao.count(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = toEntity(dto);
        roleDao.save(role);

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
        SysRole existing = roleDao.getById(dto.getId());
        if (existing != null && Integer.valueOf(1).equals(existing.getIsSystem())) {
            throw new BusinessException("不允许修改系统内置角色");
        }
        SysRole role = toEntity(dto);
        roleDao.updateById(role);

        if (dto.getMenuIds() != null) {
            roleDao.deleteRoleMenus(dto.getId());
            if (!dto.getMenuIds().isEmpty()) {
                saveRoleMenus(dto.getId(), dto.getMenuIds());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        SysRole existing = roleDao.getById(id);
        if (existing != null && Integer.valueOf(1).equals(existing.getIsSystem())) {
            throw new BusinessException("不允许删除系统内置角色");
        }
        roleDao.removeById(id);
        roleDao.deleteRoleMenus(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(String roleId, List<String> menuIds) {
        SysRole existing = roleDao.getById(roleId);
        if (existing != null && Integer.valueOf(1).equals(existing.getIsSystem())) {
            throw new BusinessException("不允许修改系统内置角色菜单");
        }
        if (!UserContext.isPlatformAdmin() && menuIds != null && !menuIds.isEmpty()) {
            List<SysMenu> menus = menuDao.listByIds(menuIds);
            boolean hasPlatform = menus.stream().anyMatch(m -> "PLATFORM".equals(m.getScope()));
            if (hasPlatform) {
                throw new BusinessException("不允许分配平台菜单");
            }
        }
        roleDao.deleteRoleMenus(roleId);
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
        roleMenuDao.insertBatchSomeColumn(roleMenus);
    }

    private RoleDTO toDTO(SysRole role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setRoleSort(role.getRoleSort());
        dto.setStatus(role.getStatus());
        dto.setDataScope(role.getDataScope());
        dto.setScope(role.getScope());
        dto.setTenantId(role.getTenantId());
        dto.setIsSystem(role.getIsSystem());
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
        role.setScope(dto.getScope());
        role.setIsSystem(dto.getIsSystem());
        role.setRemark(dto.getRemark());
        if (dto.getTenantId() != null) {
            role.setTenantId(dto.getTenantId());
        }
        return role;
    }
}
