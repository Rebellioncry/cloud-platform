package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.iot.dao.IotOtaTaskDeviceDao;
import org.lyz.iot.entity.IotOtaTaskDevice;
import org.lyz.iot.mapper.mysql.IotOtaTaskDeviceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotOtaTaskDeviceDaoImpl extends ServiceImpl<IotOtaTaskDeviceMapper, IotOtaTaskDevice> implements IotOtaTaskDeviceDao {
}
