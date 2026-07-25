package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.FirmwareDTO;
import org.lyz.iot.entity.IotFirmware;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.entity.IotFileStorage;
import org.lyz.iot.dao.IotFirmwareDao;
import org.lyz.iot.dao.IotProductDao;
import org.lyz.iot.dao.IotFileStorageDao;
import org.lyz.iot.service.FirmwareService;
import org.lyz.iot.storage.FileStorageFactory;
import org.lyz.iot.storage.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirmwareServiceImpl implements FirmwareService {

    private final IotFirmwareDao firmwareDao;
    private final IotProductDao productDao;
    private final IotFileStorageDao fileStorageDao;

    @Override
    public PageResult<IotFirmware> list(int page, int size, String productId, String name) {
        Page<IotFirmware> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotFirmware> wrapper = new LambdaQueryWrapper<>();
        if (productId != null && !productId.isEmpty()) {
            wrapper.eq(IotFirmware::getProductId, productId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotFirmware::getFirmwareName, name);
        }
        wrapper.orderByDesc(IotFirmware::getCreateTime);
        IPage<IotFirmware> result = firmwareDao.page(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public IotFirmware getById(String id) {
        IotFirmware firmware = firmwareDao.getById(id);
        if (firmware == null) {
            throw new BusinessException("固件不存在");
        }
        return firmware;
    }

    @Override
    @Transactional
    public IotFirmware create(FirmwareDTO dto, MultipartFile file) {
        IotProduct product = productDao.getById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        IotFileStorage storage = fileStorageDao.getById(dto.getStorageId());
        if (storage == null) {
            throw new BusinessException("存储配置不存在");
        }

        try {
            String ext = "";
            String origName = file.getOriginalFilename();
            if (origName != null && origName.contains(".")) {
                ext = origName.substring(origName.lastIndexOf("."));
            }
            String dateDir = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String objectName = dateDir + "/" + (origName != null ? origName : UUID.randomUUID().toString().replace("-", "") + ext);

            FileStorage fileStorage = FileStorageFactory.getStorage(storage);
            String md5 = calcMd5(file);
            try (InputStream is = file.getInputStream()) {
                fileStorage.upload(storage.getBucket(), objectName, is, file.getSize(), file.getContentType());
            }

            IotFirmware firmware = new IotFirmware();
            firmware.setProductId(dto.getProductId());
            firmware.setProductKey(product.getProductKey());
            firmware.setFirmwareName(dto.getFirmwareName());
            firmware.setFirmwareVersion(dto.getFirmwareVersion());
            firmware.setDescription(dto.getDescription());
            firmware.setStorageId(dto.getStorageId());
            firmware.setFilePath(objectName);
            firmware.setFileName(origName != null ? origName : "unknown");
            firmware.setFileSize(file.getSize());
            firmware.setFileMd5(md5);
            firmwareDao.save(firmware);
            return firmware;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("固件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void update(String id, FirmwareDTO dto) {
        IotFirmware firmware = getById(id);
        if (dto.getFirmwareName() != null) firmware.setFirmwareName(dto.getFirmwareName());
        if (dto.getFirmwareVersion() != null) firmware.setFirmwareVersion(dto.getFirmwareVersion());
        if (dto.getDescription() != null) firmware.setDescription(dto.getDescription());
        firmwareDao.updateById(firmware);
    }

    @Override
    @Transactional
    public void delete(String id) {
        getById(id);
        firmwareDao.removeById(id);
    }

    @Override
    public String getDownloadUrl(String id) {
        IotFirmware firmware = getById(id);
        return getDownloadUrl(firmware);
    }

    @Override
    public String getDownloadUrl(IotFirmware firmware) {
        IotFileStorage storage = fileStorageDao.getOneIgnoreTenant(
                new LambdaQueryWrapper<IotFileStorage>().eq(IotFileStorage::getId, firmware.getStorageId()));
        if (storage == null) {
            throw new BusinessException("存储配置不存在");
        }
        FileStorage fileStorage = FileStorageFactory.getStorage(storage);
        return fileStorage.getPresignedUrl(storage.getBucket(), firmware.getFilePath(), 86400);
    }

    private String calcMd5(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
