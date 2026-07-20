package org.lyz.auth.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.auth.dao.SysUserDao;
import org.lyz.auth.mapper.SysUserMapper;
import org.lyz.common.core.entity.SysUser;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysUserDaoImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserDao {

    @Override
    public List<String> selectRoleCodesByUserId(String userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }
}
