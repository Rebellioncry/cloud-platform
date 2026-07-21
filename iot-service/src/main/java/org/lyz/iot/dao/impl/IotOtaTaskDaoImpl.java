package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotOtaTaskDao;
import org.lyz.iot.entity.IotOtaTask;
import org.lyz.iot.mapper.mysql.IotOtaTaskMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IotOtaTaskDaoImpl extends ServiceImpl<IotOtaTaskMapper, IotOtaTask> implements IotOtaTaskDao {

    @TenantIgnore
    @Override
    public List<IotOtaTask> listIgnoreTenant(Wrapper<IotOtaTask> queryWrapper) {
        return list(queryWrapper);
    }

    @TenantIgnore
    @Override
    public boolean updateByIdIgnoreTenant(IotOtaTask entity) {
        return updateById(entity);
    }
}
