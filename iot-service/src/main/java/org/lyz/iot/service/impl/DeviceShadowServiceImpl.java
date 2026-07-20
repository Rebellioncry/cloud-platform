package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.iot.dto.DeviceShadowDTO;
import org.lyz.iot.entity.IotDeviceShadow;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.dao.IotDeviceShadowDao;
import org.lyz.iot.service.DeviceShadowService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceShadowServiceImpl implements DeviceShadowService {

    private static final int MAX_RETRY = 3;

    private final IotDeviceShadowDao shadowDao;

    @Override
    public List<DeviceShadowDTO> getShadow(String deviceId) {
        LambdaQueryWrapper<IotDeviceShadow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotDeviceShadow::getDeviceId, deviceId);
        List<IotDeviceShadow> shadows = shadowDao.list(wrapper);
        return shadows.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @TenantIgnore
    @Transactional
    public void updateDesired(String deviceId, String identifier, String value) {
        for (int i = 0; i < MAX_RETRY; i++) {
            IotDeviceShadow shadow = getOrCreateShadow(deviceId, identifier);
            shadow.setDesiredValue(value);
            shadow.setDesiredVersion(shadow.getDesiredVersion() + 1);
            shadow.setDesiredTime(LocalDateTime.now());
            shadow.setUpdateTime(LocalDateTime.now());
            boolean success = shadowDao.updateById(shadow);
            if (success) {
                log.info("设备影子期望值更新: deviceId={}, identifier={}, version={}",
                        deviceId, identifier, shadow.getDesiredVersion());
                return;
            }
            log.warn("设备影子乐观锁冲突，重试 {}/{}", i + 1, MAX_RETRY);
        }
        log.error("设备影子期望值更新失败(重试耗尽): deviceId={}, identifier={}", deviceId, identifier);
    }

    @Override
    @TenantIgnore
    @Transactional
    public void updateReported(String deviceId, String identifier, String value) {
        for (int i = 0; i < MAX_RETRY; i++) {
            IotDeviceShadow shadow = getOrCreateShadow(deviceId, identifier);
            shadow.setReportedValue(value);
            shadow.setReportedVersion(shadow.getReportedVersion() + 1);
            shadow.setReportedTime(LocalDateTime.now());
            shadow.setUpdateTime(LocalDateTime.now());
            boolean success = shadowDao.updateById(shadow);
            if (success) {
                log.info("设备影子上报值更新: deviceId={}, identifier={}, version={}",
                        deviceId, identifier, shadow.getReportedVersion());
                return;
            }
            log.warn("设备影子乐观锁冲突，重试 {}/{}", i + 1, MAX_RETRY);
        }
        log.error("设备影子上报值更新失败(重试耗尽): deviceId={}, identifier={}", deviceId, identifier);
    }

    @Override
    public List<DeviceShadowDTO> getDiff(String deviceId) {
        LambdaQueryWrapper<IotDeviceShadow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotDeviceShadow::getDeviceId, deviceId);
        List<IotDeviceShadow> shadows = shadowDao.list(wrapper);
        return shadows.stream()
                .filter(s -> {
                    if (s.getDesiredValue() == null || s.getReportedValue() == null) return false;
                    return !s.getDesiredValue().equals(s.getReportedValue());
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private IotDeviceShadow getOrCreateShadow(String deviceId, String identifier) {
        LambdaQueryWrapper<IotDeviceShadow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotDeviceShadow::getDeviceId, deviceId);
        wrapper.eq(IotDeviceShadow::getPropertyIdentifier, identifier);
        IotDeviceShadow shadow = shadowDao.getOne(wrapper);
        if (shadow == null) {
            shadow = new IotDeviceShadow();
            shadow.setDeviceId(deviceId);
            shadow.setPropertyIdentifier(identifier);
            shadow.setDesiredVersion(0L);
            shadow.setReportedVersion(0L);
            shadow.setVersion(0L);
            shadow.setCreateTime(LocalDateTime.now());
            shadow.setUpdateTime(LocalDateTime.now());
            try {
                shadowDao.save(shadow);
            } catch (DuplicateKeyException e) {
                // 并发插入冲突，重新查询
                shadow = shadowDao.getOne(wrapper);
            }
        }
        return shadow;
    }

    private DeviceShadowDTO toDTO(IotDeviceShadow s) {
        DeviceShadowDTO dto = new DeviceShadowDTO();
        dto.setIdentifier(s.getPropertyIdentifier());
        dto.setDesiredValue(s.getDesiredValue());
        dto.setDesiredVersion(s.getDesiredVersion());
        dto.setDesiredTime(s.getDesiredTime());
        dto.setReportedValue(s.getReportedValue());
        dto.setReportedVersion(s.getReportedVersion());
        dto.setReportedTime(s.getReportedTime());
        boolean hasDiff = s.getDesiredValue() != null && s.getReportedValue() != null
                && !s.getDesiredValue().equals(s.getReportedValue());
        dto.setHasDiff(hasDiff);
        return dto;
    }
}
