package org.lyz.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.system.entity.SysRoleMenu;

import java.util.List;

public interface SysRoleMenuDao extends IService<SysRoleMenu> {

    int insertBatchSomeColumn(List<SysRoleMenu> roleMenus);
}
