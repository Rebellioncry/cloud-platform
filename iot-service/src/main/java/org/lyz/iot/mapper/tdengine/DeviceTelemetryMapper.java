package org.lyz.iot.mapper.tdengine;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lyz.iot.entity.IotDeviceTelemetry;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeviceTelemetryMapper {

    void insert(@Param("t") IotDeviceTelemetry telemetry);

    @Select("SELECT ts, value, value_type, quality FROM device_telemetry " +
            "WHERE device_id = #{deviceId} AND property_id = #{propertyId} " +
            "ORDER BY ts DESC LIMIT 1")
    IotDeviceTelemetry selectLatest(@Param("deviceId") String deviceId,
                                     @Param("propertyId") String propertyId);

    @Select("SELECT ts, value, value_type, quality FROM device_telemetry " +
            "WHERE device_id = #{deviceId} AND property_id = #{propertyId} " +
            "AND ts >= #{startTime} AND ts <= #{endTime} " +
            "ORDER BY ts DESC")
    List<IotDeviceTelemetry> selectHistory(@Param("deviceId") String deviceId,
                                            @Param("propertyId") String propertyId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}
