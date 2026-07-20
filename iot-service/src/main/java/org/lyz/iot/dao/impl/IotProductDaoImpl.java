package org.lyz.iot.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.iot.dao.IotProductDao;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.mapper.mysql.IotProductMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IotProductDaoImpl extends ServiceImpl<IotProductMapper, IotProduct> implements IotProductDao {
}
