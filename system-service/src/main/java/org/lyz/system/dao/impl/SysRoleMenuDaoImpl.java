package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.system.dao.SysRoleMenuDao;
import org.lyz.system.entity.SysRoleMenu;
import org.lyz.system.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysRoleMenuDaoImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuDao {

    @Override
    public int insertBatchSomeColumn(List<SysRoleMenu> roleMenus) {
        return baseMapper.insertBatchSomeColumn(roleMenus);
    }
}
