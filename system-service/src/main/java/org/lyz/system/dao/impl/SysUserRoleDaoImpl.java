package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.system.dao.SysUserRoleDao;
import org.lyz.system.entity.SysUserRole;
import org.lyz.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysUserRoleDaoImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleDao {

    @Override
    public int insertBatchSomeColumn(List<SysUserRole> userRoles) {
        return baseMapper.insertBatchSomeColumn(userRoles);
    }
}
