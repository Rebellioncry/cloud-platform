package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.DeviceDTO;
import org.lyz.iot.entity.IotDevice;

public interface DeviceService {
    PageResult<IotDevice> list(int page, int size, String productId, String name, Integer status);
    IotDevice getById(String id);
    IotDevice create(DeviceDTO dto);
    void update(String id, DeviceDTO dto);
    void delete(String id);
    void activate(String id);
    void disable(String id);
}
