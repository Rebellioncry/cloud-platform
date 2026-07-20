package org.lyz.emqx.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.emqx.dao.EmqxAclDeviceDao;
import org.lyz.emqx.entity.EmqxAclDevice;
import org.lyz.emqx.mapper.EmqxAclDeviceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EmqxAclDeviceDaoImpl extends ServiceImpl<EmqxAclDeviceMapper, EmqxAclDevice> implements EmqxAclDeviceDao {
}
