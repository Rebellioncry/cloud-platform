SET NAMES utf8mb4;

-- =============================================
-- Nacos Config Database
-- =============================================
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nacos_config;

CREATE TABLE IF NOT EXISTS config_info (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    data_id varchar(255) NOT NULL,
    group_id varchar(128) DEFAULT NULL,
    content longtext NOT NULL,
    md5 varchar(32) DEFAULT NULL,
    gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user text,
    src_ip varchar(50) DEFAULT NULL,
    app_name varchar(128) DEFAULT NULL,
    tenant_id varchar(128) DEFAULT '',
    c_desc varchar(256) DEFAULT NULL,
    c_use varchar(64) DEFAULT NULL,
    effect varchar(64) DEFAULT NULL,
    type varchar(64) DEFAULT NULL,
    c_schema text,
    encrypted_data_key varchar(1024) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    UNIQUE KEY uk_configinfo_datagrouptenant (data_id, group_id, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS config_info_gray (
    id bigint unsigned NOT NULL AUTO_INCREMENT,
    data_id varchar(255) NOT NULL,
    group_id varchar(128) NOT NULL,
    content longtext NOT NULL,
    md5 varchar(32) DEFAULT NULL,
    src_user text,
    src_ip varchar(100) DEFAULT NULL,
    gmt_create datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    gmt_modified datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    app_name varchar(128) DEFAULT NULL,
    tenant_id varchar(128) DEFAULT '',
    gray_name varchar(128) NOT NULL,
    gray_rule text NOT NULL,
    encrypted_data_key varchar(256) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    UNIQUE KEY uk_configinfogray (data_id, group_id, tenant_id, gray_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS config_tags_relation (
    id bigint(20) NOT NULL,
    tag_name varchar(128) NOT NULL,
    tag_type varchar(64) DEFAULT NULL,
    data_id varchar(255) NOT NULL,
    group_id varchar(128) NOT NULL,
    tenant_id varchar(128) DEFAULT '',
    nid bigint(20) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (nid),
    UNIQUE KEY uk_configtagrelation (id, tag_name, tag_type),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_capacity (
    id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    group_id varchar(128) NOT NULL DEFAULT '',
    quota int(10) unsigned NOT NULL DEFAULT '0',
    `usage` int(10) unsigned NOT NULL DEFAULT '0',
    max_size int(10) unsigned NOT NULL DEFAULT '0',
    max_aggr_count int(10) unsigned NOT NULL DEFAULT '0',
    max_aggr_size int(10) unsigned NOT NULL DEFAULT '0',
    max_history_count int(10) unsigned NOT NULL DEFAULT '0',
    gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS his_config_info (
    id bigint(20) unsigned NOT NULL,
    nid bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    data_id varchar(255) NOT NULL,
    group_id varchar(128) NOT NULL,
    app_name varchar(128) DEFAULT NULL,
    content longtext NOT NULL,
    md5 varchar(32) DEFAULT NULL,
    gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    src_user text,
    src_ip varchar(50) DEFAULT NULL,
    op_type char(10) DEFAULT NULL,
    tenant_id varchar(128) DEFAULT '',
    encrypted_data_key varchar(1024) NOT NULL DEFAULT '',
    publish_type varchar(50) DEFAULT 'formal',
    gray_name varchar(50) DEFAULT NULL,
    ext_info longtext DEFAULT NULL,
    PRIMARY KEY (nid),
    KEY idx_gmt_create (gmt_create),
    KEY idx_gmt_modified (gmt_modified),
    KEY idx_did (data_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_capacity (
    id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    tenant_id varchar(128) NOT NULL DEFAULT '',
    quota int(10) unsigned NOT NULL DEFAULT '0',
    `usage` int(10) unsigned NOT NULL DEFAULT '0',
    max_size int(10) unsigned NOT NULL DEFAULT '0',
    max_aggr_count int(10) unsigned NOT NULL DEFAULT '0',
    max_aggr_size int(10) unsigned NOT NULL DEFAULT '0',
    max_history_count int(10) unsigned NOT NULL DEFAULT '0',
    gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tenant_info (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    kp varchar(128) NOT NULL,
    tenant_id varchar(128) DEFAULT '',
    tenant_name varchar(128) DEFAULT '',
    tenant_desc varchar(256) DEFAULT NULL,
    create_source varchar(32) DEFAULT NULL,
    gmt_create bigint(20) NOT NULL,
    gmt_modified bigint(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_info_kptenantid (kp, tenant_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    username varchar(50) NOT NULL,
    password varchar(500) NOT NULL,
    enabled tinyint(1) NOT NULL,
    PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
    username varchar(50) NOT NULL,
    role varchar(50) NOT NULL,
    UNIQUE KEY uk_user_role (username, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
    role varchar(50) NOT NULL,
    resource varchar(128) NOT NULL,
    action varchar(8) NOT NULL,
    UNIQUE KEY uk_role_permission (role, resource, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);
INSERT INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');

-- =============================================
-- Cloud Platform Database
-- =============================================
CREATE DATABASE IF NOT EXISTS cloud_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cloud_platform;

CREATE TABLE IF NOT EXISTS sys_tenant (
    id VARCHAR(32) NOT NULL,
    tenant_code VARCHAR(50) NOT NULL,
    tenant_name VARCHAR(100) NOT NULL,
    contact VARCHAR(50) DEFAULT NULL,
    mobile VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    package_id VARCHAR(32) DEFAULT NULL COMMENT '租户套餐ID',
    status TINYINT DEFAULT 1,
    expire_time DATETIME DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_tenant_package (
    id VARCHAR(32) NOT NULL,
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    menu_ids TEXT DEFAULT NULL COMMENT '关联的菜单ID(逗号分隔)',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常 0停用',
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    tenant_scope VARCHAR(20) NOT NULL DEFAULT 'TENANT' COMMENT 'PLATFORM=平台用户, TENANT=租户用户',
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    mobile VARCHAR(20) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username_tenant (username, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    scope VARCHAR(20) NOT NULL DEFAULT 'TENANT' COMMENT 'PLATFORM=平台角色, TENANT=租户角色',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '系统内置角色,不可删除',
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    data_scope TINYINT DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code_tenant (role_code, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_menu (
    id VARCHAR(32) NOT NULL,
    scope VARCHAR(20) NOT NULL DEFAULT 'TENANT' COMMENT 'PLATFORM=平台菜单, TENANT=租户菜单',
    parent_id VARCHAR(32) DEFAULT '0',
    menu_type TINYINT NOT NULL DEFAULT 1,
    menu_name VARCHAR(50) NOT NULL,
    path VARCHAR(200) DEFAULT NULL,
    component VARCHAR(255) DEFAULT NULL,
    icon VARCHAR(100) DEFAULT NULL,
    perms VARCHAR(100) DEFAULT NULL,
    order_num INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) NOT NULL,
    menu_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_social (
    id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    platform VARCHAR(20) NOT NULL,
    openid VARCHAR(100) NOT NULL,
    unionid VARCHAR(100) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_openid_tenant (platform, openid, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default data (short string IDs)
SET @admin_user_id = '2';
SET @admin_role_id = '3';
SET @menu_platform = '01';
SET @menu_platform_tenant = '02';
SET @menu_platform_package = '03';
SET @menu_dashboard = '15';
SET @menu_iot = '20';
SET @menu_iot_product = '21';
SET @menu_iot_device = '22';
SET @menu_iot_mqtt = '23';
SET @menu_iot_rule = '24';
SET @menu_iot_storage = '25';
SET @menu_iot_firmware = '26';
SET @menu_iot_ota = '27';
SET @menu_system = '10';
SET @menu_user = '11';
SET @menu_role = '12';
SET @menu_menu = '13';
SET @menu_tenant = '14';
SET @menu_audit = '16';
SET @pkg_default = 'P1';

INSERT INTO sys_tenant_package (id, package_name, menu_ids, status, remark)
VALUES (@pkg_default, 'IoT标准套餐', '15,20,21,22,23,24,25,26,27,10,11,12,13,14,16', 1, '包含IoT平台全部功能及系统管理');

INSERT INTO sys_user (id, tenant_id, tenant_scope, username, password, nickname, email, mobile, status)
VALUES (@admin_user_id, '', 'PLATFORM', 'admin', '123456', 'Admin', 'admin@example.com', '13800138000', 1);

INSERT INTO sys_role (id, tenant_id, scope, is_system, role_code, role_name, role_sort, status, data_scope)
VALUES (@admin_role_id, '', 'PLATFORM', 1, 'SUPER_ADMIN', '超级管理员', 1, 1, 1);

INSERT INTO sys_menu (id, scope, parent_id, menu_type, menu_name, path, component, icon, perms, order_num) VALUES
-- Platform menus (scope=PLATFORM)
(@menu_platform, 'PLATFORM', '0', 0, '平台管理', '/platform', NULL, 'Monitor', '', 0),
(@menu_platform_tenant, 'PLATFORM', @menu_platform, 1, '租户管理', '/platform/tenant', 'system/tenant/index', 'OfficeBuilding', 'platform:tenant:list', 1),
(@menu_platform_package, 'PLATFORM', @menu_platform, 1, '租户套餐管理', '/platform/package', 'platform/package/index', 'PriceTag', 'platform:package:list', 2),
-- Dashboard (scope=TENANT, shared)
(@menu_dashboard, 'TENANT', '0', 0, '首页', '/dashboard', 'dashboard/index', 'HomeFilled', '', 0),
-- IoT menus (scope=TENANT)
(@menu_iot, 'TENANT', '0', 0, 'IoT平台', '/iot', NULL, 'Monitor', '', 1),
(@menu_iot_product, 'TENANT', @menu_iot, 1, '产品管理', '/iot/product', 'iot/product/index', 'Box', 'iot:product:list', 1),
(@menu_iot_device, 'TENANT', @menu_iot, 1, '设备管理', '/iot/device', 'iot/device/index', 'Cpu', 'iot:device:list', 2),
(@menu_iot_mqtt, 'TENANT', @menu_iot, 1, 'MQTT配置', '/iot/mqtt', 'iot/mqtt/index', 'Connection', 'iot:mqtt:list', 3),
(@menu_iot_rule, 'TENANT', @menu_iot, 1, '规则引擎', '/iot/rule', 'iot/rule/index', 'Filter', 'iot:rule:list', 4),
(@menu_iot_storage, 'TENANT', @menu_iot, 1, '文件存储', '/iot/storage', 'iot/storage/index', 'FolderOpened', 'iot:storage:list', 5),
(@menu_iot_firmware, 'TENANT', @menu_iot, 1, '固件管理', '/iot/firmware', 'iot/firmware/index', 'Upload', 'iot:firmware:list', 6),
(@menu_iot_ota, 'TENANT', @menu_iot, 1, 'OTA升级', '/iot/ota', 'iot/ota/index', 'Promotion', 'iot:ota:list', 7),
-- System menus (scope=TENANT)
(@menu_system, 'TENANT', '0', 0, '系统管理', '/system', NULL, 'Setting', '', 2),
(@menu_user, 'TENANT', @menu_system, 1, '用户管理', '/system/user', 'system/user/index', 'User', 'system:user:list', 1),
(@menu_role, 'TENANT', @menu_system, 1, '角色管理', '/system/role', 'system/role/index', 'UserFilled', 'system:role:list', 2),
(@menu_menu, 'TENANT', @menu_system, 1, '菜单管理', '/system/menu', 'system/menu/index', 'Grid', 'system:menu:list', 3),
(@menu_tenant, 'TENANT', @menu_system, 1, '租户管理', '/system/tenant', 'system/tenant/index', 'OfficeBuilding', 'system:tenant:list', 4),
(@menu_audit, 'TENANT', @menu_system, 1, '审计日志', '/system/audit', 'system/audit/index', 'Document', 'system:audit:list', 5);

INSERT INTO sys_user_role (id, user_id, role_id)
VALUES ('20', @admin_user_id, @admin_role_id);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
-- Platform admin sees platform menus
('50', @admin_role_id, @menu_platform),
('51', @admin_role_id, @menu_platform_tenant),
('46', @admin_role_id, @menu_platform_package),
-- Platform admin also sees all tenant menus (for management)
('30', @admin_role_id, @menu_dashboard),
('31', @admin_role_id, @menu_iot),
('32', @admin_role_id, @menu_iot_product),
('33', @admin_role_id, @menu_iot_device),
('34', @admin_role_id, @menu_iot_mqtt),
('41', @admin_role_id, @menu_iot_rule),
('42', @admin_role_id, @menu_iot_storage),
('43', @admin_role_id, @menu_iot_firmware),
('44', @admin_role_id, @menu_iot_ota),
('35', @admin_role_id, @menu_system),
('36', @admin_role_id, @menu_user),
('37', @admin_role_id, @menu_role),
('38', @admin_role_id, @menu_menu),
('39', @admin_role_id, @menu_tenant),
('40', @admin_role_id, @menu_audit);

-- IoT Product Table
CREATE TABLE IF NOT EXISTS iot_product (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    parent_id VARCHAR(32) DEFAULT NULL,
    product_key VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    node_type TINYINT DEFAULT 0,
    protocol VARCHAR(20) DEFAULT 'MQTT',
    data_format TINYINT DEFAULT 0,
    thing_model JSON DEFAULT NULL,
    model_status TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_key (product_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT Device Table
CREATE TABLE IF NOT EXISTS iot_device (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    product_id VARCHAR(32) NOT NULL,
    product_key VARCHAR(20) NOT NULL,
    parent_device_id VARCHAR(32) DEFAULT NULL,
    device_name VARCHAR(100) NOT NULL,
    device_key VARCHAR(64) NOT NULL,
    nickname VARCHAR(100) DEFAULT NULL,
    status TINYINT DEFAULT 0,
    ip_address VARCHAR(50) DEFAULT NULL,
    firmware_version VARCHAR(50) DEFAULT NULL,
    tags JSON DEFAULT NULL,
    last_online_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_key (device_key),
    KEY idx_product_id (product_id),
    KEY idx_tenant_product (tenant_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT Device Shadow Table
CREATE TABLE IF NOT EXISTS iot_device_shadow (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    device_id VARCHAR(32) NOT NULL,
    property_identifier VARCHAR(64) NOT NULL,
    desired_value JSON DEFAULT NULL,
    desired_version BIGINT DEFAULT 0,
    desired_time DATETIME DEFAULT NULL,
    reported_value JSON DEFAULT NULL,
    reported_version BIGINT DEFAULT 0,
    reported_time DATETIME DEFAULT NULL,
    metadata JSON DEFAULT NULL,
    version BIGINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shadow_device_prop (device_id, property_identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT Device Command Table
CREATE TABLE IF NOT EXISTS iot_device_command (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    device_id VARCHAR(32) NOT NULL,
    command_type VARCHAR(20) NOT NULL,
    identifier VARCHAR(64) NOT NULL,
    input_data JSON DEFAULT NULL,
    output_data JSON DEFAULT NULL,
    status TINYINT DEFAULT 0,
    request_id VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_command_device (device_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT Rule Engine Table
CREATE TABLE IF NOT EXISTS iot_rule (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    rule_type TINYINT DEFAULT 0,
    rule_model JSON DEFAULT NULL,
    status TINYINT DEFAULT 0,
    match_count BIGINT DEFAULT 0,
    last_execute_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT Rule Execution Log Table
CREATE TABLE IF NOT EXISTS iot_rule_exec_log (
    id VARCHAR(32) NOT NULL,
    rule_id VARCHAR(32) NOT NULL,
    rule_name VARCHAR(100) DEFAULT NULL,
    node_id VARCHAR(128) DEFAULT NULL,
    node_name VARCHAR(128) DEFAULT NULL,
    node_type VARCHAR(50) DEFAULT NULL,
    status TINYINT DEFAULT 0 COMMENT '0成功 1失败',
    input_data JSON DEFAULT NULL,
    output_data JSON DEFAULT NULL,
    error_message TEXT DEFAULT NULL,
    duration BIGINT DEFAULT 0 COMMENT '执行耗时(毫秒)',
    device_key VARCHAR(200) DEFAULT NULL,
    product_key VARCHAR(200) DEFAULT NULL,
    execute_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_rule_id (rule_id),
    KEY idx_execute_time (execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT MQTT Config Table
CREATE TABLE IF NOT EXISTS iot_mqtt_config (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200) DEFAULT NULL,
    broker VARCHAR(200) NOT NULL,
    port INT DEFAULT 1883,
    username VARCHAR(100) DEFAULT NULL,
    password VARCHAR(100) DEFAULT NULL,
    client_id_prefix VARCHAR(50) DEFAULT 'iot-service',
    shared_group VARCHAR(50) DEFAULT 'iot-service',
    qos INT DEFAULT 1,
    keep_alive INT DEFAULT 60,
    auto_reconnect TINYINT DEFAULT 1,
    use_ssl TINYINT DEFAULT 0,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT File Storage Table
CREATE TABLE IF NOT EXISTS iot_file_storage (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    name VARCHAR(100) NOT NULL,
    storage_type TINYINT NOT NULL COMMENT '0本地 1MinIO',
    local_path VARCHAR(500) DEFAULT NULL,
    endpoint VARCHAR(200) DEFAULT NULL,
    access_key VARCHAR(200) DEFAULT NULL,
    secret_key VARCHAR(200) DEFAULT NULL,
    bucket VARCHAR(100) DEFAULT NULL,
    region VARCHAR(50) DEFAULT NULL,
    is_default TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT Firmware Table
CREATE TABLE IF NOT EXISTS iot_firmware (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    product_id VARCHAR(32) NOT NULL,
    product_key VARCHAR(20) NOT NULL,
    firmware_name VARCHAR(200) NOT NULL,
    firmware_version VARCHAR(50) NOT NULL,
    description VARCHAR(1000) DEFAULT NULL,
    storage_id VARCHAR(32) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT DEFAULT 0,
    file_md5 VARCHAR(64) DEFAULT NULL,
    signature VARCHAR(500) DEFAULT NULL,
    status TINYINT DEFAULT 0 COMMENT '0未发布 1已发布 2已禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT OTA Task Table
CREATE TABLE IF NOT EXISTS iot_ota_task (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    task_name VARCHAR(100) NOT NULL,
    firmware_id VARCHAR(32) NOT NULL,
    product_id VARCHAR(32) NOT NULL,
    product_key VARCHAR(20) NOT NULL,
    target_type TINYINT NOT NULL COMMENT '0产品全量 1指定设备 2按版本',
    target_value TEXT DEFAULT NULL,
    total_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    fail_count INT DEFAULT 0,
    progress INT DEFAULT 0,
    status TINYINT DEFAULT 0 COMMENT '0待执行 1执行中 2已完成 3已取消',
    start_time DATETIME DEFAULT NULL,
    end_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_firmware (firmware_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IoT OTA Task Device Table
CREATE TABLE IF NOT EXISTS iot_ota_task_device (
    id VARCHAR(32) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL DEFAULT '',
    task_id VARCHAR(32) NOT NULL,
    device_id VARCHAR(32) NOT NULL,
    device_name VARCHAR(100) NOT NULL,
    product_key VARCHAR(20) NOT NULL,
    current_version VARCHAR(50) DEFAULT NULL,
    target_version VARCHAR(50) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0待升级 1推送中 2下载中 3升级中 4成功 5失败 6已取消',
    progress INT DEFAULT 0,
    error_message VARCHAR(500) DEFAULT NULL,
    push_time DATETIME DEFAULT NULL,
    complete_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_task (task_id),
    KEY idx_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

