package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.system.dao.SysTenantDao;
import org.lyz.system.dao.SysTenantPackageDao;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.entity.SysTenantPackage;
import org.lyz.system.service.TenantPackageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantPackageServiceImpl implements TenantPackageService {

    private final SysTenantPackageDao packageDao;
    private final SysTenantDao tenantDao;

    @Override
    public PageResult<SysTenantPackage> list(int page, int size) {
        Page<SysTenantPackage> pageParam = new Page<>(page, size);
        IPage<SysTenantPackage> result = packageDao.page(pageParam,
                new LambdaQueryWrapper<SysTenantPackage>().orderByDesc(SysTenantPackage::getCreateTime));
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public SysTenantPackage getById(String id) {
        SysTenantPackage pkg = packageDao.getById(id);
        if (pkg == null) {
            throw new BusinessException("套餐不存在");
        }
        return pkg;
    }

    @Override
    public List<SysTenantPackage> listAll() {
        return packageDao.list(new LambdaQueryWrapper<SysTenantPackage>()
                .eq(SysTenantPackage::getStatus, 1)
                .orderByDesc(SysTenantPackage::getCreateTime));
    }

    @Override
    public void create(SysTenantPackage pkg) {
        packageDao.save(pkg);
    }

    @Override
    public void update(SysTenantPackage pkg) {
        if (pkg.getId() == null) {
            throw new BusinessException("套餐ID不能为空");
        }
        packageDao.updateById(pkg);
    }

    @Override
    public void delete(String id) {
        long count = tenantDao.count(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getPackageId, id));
        if (count > 0) {
            throw new BusinessException("该套餐已被 " + count + " 个租户使用，无法删除");
        }
        packageDao.removeById(id);
    }
}
