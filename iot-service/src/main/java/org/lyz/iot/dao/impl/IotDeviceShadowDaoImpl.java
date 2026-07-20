package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.iot.dao.IotDeviceShadowDao;
import org.lyz.iot.entity.IotDeviceShadow;
import org.lyz.iot.mapper.mysql.IotDeviceShadowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotDeviceShadowDaoImpl extends ServiceImpl<IotDeviceShadowMapper, IotDeviceShadow> implements IotDeviceShadowDao {
}
