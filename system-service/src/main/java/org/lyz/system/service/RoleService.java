package org.lyz.system.service;

import org.lyz.system.dto.RoleDTO;
import org.lyz.system.entity.SysRole;
import org.lyz.common.core.result.PageResult;
import java.util.List;

public interface RoleService {
    PageResult<SysRole> list(int page, int size);
    RoleDTO getById(String id);
    void create(RoleDTO dto);
    void update(RoleDTO dto);
    void delete(String id);
    void assignMenus(String roleId, List<String> menuIds);
}
