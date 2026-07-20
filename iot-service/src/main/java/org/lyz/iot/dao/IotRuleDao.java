package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotRule;

import java.util.List;

public interface IotRuleDao extends IService<IotRule> {

    List<IotRule> listIgnoreTenant();

    boolean updateIgnoreTenant(IotRule entity, Wrapper<IotRule> updateWrapper);
}
