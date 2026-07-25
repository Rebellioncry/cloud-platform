package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotOtaTaskDevice;

import java.util.List;

public interface IotOtaTaskDeviceDao extends IService<IotOtaTaskDevice> {

    List<IotOtaTaskDevice> listIgnoreTenant(Wrapper<IotOtaTaskDevice> queryWrapper);

    IotOtaTaskDevice getOneIgnoreTenant(Wrapper<IotOtaTaskDevice> queryWrapper);

    boolean updateByIdIgnoreTenant(IotOtaTaskDevice entity);
}
