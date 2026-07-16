package org.lyz.iot.mapper.tdengine;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lyz.iot.entity.IotDeviceLog;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeviceLogMapper {

    void insert(@Param("l") IotDeviceLog log);

    List<IotDeviceLog> selectByDevice(@Param("deviceId") String deviceId,
                                       @Param("logType") String logType,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
}
