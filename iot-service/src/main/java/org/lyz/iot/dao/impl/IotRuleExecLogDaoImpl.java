package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotRuleExecLogDao;
import org.lyz.iot.entity.IotRuleExecLog;
import org.lyz.iot.mapper.mysql.IotRuleExecLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotRuleExecLogDaoImpl extends ServiceImpl<IotRuleExecLogMapper, IotRuleExecLog> implements IotRuleExecLogDao {

    @TenantIgnore
    @Override
    public boolean saveIgnoreTenant(IotRuleExecLog entity) {
        return save(entity);
    }

    @TenantIgnore
    @Override
    public IPage<IotRuleExecLog> pageIgnoreTenant(IPage<IotRuleExecLog> page, Wrapper<IotRuleExecLog> queryWrapper) {
        return page(page, queryWrapper);
    }
}
