package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.context.TenantHelper;
import org.lyz.common.core.context.UserContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.DeviceDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.dao.IotProductDao;
import org.lyz.iot.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final IotDeviceDao deviceDao;
    private final IotProductDao productDao;

    @Override
    public PageResult<IotDevice> list(int page, int size, String productId, String name, Integer status) {
        Page<IotDevice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotDevice> wrapper = new LambdaQueryWrapper<>();
        if (!UserContext.isPlatformAdmin()) {
            String tenantId = UserContext.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                wrapper.eq(IotDevice::getTenantId, tenantId);
            }
        }
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
        IPage<IotDevice>[] resultRef = new IPage[1];
        TenantHelper.ignore(() -> resultRef[0] = deviceDao.page(pageParam, wrapper));
        return PageResult.of(resultRef[0].getTotal(), page, size, resultRef[0].getRecords());
    }

    @Override
    public IotDevice getById(String id) {
        IotDevice[] ref = new IotDevice[1];
        TenantHelper.ignore(() -> ref[0] = deviceDao.getById(id));
        if (ref[0] == null) {
            throw new BusinessException("设备不存在");
        }
        return ref[0];
    }

    @Override
    @Transactional
    public IotDevice create(DeviceDTO dto) {
        IotProduct[] productRef = new IotProduct[1];
        TenantHelper.ignore(() -> productRef[0] = productDao.getById(dto.getProductId()));
        if (productRef[0] == null) {
            throw new BusinessException("产品不存在");
        }
        IotProduct product = productRef[0];
        IotDevice device = new IotDevice();
        String tenantId = UserContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            device.setTenantId(tenantId);
        }
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
        deviceDao.save(device);
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
        deviceDao.updateById(device);
    }

    @Override
    @Transactional
    public void delete(String id) {
        IotDevice device = getById(id);
        if (device.getStatus() != null && device.getStatus() != 3) {
            throw new BusinessException("设备未禁用，无法删除。请先禁用设备");
        }
        deviceDao.removeById(id);
    }

    @Override
    @Transactional
    public void activate(String id) {
        IotDevice device = getById(id);
        device.setStatus(1);
        device.setLastOnlineTime(LocalDateTime.now());
        deviceDao.updateById(device);
    }

    @Override
    @Transactional
    public void disable(String id) {
        IotDevice device = getById(id);
        device.setStatus(3);
        deviceDao.updateById(device);
    }

    private String generateDeviceKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
