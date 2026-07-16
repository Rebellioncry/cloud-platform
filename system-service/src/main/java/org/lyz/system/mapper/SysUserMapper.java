package org.lyz.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lyz.common.core.entity.SysUser;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    void deleteUserRoles(@Param("userId") String userId);
    void insertUserRoles(@Param("userId") String userId, @Param("roleIds") List<String> roleIds);
}
