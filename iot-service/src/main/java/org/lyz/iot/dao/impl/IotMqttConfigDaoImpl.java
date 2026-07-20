package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotMqttConfigDao;
import org.lyz.iot.entity.IotMqttConfig;
import org.lyz.iot.mapper.mysql.IotMqttConfigMapper;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
public class IotMqttConfigDaoImpl extends ServiceImpl<IotMqttConfigMapper, IotMqttConfig> implements IotMqttConfigDao {

    @TenantIgnore
    @Override
    public List<IotMqttConfig> listIgnoreTenant(Wrapper<IotMqttConfig> queryWrapper) {
        return list(queryWrapper);
    }

    @TenantIgnore
    @Override
    public IotMqttConfig getByIdIgnoreTenant(Serializable id) {
        return getById(id);
    }

    @TenantIgnore
    @Override
    public boolean updateByIdIgnoreTenant(IotMqttConfig entity) {
        return updateById(entity);
    }
}
