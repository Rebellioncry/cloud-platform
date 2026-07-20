package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.system.dao.SysRoleDao;
import org.lyz.system.entity.SysRole;
import org.lyz.system.mapper.SysRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysRoleDaoImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleDao {

    @Override
    public void deleteRoleMenus(String roleId) {
        baseMapper.deleteRoleMenus(roleId);
    }

    @Override
    public void insertRoleMenus(String roleId, List<String> menuIds) {
        baseMapper.insertRoleMenus(roleId, menuIds);
    }
}
