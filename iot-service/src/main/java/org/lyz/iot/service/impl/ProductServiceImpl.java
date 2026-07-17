package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.ProductDTO;
import org.lyz.iot.dto.ProductTreeDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mapper.mysql.IotProductMapper;
import org.lyz.iot.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final IotProductMapper productMapper;
    private final IotDeviceMapper deviceMapper;

    @Override
    public PageResult<IotProduct> list(int page, int size, String name) {
        Page<IotProduct> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotProduct> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotProduct::getName, name);
        }
        wrapper.orderByDesc(IotProduct::getCreateTime);
        IPage<IotProduct> result = productMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public IotProduct getById(String id) {
        IotProduct product = productMapper.selectById(id);
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
        productMapper.insert(product);
        return product;
    }

    @Override
    @Transactional
    public void update(String id, ProductDTO dto) {
        IotProduct product = getById(id);
        if (dto.getProductKey() != null && !dto.getProductKey().equals(product.getProductKey())) {
            long count = deviceMapper.selectCount(
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
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void delete(String id) {
        productMapper.deleteById(id);
    }

    @Override
    public List<ProductTreeDTO> getTree() {
        LambdaQueryWrapper<IotProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(IotProduct::getCreateTime);
        List<IotProduct> products = productMapper.selectList(wrapper);
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
        product.setModelStatus(0);
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void publishThingModel(String id) {
        IotProduct product = getById(id);
        if (product.getThingModel() == null || product.getThingModel().isEmpty()) {
            throw new BusinessException("物模型为空，无法发布");
        }
        product.setModelStatus(1);
        productMapper.updateById(product);
    }

    private String generateProductKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
