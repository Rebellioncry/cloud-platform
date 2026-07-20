package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.system.dao.SysTenantDao;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.mapper.SysTenantMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysTenantDaoImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantDao {
}
