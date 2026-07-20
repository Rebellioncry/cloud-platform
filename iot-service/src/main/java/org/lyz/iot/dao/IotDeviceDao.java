package org.lyz.iot.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotDevice;

public interface IotDeviceDao extends IService<IotDevice> {

    IotDevice getOneIgnoreTenant(com.baomidou.mybatisplus.core.conditions.Wrapper<IotDevice> queryWrapper);

    boolean updateByIdIgnoreTenant(IotDevice entity);
}
