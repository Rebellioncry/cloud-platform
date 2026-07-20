package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotDeviceCommandDao;
import org.lyz.iot.entity.IotDeviceCommand;
import org.lyz.iot.mapper.mysql.IotDeviceCommandMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotDeviceCommandDaoImpl extends ServiceImpl<IotDeviceCommandMapper, IotDeviceCommand> implements IotDeviceCommandDao {

    @TenantIgnore
    @Override
    public boolean saveIgnoreTenant(IotDeviceCommand entity) {
        return save(entity);
    }
}
