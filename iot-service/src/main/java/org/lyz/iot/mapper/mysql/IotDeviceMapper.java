package org.lyz.iot.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lyz.iot.entity.IotDevice;

@Mapper
public interface IotDeviceMapper extends BaseMapper<IotDevice> {
}
