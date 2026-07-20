package org.lyz.iot.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lyz.iot.entity.IotRuleExecLog;

public interface IotRuleExecLogDao extends IService<IotRuleExecLog> {

    boolean saveIgnoreTenant(IotRuleExecLog entity);

    IPage<IotRuleExecLog> pageIgnoreTenant(IPage<IotRuleExecLog> page, Wrapper<IotRuleExecLog> queryWrapper);
}
