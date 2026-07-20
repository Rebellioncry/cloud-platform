package org.lyz.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.common.core.entity.SysUser;

import java.util.List;

public interface SysUserDao extends IService<SysUser> {

    void deleteUserRoles(String userId);

    void insertUserRoles(String userId, List<String> roleIds);
}
