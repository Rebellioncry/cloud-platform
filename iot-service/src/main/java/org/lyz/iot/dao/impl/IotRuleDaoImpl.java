package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotRuleDao;
import org.lyz.iot.entity.IotRule;
import org.lyz.iot.mapper.mysql.IotRuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IotRuleDaoImpl extends ServiceImpl<IotRuleMapper, IotRule> implements IotRuleDao {

    @TenantIgnore
    @Override
    public List<IotRule> listIgnoreTenant() {
        return list();
    }

    @TenantIgnore
    @Override
    public boolean updateIgnoreTenant(IotRule entity, Wrapper<IotRule> updateWrapper) {
        return update(entity, updateWrapper);
    }
}
