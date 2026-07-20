package org.lyz.auth.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lyz.auth.dao.SysSocialDao;
import org.lyz.auth.entity.SysSocial;
import org.lyz.auth.mapper.SysSocialMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysSocialDaoImpl extends ServiceImpl<SysSocialMapper, SysSocial> implements SysSocialDao {
}
