package org.lyz.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.system.entity.SysUserRole;

import java.util.List;

public interface SysUserRoleDao extends IService<SysUserRole> {

    int insertBatchSomeColumn(List<SysUserRole> userRoles);
}
