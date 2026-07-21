package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotFileStorage;

public interface IotFileStorageDao extends IService<IotFileStorage> {

    IotFileStorage getOneIgnoreTenant(Wrapper<IotFileStorage> queryWrapper);

    boolean updateByIdIgnoreTenant(IotFileStorage entity);
}
