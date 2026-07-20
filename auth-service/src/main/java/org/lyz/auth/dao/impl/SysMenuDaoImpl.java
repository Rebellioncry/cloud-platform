package org.lyz.auth.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.auth.dao.SysMenuDao;
import org.lyz.auth.entity.SysMenu;
import org.lyz.auth.mapper.SysMenuMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysMenuDaoImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuDao {
}
