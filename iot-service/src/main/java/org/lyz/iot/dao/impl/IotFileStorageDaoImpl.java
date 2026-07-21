package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotFileStorageDao;
import org.lyz.iot.entity.IotFileStorage;
import org.lyz.iot.mapper.mysql.IotFileStorageMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotFileStorageDaoImpl extends ServiceImpl<IotFileStorageMapper, IotFileStorage> implements IotFileStorageDao {

    @TenantIgnore
    @Override
    public IotFileStorage getOneIgnoreTenant(Wrapper<IotFileStorage> queryWrapper) {
        return getOne(queryWrapper);
    }

    @TenantIgnore
    @Override
    public boolean updateByIdIgnoreTenant(IotFileStorage entity) {
        return updateById(entity);
    }
}
