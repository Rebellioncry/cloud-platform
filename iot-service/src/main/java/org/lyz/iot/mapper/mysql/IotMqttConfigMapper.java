package org.lyz.iot.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lyz.iot.entity.IotMqttConfig;

@Mapper
public interface IotMqttConfigMapper extends BaseMapper<IotMqttConfig> {
}
