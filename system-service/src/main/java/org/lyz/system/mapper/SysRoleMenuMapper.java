package org.lyz.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lyz.system.entity.SysRoleMenu;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    int insertBatchSomeColumn(@Param("list") List<SysRoleMenu> roleMenus);
}
