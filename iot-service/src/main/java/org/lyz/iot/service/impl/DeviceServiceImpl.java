package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.DeviceDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mapper.mysql.IotProductMapper;
import org.lyz.iot.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final IotDeviceMapper deviceMapper;
    private final IotProductMapper productMapper;

    @Override
    public PageResult<IotDevice> list(int page, int size, String productId, String name, Integer status) {
        Page<IotDevice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotDevice> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(IotDevice::getProductId, productId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotDevice::getDeviceName, name);
        }
        if (status != null) {
            wrapper.eq(IotDevice::getStatus, status);
        }
        wrapper.orderByDesc(IotDevice::getCreateTime);
        IPage<IotDevice> result = deviceMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public IotDevice getById(String id) {
        IotDevice device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        return device;
    }

    @Override
    @Transactional
    public IotDevice create(DeviceDTO dto) {
        IotProduct product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        IotDevice device = new IotDevice();
        device.setProductId(dto.getProductId());
        device.setProductKey(product.getProductKey());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceKey(dto.getDeviceKey() != null && !dto.getDeviceKey().isEmpty()
                ? dto.getDeviceKey() : generateDeviceKey());
        device.setNickname(dto.getNickname());
        device.setFirmwareVersion(dto.getFirmwareVersion());
        device.setTags(dto.getTags());
        device.setParentDeviceId(dto.getParentDeviceId());
        device.setStatus(0);
        deviceMapper.insert(device);
        return device;
    }

    @Override
    @Transactional
    public void update(String id, DeviceDTO dto) {
        IotDevice device = getById(id);
        boolean active = device.getStatus() != null && device.getStatus() != 0 && device.getStatus() != 3;
        if (active && (dto.getDeviceName() != null || dto.getDeviceKey() != null)) {
            throw new BusinessException("设备运行中，无法修改设备标识信息");
        }
        if (dto.getDeviceName() != null) device.setDeviceName(dto.getDeviceName());
        if (dto.getDeviceKey() != null) device.setDeviceKey(dto.getDeviceKey());
        if (dto.getNickname() != null) device.setNickname(dto.getNickname());
        if (dto.getFirmwareVersion() != null) device.setFirmwareVersion(dto.getFirmwareVersion());
        if (dto.getTags() != null) device.setTags(dto.getTags());
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional
    public void delete(String id) {
        deviceMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void activate(String id) {
        IotDevice device = getById(id);
        device.setStatus(1);
        device.setLastOnlineTime(LocalDateTime.now());
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional
    public void disable(String id) {
        IotDevice device = getById(id);
        device.setStatus(3);
        deviceMapper.updateById(device);
    }

    private String generateDeviceKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
