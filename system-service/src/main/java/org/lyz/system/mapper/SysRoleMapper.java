package org.lyz.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lyz.system.entity.SysRole;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    void deleteRoleMenus(@Param("roleId") Long roleId);
    void insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
