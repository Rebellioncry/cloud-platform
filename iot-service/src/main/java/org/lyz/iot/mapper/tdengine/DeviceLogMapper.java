package org.lyz.iot.mapper.tdengine;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    @Select("SELECT ts, log_type, content, message_id FROM device_log " +
            "WHERE device_id = #{deviceId} AND log_type = 'event' " +
            "ORDER BY ts DESC LIMIT 1")
    IotDeviceLog selectLatestEvent(@Param("deviceId") String deviceId);

    List<IotDeviceLog> selectLatestEvents(@Param("deviceId") String deviceId, @Param("limit") int limit);
}
