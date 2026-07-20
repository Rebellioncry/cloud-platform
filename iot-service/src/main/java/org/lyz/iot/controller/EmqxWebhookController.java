package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.result.Result;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.service.DeviceLogService;
import org.lyz.iot.entity.IotDeviceLog;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Tag(name = "EMQX Webhook")
@RestController
@RequestMapping("/emqx")
@RequiredArgsConstructor
public class EmqxWebhookController {

    private final IotDeviceDao deviceDao;
    private final DeviceLogService deviceLogService;

    @Operation(summary = "设备上下线回调(EMQX Webhook)")
    @PostMapping("/device-status")
    public Result<Void> deviceStatus(@RequestBody Map<String, Object> payload) {
        String event = (String) payload.get("event");
        String clientId = (String) payload.get("clientid");
        String username = (String) payload.get("username");

        log.info("EMQX Webhook: event={}, clientId={}, username={}", event, clientId, username);

        if (username == null) {
            log.warn("EMQX Webhook: username为空, 忽略");
            return Result.success(null);
        }

        IotDevice device = deviceDao.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IotDevice>()
                        .eq(IotDevice::getDeviceName, username)
                        .last("LIMIT 1"));

        if (device == null) {
            log.warn("未找到设备: deviceName={}", username);
            return Result.success(null);
        }

        if ("client.connected".equals(event)) {
            device.setStatus(1);
            device.setLastOnlineTime(LocalDateTime.now());
            if (payload.containsKey("peername")) {
                String peername = (String) payload.get("peername");
                if (peername != null && peername.contains(":")) {
                    device.setIpAddress(peername.substring(0, peername.lastIndexOf(":")));
                }
            }
            deviceDao.updateById(device);
            saveDeviceLog(device, "online", "设备上线", null);
            log.info("设备上线: {}", device.getDeviceName());
        } else if ("client.disconnected".equals(event)) {
            device.setStatus(2);
            deviceDao.updateById(device);
            String reason = (String) payload.get("reason");
            saveDeviceLog(device, "offline", "设备离线: " + (reason != null ? reason : ""), null);
            log.info("设备离线: {}", device.getDeviceName());
        }

        return Result.success(null);
    }

    private void saveDeviceLog(IotDevice device, String logType, String content, String messageId) {
        IotDeviceLog deviceLog = new IotDeviceLog();
        deviceLog.setTs(LocalDateTime.now());
        deviceLog.setLogType(logType);
        deviceLog.setContent(content);
        deviceLog.setMessageId(messageId);
        deviceLog.setDeviceId(String.valueOf(device.getId()));
        deviceLog.setProductKey(device.getProductKey());
        deviceLog.setDeviceName(device.getDeviceName());
        deviceLog.setTableDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        deviceLogService.save(deviceLog);
    }
}
