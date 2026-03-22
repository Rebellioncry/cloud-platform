package org.lyz.system.service;

import org.lyz.system.dto.MenuDTO;
import org.lyz.system.entity.SysMenu;
import java.util.List;

public interface MenuService {
    List<SysMenu> list();
    List<MenuDTO> getMenuTree();
    MenuDTO getById(Long id);
    void create(MenuDTO dto);
    void update(MenuDTO dto);
    void delete(Long id);
}
