package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotOtaTask;

import java.util.List;

public interface IotOtaTaskDao extends IService<IotOtaTask> {

    List<IotOtaTask> listIgnoreTenant(Wrapper<IotOtaTask> queryWrapper);

    boolean updateByIdIgnoreTenant(IotOtaTask entity);
}
