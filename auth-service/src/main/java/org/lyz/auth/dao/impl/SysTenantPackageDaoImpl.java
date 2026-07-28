package org.lyz.auth.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.auth.dao.SysTenantPackageDao;
import org.lyz.auth.entity.SysTenantPackage;
import org.lyz.auth.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysTenantPackageDaoImpl extends ServiceImpl<SysTenantPackageMapper, SysTenantPackage> implements SysTenantPackageDao {
}
