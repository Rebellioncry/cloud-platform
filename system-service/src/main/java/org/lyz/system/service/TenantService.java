package org.lyz.system.service;

import org.lyz.system.dto.TenantDTO;
import org.lyz.system.entity.SysTenant;
import org.lyz.common.core.result.PageResult;
import java.util.List;

public interface TenantService {
    PageResult<SysTenant> list(int page, int size);
    TenantDTO getById(String id);
    void create(TenantDTO dto);
    void update(TenantDTO dto);
    void delete(String id);
}
