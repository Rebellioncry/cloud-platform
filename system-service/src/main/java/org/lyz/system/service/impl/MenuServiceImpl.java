package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.MenuDTO;
import org.lyz.system.entity.SysMenu;
import org.lyz.common.core.result.PageResult;
import org.lyz.system.dao.SysMenuDao;
import org.lyz.system.service.MenuService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuDao menuDao;

    @Override
    public PageResult<SysMenu> list(int page, int size) {
        Page<SysMenu> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getOrderNum);
        IPage<SysMenu> result = menuDao.page(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public List<MenuDTO> getMenuTree() {
        List<SysMenu> menus = listAll();
        return buildTree(menus, "0");
    }

    private List<SysMenu> listAll() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getOrderNum);
        return menuDao.list(wrapper);
    }

    @Override
    public MenuDTO getById(String id) {
        SysMenu menu = menuDao.getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return toDTO(menu);
    }

    @Override
    public void create(MenuDTO dto) {
        SysMenu menu = toEntity(dto);
        menuDao.save(menu);
    }

    @Override
    public void update(MenuDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("菜单ID不能为空");
        }
        SysMenu menu = toEntity(dto);
        menuDao.updateById(menu);
    }

    @Override
    public void delete(String id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        if (menuDao.count(wrapper) > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        menuDao.removeById(id);
    }

    private List<MenuDTO> buildTree(List<SysMenu> menus, String parentId) {
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
        dto.setCreateTime(menu.getCreateTime());
        return dto;
    }

    private SysMenu toEntity(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        menu.setId(dto.getId());
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : "0");
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
