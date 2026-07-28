package org.lyz.auth.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.auth.dao.SysTenantDao;
import org.lyz.auth.entity.SysTenant;
import org.lyz.auth.mapper.SysTenantMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysTenantDaoImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantDao {
}
