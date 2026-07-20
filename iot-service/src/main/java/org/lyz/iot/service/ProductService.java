package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.ProductDTO;
import org.lyz.iot.dto.ProductTreeDTO;
import org.lyz.iot.entity.IotProduct;

import java.util.List;

public interface ProductService {
    PageResult<IotProduct> list(int page, int size, String name);
    IotProduct getById(String id);
    IotProduct create(ProductDTO dto);
    void update(String id, ProductDTO dto);
    void delete(String id);
    List<ProductTreeDTO> getTree();
    String getThingModel(String id);
    void updateThingModel(String id, String thingModel);
}
