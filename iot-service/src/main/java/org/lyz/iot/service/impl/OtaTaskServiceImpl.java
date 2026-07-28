package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.context.TenantHelper;
import org.lyz.common.core.context.UserContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.OtaTaskDTO;
import org.lyz.iot.entity.*;
import org.lyz.iot.dao.*;
import org.lyz.iot.service.OtaTaskService;
import org.lyz.iot.service.FirmwareService;
import org.lyz.iot.mqtt.MqttTopicConstants;
import org.lyz.iot.mqtt.MqttClientManager;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtaTaskServiceImpl implements OtaTaskService {

    private final IotOtaTaskDao taskDao;
    private final IotOtaTaskDeviceDao taskDeviceDao;
    private final IotFirmwareDao firmwareDao;
    private final IotDeviceDao deviceDao;
    private final IotMqttConfigDao mqttConfigDao;
    private final FirmwareService firmwareService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;

    @Override
    public PageResult<IotOtaTask> list(int page, int size, String taskName) {
        Page<IotOtaTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotOtaTask> wrapper = new LambdaQueryWrapper<>();
        if (!UserContext.isPlatformAdmin()) {
            String tenantId = UserContext.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                wrapper.eq(IotOtaTask::getTenantId, tenantId);
            }
        }
        if (taskName != null && !taskName.isEmpty()) {
            wrapper.like(IotOtaTask::getTaskName, taskName);
        }
        wrapper.orderByDesc(IotOtaTask::getCreateTime);
        IPage<IotOtaTask>[] resultRef = new IPage[1];
        TenantHelper.ignore(() -> resultRef[0] = taskDao.page(pageParam, wrapper));
        return PageResult.of(resultRef[0].getTotal(), page, size, resultRef[0].getRecords());
    }

    @Override
    public IotOtaTask getById(String id) {
        IotOtaTask[] ref = new IotOtaTask[1];
        TenantHelper.ignore(() -> ref[0] = taskDao.getById(id));
        if (ref[0] == null) {
            throw new BusinessException("升级任务不存在");
        }
        return ref[0];
    }

    @Override
    @Transactional
    public IotOtaTask create(OtaTaskDTO dto) {
        IotFirmware[] firmwareRef = new IotFirmware[1];
        TenantHelper.ignore(() -> firmwareRef[0] = firmwareDao.getById(dto.getFirmwareId()));
        if (firmwareRef[0] == null) {
            throw new BusinessException("固件不存在或未发布");
        }
        IotFirmware firmware = firmwareRef[0];

        IotOtaTask task = new IotOtaTask();
        String tenantId = UserContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            task.setTenantId(tenantId);
        }
        task.setTaskName(dto.getTaskName());
        task.setFirmwareId(dto.getFirmwareId());
        task.setProductId(dto.getProductId());
        task.setProductKey(firmware.getProductKey());
        task.setTargetType(dto.getTargetType());
        task.setTargetValue(dto.getTargetValue());
        task.setStatus(0);
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setProgress(0);
        taskDao.save(task);
        return task;
    }

    @Override
    @Transactional
    public void delete(String id) {
        IotOtaTask task = getById(id);
        if (task.getStatus() == 1) {
            throw new BusinessException("执行中的任务不能删除");
        }
        taskDeviceDao.remove(new LambdaQueryWrapper<IotOtaTaskDevice>().eq(IotOtaTaskDevice::getTaskId, id));
        taskDao.removeById(id);
    }

    @Override
    @Transactional
    public void start(String id) {
        IotOtaTask task = getById(id);
        if (task.getStatus() == 1) {
            throw new BusinessException("任务已在执行中");
        }

        IotFirmware[] firmwareRef = new IotFirmware[1];
        TenantHelper.ignore(() -> firmwareRef[0] = firmwareDao.getById(task.getFirmwareId()));
        if (firmwareRef[0] == null) {
            throw new BusinessException("关联固件不存在");
        }
        IotFirmware firmware = firmwareRef[0];

        List<IotDevice> devices = matchDevices(task);
        if (devices.isEmpty()) {
            throw new BusinessException("没有匹配到需要升级的设备");
        }

        task.setTotalCount(devices.size());
        task.setStatus(1);
        task.setStartTime(LocalDateTime.now());
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setProgress(0);
        taskDao.updateById(task);

        String downloadUrl = firmwareService.getDownloadUrl(task.getFirmwareId());

        IotMqttConfig mqttConfig = findActiveMqttConfig();
        if (mqttConfig == null) {
            log.warn("OTA推送: 无可用MQTT连接");
        }

        for (IotDevice device : devices) {
            IotOtaTaskDevice taskDevice = new IotOtaTaskDevice();
            taskDevice.setTaskId(id);
            taskDevice.setDeviceId(device.getId());
            taskDevice.setDeviceName(device.getDeviceName());
            taskDevice.setProductKey(device.getProductKey());
            taskDevice.setCurrentVersion(device.getFirmwareVersion());
            taskDevice.setTargetVersion(firmware.getFirmwareVersion());
            taskDevice.setStatus(1);
            taskDevice.setProgress(0);
            taskDevice.setPushTime(LocalDateTime.now());
            taskDeviceDao.save(taskDevice);

            if (mqttConfig != null) {
                try {
                    String topic = MqttTopicConstants.buildOtaUpgradeTopic(device.getProductKey(), device.getDeviceName());
                    String payload = objectMapper.writeValueAsString(java.util.Map.of(
                            "firmwareUrl", downloadUrl,
                            "firmwareVersion", firmware.getFirmwareVersion(),
                            "fileSize", firmware.getFileSize(),
                            "fileMd5", firmware.getFileMd5() != null ? firmware.getFileMd5() : "",
                            "taskId", id
                    ));
                    applicationContext.getBean(MqttClientManager.class).publish(mqttConfig.getId(), topic, payload, mqttConfig.getQos());
                } catch (Exception e) {
                    log.error("OTA推送失败: device={}", device.getDeviceName(), e);
                    taskDevice.setStatus(5);
                    taskDevice.setErrorMessage("推送失败: " + e.getMessage());
                    taskDevice.setCompleteTime(LocalDateTime.now());
                    taskDeviceDao.updateById(taskDevice);
                    task.setFailCount(task.getFailCount() + 1);
                }
            }
        }

        taskDao.updateById(task);
        log.info("OTA任务已启动: taskId={}, 设备数={}", id, devices.size());
    }

    @Override
    @Transactional
    public void cancel(String id) {
        IotOtaTask task = getById(id);
        if (task.getStatus() != 1) {
            throw new BusinessException("只有执行中的任务可以取消");
        }
        task.setStatus(3);
        task.setEndTime(LocalDateTime.now());
        taskDao.updateById(task);

        List<IotOtaTaskDevice> devices = taskDeviceDao.list(
                new LambdaQueryWrapper<IotOtaTaskDevice>()
                        .eq(IotOtaTaskDevice::getTaskId, id)
                        .in(IotOtaTaskDevice::getStatus, 0, 1, 2, 3));
        for (IotOtaTaskDevice d : devices) {
            d.setStatus(6);
            d.setCompleteTime(LocalDateTime.now());
            taskDeviceDao.updateById(d);
        }
    }

    @Override
    public List<IotOtaTaskDevice> listTaskDevices(String taskId) {
        List<IotOtaTaskDevice> list = taskDeviceDao.list(
                new LambdaQueryWrapper<IotOtaTaskDevice>()
                        .eq(IotOtaTaskDevice::getTaskId, taskId)
                        .orderByDesc(IotOtaTaskDevice::getCreateTime));

        List<String> nullVersionIds = list.stream()
                .filter(d -> d.getCurrentVersion() == null)
                .map(IotOtaTaskDevice::getDeviceId)
                .toList();
        if (!nullVersionIds.isEmpty()) {
            Map<String, IotDevice> deviceMap = deviceDao.listByIds(nullVersionIds).stream()
                    .collect(Collectors.toMap(IotDevice::getId, d -> d));
            list.forEach(d -> {
                if (d.getCurrentVersion() == null && deviceMap.containsKey(d.getDeviceId())) {
                    d.setCurrentVersion(deviceMap.get(d.getDeviceId()).getFirmwareVersion());
                }
            });
        }

        return list;
    }

    @Override
    @Transactional
    public void updateDeviceProgress(String taskId, String deviceName, Integer progress, String status, String errorMessage) {
        IotOtaTaskDevice taskDevice = taskDeviceDao.getOneIgnoreTenant(
                new LambdaQueryWrapper<IotOtaTaskDevice>()
                        .eq(IotOtaTaskDevice::getTaskId, taskId)
                        .eq(IotOtaTaskDevice::getDeviceName, deviceName)
                        .last("LIMIT 1"));
        if (taskDevice == null) {
            log.warn("OTA进度更新: 未找到设备记录, taskId={}, deviceName={}", taskId, deviceName);
            return;
        }

        if ("success".equals(status)) {
            taskDevice.setStatus(4);
            taskDevice.setProgress(100);
            taskDevice.setErrorMessage(null);
            taskDevice.setCompleteTime(LocalDateTime.now());
        } else if ("fail".equals(status)) {
            taskDevice.setStatus(5);
            taskDevice.setErrorMessage(errorMessage);
            taskDevice.setCompleteTime(LocalDateTime.now());
        } else if ("downloading".equals(status)) {
            taskDevice.setStatus(2);
            taskDevice.setProgress(progress);
        } else if ("upgrading".equals(status)) {
            taskDevice.setStatus(3);
            taskDevice.setProgress(progress);
        }
        taskDeviceDao.updateByIdIgnoreTenant(taskDevice);

        IotOtaTask task = taskDao.listIgnoreTenant(
                new LambdaQueryWrapper<IotOtaTask>().eq(IotOtaTask::getId, taskId))
                .stream().findFirst().orElse(null);
        if (task != null) {
            LambdaQueryWrapper<IotOtaTaskDevice> qw = new LambdaQueryWrapper<IotOtaTaskDevice>()
                    .eq(IotOtaTaskDevice::getTaskId, taskId);
            long total = taskDeviceDao.listIgnoreTenant(qw).size();
            long success = taskDeviceDao.listIgnoreTenant(qw.eq(IotOtaTaskDevice::getStatus, 4)).size();
            long fail = taskDeviceDao.listIgnoreTenant(qw.eq(IotOtaTaskDevice::getStatus, 5)).size();
            task.setTotalCount((int) total);
            task.setSuccessCount((int) success);
            task.setFailCount((int) fail);
            task.setProgress(total > 0 ? (int) ((success + fail) * 100 / total) : 0);
            if (success + fail >= total) {
                task.setStatus(2);
                task.setEndTime(LocalDateTime.now());
            }
            taskDao.updateByIdIgnoreTenant(task);
        }
    }

    @Override
    @Transactional
    public void retryDevices(String taskId, List<String> deviceIds) {
        IotOtaTask task = getById(taskId);
        if (task.getStatus() != 1 && task.getStatus() != 2 && task.getStatus() != 3) {
            throw new BusinessException("当前任务状态不支持重试");
        }

        IotFirmware[] firmwareRef = new IotFirmware[1];
        TenantHelper.ignore(() -> firmwareRef[0] = firmwareDao.getById(task.getFirmwareId()));
        if (firmwareRef[0] == null) {
            throw new BusinessException("关联固件不存在");
        }
        IotFirmware firmware = firmwareRef[0];

        LambdaQueryWrapper<IotOtaTaskDevice> wrapper = new LambdaQueryWrapper<IotOtaTaskDevice>()
                .eq(IotOtaTaskDevice::getTaskId, taskId)
                .eq(IotOtaTaskDevice::getStatus, 5);
        if (deviceIds != null && !deviceIds.isEmpty()) {
            wrapper.in(IotOtaTaskDevice::getDeviceId, deviceIds);
        }
        List<IotOtaTaskDevice> failedDevices = taskDeviceDao.list(wrapper);
        if (failedDevices.isEmpty()) {
            throw new BusinessException("没有需要重试的失败设备");
        }

        String downloadUrl = firmwareService.getDownloadUrl(task.getFirmwareId());
        IotMqttConfig mqttConfig = findActiveMqttConfig();

        for (IotOtaTaskDevice td : failedDevices) {
            td.setStatus(1);
            td.setProgress(0);
            td.setErrorMessage(null);
            td.setPushTime(LocalDateTime.now());
            td.setCompleteTime(null);
            taskDeviceDao.updateById(td);

            if (mqttConfig != null) {
                try {
                    String topic = MqttTopicConstants.buildOtaUpgradeTopic(td.getProductKey(), td.getDeviceName());
                    String payload = objectMapper.writeValueAsString(java.util.Map.of(
                            "firmwareUrl", downloadUrl,
                            "firmwareVersion", firmware.getFirmwareVersion(),
                            "fileSize", firmware.getFileSize(),
                            "fileMd5", firmware.getFileMd5() != null ? firmware.getFileMd5() : "",
                            "taskId", taskId
                    ));
                    applicationContext.getBean(MqttClientManager.class).publish(mqttConfig.getId(), topic, payload, mqttConfig.getQos());
                } catch (Exception e) {
                    log.error("OTA重试推送失败: device={}", td.getDeviceName(), e);
                    td.setStatus(5);
                    td.setErrorMessage("推送失败: " + e.getMessage());
                    td.setCompleteTime(LocalDateTime.now());
                    taskDeviceDao.updateById(td);
                }
            }
        }

        if (task.getStatus() != 1) {
            task.setStatus(1);
            task.setStartTime(LocalDateTime.now());
            task.setEndTime(null);
        }
        refreshTaskStats(task);
        log.info("OTA重试: taskId={}, 重试设备数={}", taskId, failedDevices.size());
    }

    private void refreshTaskStats(IotOtaTask task) {
        LambdaQueryWrapper<IotOtaTaskDevice> qw = new LambdaQueryWrapper<IotOtaTaskDevice>()
                .eq(IotOtaTaskDevice::getTaskId, task.getId());
        long total = taskDeviceDao.count(qw);
        long success = taskDeviceDao.count(new LambdaQueryWrapper<IotOtaTaskDevice>()
                .eq(IotOtaTaskDevice::getTaskId, task.getId())
                .eq(IotOtaTaskDevice::getStatus, 4));
        long fail = taskDeviceDao.count(new LambdaQueryWrapper<IotOtaTaskDevice>()
                .eq(IotOtaTaskDevice::getTaskId, task.getId())
                .eq(IotOtaTaskDevice::getStatus, 5));
        task.setTotalCount((int) total);
        task.setSuccessCount((int) success);
        task.setFailCount((int) fail);
        task.setProgress(total > 0 ? (int) ((success + fail) * 100 / total) : 0);
        if (success + fail >= total) {
            task.setStatus(2);
            task.setEndTime(LocalDateTime.now());
        }
        taskDao.updateById(task);
    }

    private List<IotDevice> matchDevices(IotOtaTask task) {
        LambdaQueryWrapper<IotDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotDevice::getProductKey, task.getProductKey());
        wrapper.ne(IotDevice::getStatus, 3);

        if (task.getTargetType() == 1 && task.getTargetValue() != null) {
            try {
                List<String> deviceIds = objectMapper.readValue(task.getTargetValue(), new TypeReference<List<String>>() {});
                wrapper.in(IotDevice::getId, deviceIds);
            } catch (Exception e) {
                log.error("解析设备ID列表失败: {}", task.getTargetValue());
                return new ArrayList<>();
            }
        } else if (task.getTargetType() == 2 && task.getTargetValue() != null) {
            wrapper.eq(IotDevice::getFirmwareVersion, task.getTargetValue());
        }

        return deviceDao.list(wrapper);
    }

    private IotMqttConfig findActiveMqttConfig() {
        LambdaQueryWrapper<IotMqttConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotMqttConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        return mqttConfigDao.getOne(wrapper);
    }
}
