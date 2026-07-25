package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotFirmwareDao;
import org.lyz.iot.entity.IotFirmware;
import org.lyz.iot.mapper.mysql.IotFirmwareMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IotFirmwareDaoImpl extends ServiceImpl<IotFirmwareMapper, IotFirmware> implements IotFirmwareDao {

    @TenantIgnore
    @Override
    public List<IotFirmware> listIgnoreTenant(Wrapper<IotFirmware> queryWrapper) {
        return list(queryWrapper);
    }

    @TenantIgnore
    @Override
    public IotFirmware getOneIgnoreTenant(Wrapper<IotFirmware> queryWrapper) {
        return getOne(queryWrapper);
    }

    @TenantIgnore
    @Override
    public boolean updateByIdIgnoreTenant(IotFirmware entity) {
        return updateById(entity);
    }
}
