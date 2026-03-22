package org.lyz.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lyz.system.entity.SysUserRole;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    int insertBatchSomeColumn(@Param("list") List<SysUserRole> userRoles);
}
