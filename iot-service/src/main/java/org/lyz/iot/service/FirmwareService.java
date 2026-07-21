package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.FirmwareDTO;
import org.lyz.iot.entity.IotFirmware;
import org.springframework.web.multipart.MultipartFile;

public interface FirmwareService {
    PageResult<IotFirmware> list(int page, int size, String productId, String name);
    IotFirmware getById(String id);
    IotFirmware create(FirmwareDTO dto, MultipartFile file);
    void update(String id, FirmwareDTO dto);
    void delete(String id);
    void publish(String id);
    void disable(String id);
    String getDownloadUrl(String id);
}
