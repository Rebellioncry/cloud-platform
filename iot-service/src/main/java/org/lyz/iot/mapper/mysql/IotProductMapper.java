package org.lyz.iot.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lyz.iot.entity.IotProduct;

@Mapper
public interface IotProductMapper extends BaseMapper<IotProduct> {
}
