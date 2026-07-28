package org.lyz.system.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.system.dao.SysTenantPackageDao;
import org.lyz.system.entity.SysTenantPackage;
import org.lyz.system.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysTenantPackageDaoImpl extends ServiceImpl<SysTenantPackageMapper, SysTenantPackage> implements SysTenantPackageDao {
}
