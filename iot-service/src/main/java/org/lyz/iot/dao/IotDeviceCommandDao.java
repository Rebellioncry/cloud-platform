package org.lyz.iot.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotDeviceCommand;

public interface IotDeviceCommandDao extends IService<IotDeviceCommand> {

    boolean saveIgnoreTenant(IotDeviceCommand entity);
}
