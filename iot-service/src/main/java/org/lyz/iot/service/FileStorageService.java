package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.FileStorageDTO;
import org.lyz.iot.entity.IotFileStorage;

public interface FileStorageService {
    PageResult<IotFileStorage> list(int page, int size, String name);
    IotFileStorage getById(String id);
    IotFileStorage create(FileStorageDTO dto);
    void update(String id, FileStorageDTO dto);
    void delete(String id);
    boolean testConnection(String id);
}
