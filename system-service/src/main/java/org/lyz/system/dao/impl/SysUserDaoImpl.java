package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.core.entity.SysUser;
import org.lyz.system.dao.SysUserDao;
import org.lyz.system.mapper.SysUserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysUserDaoImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserDao {

    @Override
    public void deleteUserRoles(String userId) {
        baseMapper.deleteUserRoles(userId);
    }

    @Override
    public void insertUserRoles(String userId, List<String> roleIds) {
        baseMapper.insertUserRoles(userId, roleIds);
    }
}
