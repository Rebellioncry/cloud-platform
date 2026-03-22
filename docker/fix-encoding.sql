-- Fix character encoding for Chinese text
-- Convert from wrong encoding to correct UTF-8

USE cloud_platform;

-- Fix tenant table
UPDATE sys_tenant SET 
    tenant_name = CONVERT(BINARY(CONVERT(tenant_name USING latin1)) USING utf8mb4),
    contact = CONVERT(BINARY(CONVERT(contact USING latin1)) USING utf8mb4)
WHERE tenant_name IS NOT NULL;

-- Fix user table
UPDATE sys_user SET 
    nickname = CONVERT(BINARY(CONVERT(nickname USING latin1)) USING utf8mb4)
WHERE nickname IS NOT NULL;

-- Fix role table
UPDATE sys_role SET 
    role_name = CONVERT(BINARY(CONVERT(role_name USING latin1)) USING utf8mb4),
    remark = CONVERT(BINARY(CONVERT(remark USING latin1)) USING utf8mb4)
WHERE role_name IS NOT NULL;

-- Fix menu table
UPDATE sys_menu SET 
    menu_name = CONVERT(BINARY(CONVERT(menu_name USING latin1)) USING utf8mb4),
    icon = CONVERT(BINARY(CONVERT(icon USING latin1)) USING utf8mb4)
WHERE menu_name IS NOT NULL;
