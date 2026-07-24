package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.FileStorageDTO;
import org.lyz.iot.entity.IotFileStorage;
import org.lyz.iot.dao.IotFileStorageDao;
import org.lyz.iot.service.FileStorageService;
import org.lyz.iot.storage.FileStorageFactory;
import org.lyz.iot.storage.MinioFileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final IotFileStorageDao fileStorageDao;

    @Override
    public PageResult<IotFileStorage> list(int page, int size, String name) {
        Page<IotFileStorage> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotFileStorage> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotFileStorage::getName, name);
        }
        wrapper.orderByDesc(IotFileStorage::getCreateTime);
        IPage<IotFileStorage> result = fileStorageDao.page(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public IotFileStorage getById(String id) {
        IotFileStorage storage = fileStorageDao.getById(id);
        if (storage == null) {
            throw new BusinessException("存储配置不存在");
        }
        return storage;
    }

    @Override
    @Transactional
    public IotFileStorage create(FileStorageDTO dto) {
        IotFileStorage storage = new IotFileStorage();
        storage.setName(dto.getName());
        storage.setStorageType(dto.getStorageType());
        storage.setLocalPath(dto.getLocalPath());
        storage.setEndpoint(dto.getEndpoint());
        storage.setAccessKey(dto.getAccessKey());
        storage.setSecretKey(dto.getSecretKey());
        storage.setBucket(dto.getBucket());
        storage.setRegion(dto.getRegion());
        storage.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        fileStorageDao.save(storage);
        return storage;
    }

    @Override
    @Transactional
    public void update(String id, FileStorageDTO dto) {
        IotFileStorage storage = getById(id);
        if (dto.getName() != null) storage.setName(dto.getName());
        if (dto.getStorageType() != null) storage.setStorageType(dto.getStorageType());
        if (dto.getLocalPath() != null) storage.setLocalPath(dto.getLocalPath());
        if (dto.getEndpoint() != null) storage.setEndpoint(dto.getEndpoint());
        if (dto.getAccessKey() != null) storage.setAccessKey(dto.getAccessKey());
        if (dto.getSecretKey() != null) storage.setSecretKey(dto.getSecretKey());
        if (dto.getBucket() != null) storage.setBucket(dto.getBucket());
        if (dto.getRegion() != null) storage.setRegion(dto.getRegion());
        if (dto.getIsDefault() != null) storage.setIsDefault(dto.getIsDefault());
        fileStorageDao.updateById(storage);
        FileStorageFactory.removeCache(id);
    }

    @Override
    @Transactional
    public void delete(String id) {
        getById(id);
        fileStorageDao.removeById(id);
        FileStorageFactory.removeCache(id);
    }

    @Override
    public boolean testConnection(String id) {
        IotFileStorage storage = getById(id);
        MinioFileStorage minio = (MinioFileStorage) FileStorageFactory.getStorage(storage);
        return minio.testConnection();
    }
}
