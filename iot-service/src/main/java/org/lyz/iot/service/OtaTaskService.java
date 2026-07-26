package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.OtaTaskDTO;
import org.lyz.iot.entity.IotOtaTask;
import org.lyz.iot.entity.IotOtaTaskDevice;

import java.util.List;

public interface OtaTaskService {
    PageResult<IotOtaTask> list(int page, int size, String taskName);
    IotOtaTask getById(String id);
    IotOtaTask create(OtaTaskDTO dto);
    void delete(String id);
    void start(String id);
    void cancel(String id);
    List<IotOtaTaskDevice> listTaskDevices(String taskId);
    void updateDeviceProgress(String taskId, String deviceName, Integer progress, String status, String errorMessage);
    void retryDevices(String taskId, List<String> deviceIds);
}
