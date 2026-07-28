package org.lyz.system.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.system.dto.TenantDTO;
import org.lyz.system.dto.UserDTO;

import java.util.Map;

public interface PlatformService {

    PageResult<Map<String, Object>> listTenants(int page, int size);

    Map<String, Object> getTenantDetail(String id);

    void createTenantWithAdmin(TenantDTO tenantDto, UserDTO adminDto);

    void createTenantAdmin(String tenantId, UserDTO adminDto);

    void updateTenantStatus(String tenantId, Integer status);

    Map<String, Object> impersonateTenant(String tenantId);

    void returnFromImpersonate();
}
