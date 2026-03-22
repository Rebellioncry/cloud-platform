package org.lyz.system.service;

import org.lyz.system.dto.TenantDTO;
import org.lyz.system.entity.SysTenant;
import java.util.List;

public interface TenantService {
    List<SysTenant> list();
    TenantDTO getById(Long id);
    void create(TenantDTO dto);
    void update(TenantDTO dto);
    void delete(Long id);
}
