package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.ProductDTO;
import org.lyz.iot.dto.ProductTreeDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.dao.IotProductDao;
import org.lyz.iot.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final IotProductDao productDao;
    private final IotDeviceDao deviceDao;

    @Override
    public PageResult<IotProduct> list(int page, int size, String name) {
        Page<IotProduct> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotProduct> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotProduct::getName, name);
        }
        wrapper.orderByDesc(IotProduct::getCreateTime);
        IPage<IotProduct> result = productDao.page(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public IotProduct getById(String id) {
        IotProduct product = productDao.getById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        return product;
    }

    @Override
    @Transactional
    public IotProduct create(ProductDTO dto) {
        IotProduct product = new IotProduct();
        product.setParentId(dto.getParentId());
        product.setProductKey(dto.getProductKey() != null && !dto.getProductKey().isEmpty()
                ? dto.getProductKey() : generateProductKey());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setNodeType(dto.getNodeType() != null ? dto.getNodeType() : 0);
        product.setProtocol(dto.getProtocol() != null ? dto.getProtocol() : "MQTT");
        product.setDataFormat(dto.getDataFormat() != null ? dto.getDataFormat() : 0);
        product.setModelStatus(0);
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        productDao.save(product);
        return product;
    }

    @Override
    @Transactional
    public void update(String id, ProductDTO dto) {
        IotProduct product = getById(id);
        if (dto.getProductKey() != null && !dto.getProductKey().equals(product.getProductKey())) {
            long count = deviceDao.count(
                    new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getProductId, id));
            if (count > 0) {
                throw new BusinessException("该产品下存在设备，无法修改产品密钥");
            }
            product.setProductKey(dto.getProductKey());
        }
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getNodeType() != null) product.setNodeType(dto.getNodeType());
        if (dto.getProtocol() != null) product.setProtocol(dto.getProtocol());
        if (dto.getStatus() != null) {
            boolean wasEnabled = product.getStatus() != null && product.getStatus() == 1;
            boolean nowDisabled = dto.getStatus() == 0;
            product.setStatus(dto.getStatus());
            if (wasEnabled && nowDisabled) {
                deviceDao.update(null,
                    new LambdaUpdateWrapper<IotDevice>()
                        .eq(IotDevice::getProductId, id)
                        .ne(IotDevice::getStatus, 3)
                        .set(IotDevice::getStatus, 3));
            }
        }
        productDao.updateById(product);
    }

    @Override
    @Transactional
    public void delete(String id) {
        IotProduct product = getById(id);
        if (product.getStatus() != null && product.getStatus() == 1) {
            throw new BusinessException("产品已启用，无法删除。请先禁用产品");
        }
        long deviceCount = deviceDao.count(
                new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getProductId, id));
        if (deviceCount > 0) {
            throw new BusinessException("该产品下存在 " + deviceCount + " 个设备，请先删除所有设备");
        }
        productDao.removeById(id);
    }

    @Override
    public List<ProductTreeDTO> getTree() {
        LambdaQueryWrapper<IotProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(IotProduct::getCreateTime);
        List<IotProduct> products = productDao.list(wrapper);
        return buildTree(products, null);
    }

    private List<ProductTreeDTO> buildTree(List<IotProduct> products, String parentId) {
        return products.stream()
                .filter(p -> (parentId == null ? p.getParentId() == null : parentId.equals(p.getParentId())))
                .map(p -> {
                    ProductTreeDTO dto = new ProductTreeDTO();
                    dto.setId(p.getId());
                    dto.setParentId(p.getParentId());
                    dto.setProductKey(p.getProductKey());
                    dto.setName(p.getName());
                    dto.setNodeType(p.getNodeType());
                    dto.setStatus(p.getStatus());
                    dto.setChildren(buildTree(products, p.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getThingModel(String id) {
        IotProduct product = getById(id);
        return product.getThingModel();
    }

    @Override
    @Transactional
    public void updateThingModel(String id, String thingModel) {
        IotProduct product = getById(id);
        product.setThingModel(thingModel);
        productDao.updateById(product);
    }

    private String generateProductKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
