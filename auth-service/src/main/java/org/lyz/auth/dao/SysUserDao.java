package org.lyz.auth.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.common.core.entity.SysUser;

import java.util.List;

public interface SysUserDao extends IService<SysUser> {

    List<String> selectRoleCodesByUserId(String userId);
}
