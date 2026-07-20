package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.system.dao.SysMenuDao;
import org.lyz.system.entity.SysMenu;
import org.lyz.system.mapper.SysMenuMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysMenuDaoImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuDao {
}
