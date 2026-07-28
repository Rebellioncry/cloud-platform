package org.lyz.system.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.system.entity.SysTenantPackage;

import java.util.List;

public interface TenantPackageService {
    PageResult<SysTenantPackage> list(int page, int size);
    SysTenantPackage getById(String id);
    List<SysTenantPackage> listAll();
    void create(SysTenantPackage pkg);
    void update(SysTenantPackage pkg);
    void delete(String id);
}
