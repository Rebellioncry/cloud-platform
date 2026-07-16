package org.lyz.emqx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.lyz.emqx.entity.EmqxAclDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmqxAclDeviceMapper extends BaseMapper<EmqxAclDevice> {
}
