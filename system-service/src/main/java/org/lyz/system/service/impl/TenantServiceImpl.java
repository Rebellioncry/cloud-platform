package org.lyz.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.system.dto.TenantDTO;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.mapper.SysTenantMapper;
import org.lyz.system.service.TenantService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final SysTenantMapper tenantMapper;

    @Override
    public List<SysTenant> list() {
        return tenantMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public TenantDTO getById(Long id) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        return toDTO(tenant);
    }

    @Override
    public void create(TenantDTO dto) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantCode, dto.getTenantCode());
        if (tenantMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("租户编码已存在");
        }
        SysTenant tenant = toEntity(dto);
        tenantMapper.insert(tenant);
    }

    @Override
    public void update(TenantDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("租户ID不能为空");
        }
        SysTenant tenant = toEntity(dto);
        tenantMapper.updateById(tenant);
    }

    @Override
    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }

    private TenantDTO toDTO(SysTenant tenant) {
        TenantDTO dto = new TenantDTO();
        dto.setId(tenant.getId());
        dto.setTenantCode(tenant.getTenantCode());
        dto.setTenantName(tenant.getTenantName());
        dto.setContact(tenant.getContact());
        dto.setMobile(tenant.getMobile());
        dto.setEmail(tenant.getEmail());
        dto.setStatus(tenant.getStatus());
        dto.setExpireTime(tenant.getExpireTime());
        dto.setRemark(tenant.getRemark());
        return dto;
    }

    private SysTenant toEntity(TenantDTO dto) {
        SysTenant tenant = new SysTenant();
        tenant.setId(dto.getId());
        tenant.setTenantCode(dto.getTenantCode());
        tenant.setTenantName(dto.getTenantName());
        tenant.setContact(dto.getContact());
        tenant.setMobile(dto.getMobile());
        tenant.setEmail(dto.getEmail());
        tenant.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        tenant.setExpireTime(dto.getExpireTime());
        tenant.setRemark(dto.getRemark());
        return tenant;
    }
}
