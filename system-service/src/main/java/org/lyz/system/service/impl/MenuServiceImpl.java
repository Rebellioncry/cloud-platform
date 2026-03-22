package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.MenuDTO;
import org.lyz.system.entity.SysMenu;
import org.lyz.system.mapper.SysMenuMapper;
import org.lyz.system.service.MenuService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> list() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            wrapper.eq(SysMenu::getTenantId, Long.parseLong(tenantId));
        }
        wrapper.orderByAsc(SysMenu::getOrderNum);
        return menuMapper.selectList(wrapper);
    }

    @Override
    public List<MenuDTO> getMenuTree() {
        List<SysMenu> menus = list();
        return buildTree(menus, 0L);
    }

    @Override
    public MenuDTO getById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return toDTO(menu);
    }

    @Override
    public void create(MenuDTO dto) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = "1";
        }

        SysMenu menu = toEntity(dto);
        menu.setTenantId(Long.parseLong(tenantId));
        menuMapper.insert(menu);
    }

    @Override
    public void update(MenuDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("菜单ID不能为空");
        }
        SysMenu menu = toEntity(dto);
        menuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        if (menuMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
    }

    private List<MenuDTO> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .map(m -> {
                    MenuDTO dto = toDTO(m);
                    dto.setChildren(buildTree(menus, m.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private MenuDTO toDTO(SysMenu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setParentId(menu.getParentId());
        dto.setMenuName(menu.getMenuName());
        dto.setMenuType(menu.getMenuType());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setIcon(menu.getIcon());
        dto.setPerms(menu.getPerms());
        dto.setOrderNum(menu.getOrderNum());
        dto.setVisible(menu.getVisible());
        dto.setStatus(menu.getStatus());
        return dto;
    }

    private SysMenu toEntity(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setId(dto.getId());
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        menu.setMenuName(dto.getMenuName());
        menu.setMenuType(dto.getMenuType() != null ? dto.getMenuType() : 1);
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setIcon(dto.getIcon());
        menu.setPerms(dto.getPerms());
        menu.setOrderNum(dto.getOrderNum() != null ? dto.getOrderNum() : 0);
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : 1);
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return menu;
    }
}
