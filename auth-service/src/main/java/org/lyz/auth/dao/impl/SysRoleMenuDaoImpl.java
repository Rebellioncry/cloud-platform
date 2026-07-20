package org.lyz.auth.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.auth.dao.SysRoleMenuDao;
import org.lyz.auth.entity.SysRoleMenu;
import org.lyz.auth.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysRoleMenuDaoImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuDao {
}
