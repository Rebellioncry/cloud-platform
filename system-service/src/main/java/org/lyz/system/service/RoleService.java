package org.lyz.system.service;

import org.lyz.system.dto.RoleDTO;
import org.lyz.system.entity.SysRole;
import java.util.List;

public interface RoleService {
    List<SysRole> list();
    RoleDTO getById(Long id);
    void create(RoleDTO dto);
    void update(RoleDTO dto);
    void delete(Long id);
    void assignMenus(Long roleId, List<Long> menuIds);
}
