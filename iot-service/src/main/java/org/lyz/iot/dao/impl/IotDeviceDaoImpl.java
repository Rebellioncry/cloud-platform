package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotDeviceDaoImpl extends ServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceDao {

    @TenantIgnore
    @Override
    public IotDevice getOneIgnoreTenant(Wrapper<IotDevice> queryWrapper) {
        return getOne(queryWrapper);
    }

    @TenantIgnore
    @Override
    public boolean updateByIdIgnoreTenant(IotDevice entity) {
        return updateById(entity);
    }
}
