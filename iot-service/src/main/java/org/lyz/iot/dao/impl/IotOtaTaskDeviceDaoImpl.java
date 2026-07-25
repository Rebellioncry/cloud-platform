package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotOtaTaskDeviceDao;
import org.lyz.iot.entity.IotOtaTaskDevice;
import org.lyz.iot.mapper.mysql.IotOtaTaskDeviceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IotOtaTaskDeviceDaoImpl extends ServiceImpl<IotOtaTaskDeviceMapper, IotOtaTaskDevice> implements IotOtaTaskDeviceDao {

    @TenantIgnore
    @Override
    public List<IotOtaTaskDevice> listIgnoreTenant(Wrapper<IotOtaTaskDevice> queryWrapper) {
        return list(queryWrapper);
    }

    @TenantIgnore
    @Override
    public IotOtaTaskDevice getOneIgnoreTenant(Wrapper<IotOtaTaskDevice> queryWrapper) {
        return getOne(queryWrapper);
    }

    @TenantIgnore
    @Override
    public boolean updateByIdIgnoreTenant(IotOtaTaskDevice entity) {
        return updateById(entity);
    }
}
