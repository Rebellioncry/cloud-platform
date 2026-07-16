package org.lyz.system.service;

import org.lyz.system.dto.MenuDTO;
import org.lyz.system.entity.SysMenu;
import org.lyz.common.core.result.PageResult;
import java.util.List;

public interface MenuService {
    PageResult<SysMenu> list(int page, int size);
    List<MenuDTO> getMenuTree();
    MenuDTO getById(String id);
    void create(MenuDTO dto);
    void update(MenuDTO dto);
    void delete(String id);
}
