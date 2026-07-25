package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.*;
import org.lyz.iot.dao.*;
import org.lyz.iot.service.OtaTaskService;
import org.lyz.iot.service.FirmwareService;
import org.lyz.iot.mqtt.MqttClientManager;
import org.lyz.iot.mqtt.MqttTopicConstants;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.lyz.iot.mqtt.VersionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtaHandler {

    private final OtaTaskService otaTaskService;
    private final ObjectMapper objectMapper;
    private final IotDeviceDao deviceDao;
    private final IotOtaTaskDao taskDao;
    private final IotOtaTaskDeviceDao taskDeviceDao;
    private final IotFirmwareDao firmwareDao;
    private final FirmwareService firmwareService;
    private final ApplicationContext applicationContext;

    public void handleDeviceInform(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractOtaProductKey(topic);
            String deviceName = MqttTopicUtils.extractOtaDeviceName(topic);
            JsonNode params = root.get("params");
            if (params == null) return;

            String version = params.has("firmwareVersion") ? params.get("firmwareVersion").asText() : null;
            log.info("设备固件版本上报: productKey={}, deviceName={}, version={}", productKey, deviceName, version);

            if (version == null || version.isEmpty()) return;

            IotDevice device = deviceDao.getOneIgnoreTenant(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("主动拉取: 设备不存在, deviceName={}", deviceName);
                return;
            }

            checkAndPushUpgrade(device, productKey, deviceName, version);
        } catch (Exception e) {
            log.error("设备固件版本上报处理失败", e);
        }
    }

    private void checkAndPushUpgrade(IotDevice device, String productKey, String deviceName, String currentVersion) {
        try {
            List<IotOtaTask> tasks = taskDao.listIgnoreTenant(
                    new LambdaQueryWrapper<IotOtaTask>()
                            .eq(IotOtaTask::getProductKey, productKey)
                            .in(IotOtaTask::getStatus, 0, 1)
                            .orderByDesc(IotOtaTask::getCreateTime));

            if (tasks.isEmpty()) return;

            for (IotOtaTask task : tasks) {
                IotFirmware firmware = firmwareDao.getOneIgnoreTenant(
                        new LambdaQueryWrapper<IotFirmware>().eq(IotFirmware::getId, task.getFirmwareId()));
                if (firmware == null || firmware.getFirmwareVersion() == null) continue;

                String targetVersion = firmware.getFirmwareVersion();
                if (!VersionUtils.isNewer(targetVersion, currentVersion)) {
                    log.debug("主动拉取: 目标版本{}不比当前版本{}新, 跳过. task={}", targetVersion, currentVersion, task.getId());
                    continue;
                }

                IotOtaTaskDevice taskDevice = taskDeviceDao.getOneIgnoreTenant(
                        new LambdaQueryWrapper<IotOtaTaskDevice>()
                                .eq(IotOtaTaskDevice::getTaskId, task.getId())
                                .eq(IotOtaTaskDevice::getDeviceName, deviceName)
                                .last("LIMIT 1"));

                if (taskDevice == null) {
                    if (task.getTargetType() != null && task.getTargetType() == 1 && task.getTargetValue() != null) {
                        try {
                            List<String> targetDeviceIds = objectMapper.readValue(task.getTargetValue(),
                                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                            if (!targetDeviceIds.contains(device.getId())) {
                                log.debug("主动拉取: 设备{}不在任务指定列表中, 跳过. task={}", deviceName, task.getId());
                                continue;
                            }
                        } catch (Exception e) {
                            log.error("解析任务目标设备列表失败: taskId={}", task.getId());
                            continue;
                        }
                    } else if (task.getTargetType() != null && task.getTargetType() == 2 && task.getTargetValue() != null) {
                        if (!task.getTargetValue().equals(currentVersion)) {
                            log.debug("主动拉取: 设备当前版本{}不匹配目标版本{}, 跳过. task={}", currentVersion, task.getTargetValue(), task.getId());
                            continue;
                        }
                    }

                    taskDevice = new IotOtaTaskDevice();
                    taskDevice.setTaskId(task.getId());
                    taskDevice.setDeviceId(device.getId());
                    taskDevice.setDeviceName(deviceName);
                    taskDevice.setProductKey(productKey);
                    taskDevice.setCurrentVersion(currentVersion);
                    taskDevice.setTargetVersion(targetVersion);
                    taskDevice.setTenantId(device.getTenantId());
                    taskDevice.setStatus(0);
                    taskDevice.setProgress(0);
                    taskDeviceDao.save(taskDevice);
                } else {
                    if (taskDevice.getStatus() == 4 || taskDevice.getStatus() == 2 || taskDevice.getStatus() == 3) {
                        log.debug("主动拉取: 设备已在升级中或已完成, 跳过. device={}, task={}, status={}", deviceName, task.getId(), taskDevice.getStatus());
                        continue;
                    }
                }

                pushUpgradeToDevice(device, firmware, task, taskDevice);
                return;
            }
        } catch (Exception e) {
            log.error("主动拉取检查失败: device={}", deviceName, e);
        }
    }

    private void pushUpgradeToDevice(IotDevice device, IotFirmware firmware, IotOtaTask task, IotOtaTaskDevice taskDevice) {
        try {
            String downloadUrl = firmwareService.getDownloadUrl(firmware);
            IotMqttConfig mqttConfig = findActiveMqttConfig();
            if (mqttConfig == null) {
                log.warn("主动拉取: 无可用MQTT连接, taskId={}", task.getId());
                return;
            }

            String topic = MqttTopicConstants.buildOtaUpgradeTopic(device.getProductKey(), device.getDeviceName());
            String payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "firmwareUrl", downloadUrl,
                    "firmwareVersion", firmware.getFirmwareVersion(),
                    "fileSize", firmware.getFileSize() != null ? firmware.getFileSize() : 0,
                    "fileMd5", firmware.getFileMd5() != null ? firmware.getFileMd5() : "",
                    "taskId", task.getId()
            ));

            taskDevice.setStatus(1);
            taskDevice.setPushTime(LocalDateTime.now());
            taskDeviceDao.updateByIdIgnoreTenant(taskDevice);

            if (task.getStatus() == 0) {
                task.setStatus(1);
                task.setStartTime(LocalDateTime.now());
                taskDao.updateByIdIgnoreTenant(task);
            }

            applicationContext.getBean(MqttClientManager.class).publish(mqttConfig.getId(), topic, payload, mqttConfig.getQos());
            log.info("主动拉取: 升级已推送. device={}, task={}, targetVersion={}", device.getDeviceName(), task.getId(), firmware.getFirmwareVersion());
        } catch (Exception e) {
            log.error("主动拉取推送失败: device={}, task={}", device.getDeviceName(), task.getId(), e);
            taskDevice.setStatus(5);
            taskDevice.setErrorMessage("推送失败: " + e.getMessage());
            taskDevice.setCompleteTime(LocalDateTime.now());
            taskDeviceDao.updateByIdIgnoreTenant(taskDevice);
        }
    }

    public void handleDeviceProgress(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractOtaProductKey(topic);
            String deviceName = MqttTopicUtils.extractOtaDeviceName(topic);

            String taskId = root.has("id") ? root.get("id").asText() : null;
            Integer progress = root.has("progress") ? root.get("progress").asInt() : 0;
            String status = root.has("status") ? root.get("status").asText() : "";
            String errorMessage = root.has("message") ? root.get("message").asText() : null;

            if (taskId != null && !taskId.isEmpty()) {
                otaTaskService.updateDeviceProgress(taskId, deviceName, progress, status, errorMessage);
            }

            log.info("OTA进度上报: device={}/{}, progress={}, status={}", productKey, deviceName, progress, status);
        } catch (Exception e) {
            log.error("OTA进度上报处理失败", e);
        }
    }

    public void handleDeviceDownload(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractOtaProductKey(topic);
            String deviceName = MqttTopicUtils.extractOtaDeviceName(topic);

            String taskId = root.has("id") ? root.get("id").asText() : null;
            String status = root.has("status") ? root.get("status").asText() : "";

            if (taskId != null && !taskId.isEmpty()) {
                otaTaskService.updateDeviceProgress(taskId, deviceName, 0, "downloading".equals(status) ? "downloading" : status, null);
            }

            log.info("OTA下载状态上报: device={}/{}, status={}", productKey, deviceName, status);
        } catch (Exception e) {
            log.error("OTA下载状态上报处理失败", e);
        }
    }

    private IotMqttConfig findActiveMqttConfig() {
        LambdaQueryWrapper<IotMqttConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotMqttConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        return applicationContext.getBean(IotMqttConfigDao.class).listIgnoreTenant(wrapper)
                .stream().findFirst().orElse(null);
    }
}
