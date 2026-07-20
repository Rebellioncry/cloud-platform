package org.lyz.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.system.entity.SysRole;

import java.util.List;

public interface SysRoleDao extends IService<SysRole> {

    void deleteRoleMenus(String roleId);

    void insertRoleMenus(String roleId, List<String> menuIds);
}
