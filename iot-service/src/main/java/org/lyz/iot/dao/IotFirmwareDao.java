package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotFirmware;

import java.util.List;

public interface IotFirmwareDao extends IService<IotFirmware> {

    List<IotFirmware> listIgnoreTenant(Wrapper<IotFirmware> queryWrapper);

    IotFirmware getOneIgnoreTenant(Wrapper<IotFirmware> queryWrapper);

    boolean updateByIdIgnoreTenant(IotFirmware entity);
}
