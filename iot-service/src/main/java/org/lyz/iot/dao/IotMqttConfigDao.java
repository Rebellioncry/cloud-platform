package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotMqttConfig;

import java.io.Serializable;
import java.util.List;

public interface IotMqttConfigDao extends IService<IotMqttConfig> {

    List<IotMqttConfig> listIgnoreTenant(Wrapper<IotMqttConfig> queryWrapper);

    IotMqttConfig getByIdIgnoreTenant(Serializable id);

    boolean updateByIdIgnoreTenant(IotMqttConfig entity);
}
