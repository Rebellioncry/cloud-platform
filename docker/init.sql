-- Cloud Platform Database Initialization Script

CREATE DATABASE IF NOT EXISTS cloud_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cloud_platform;

-- Tenant Table
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT NOT NULL COMMENT '主键',
    tenant_code VARCHAR(50) NOT NULL COMMENT '租户编码',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租户名称',
    contact VARCHAR(50) COMMENT '联系人',
    mobile VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态(0禁用1正常)',
    expire_time DATETIME COMMENT '过期时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- User Table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL COMMENT '主键',
    tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    mobile VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态(0禁用1正常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username_tenant (username, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Role Table
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL COMMENT '主键',
    tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态(0禁用1正常)',
    data_scope TINYINT DEFAULT 1 COMMENT '数据权限(1全部2本部门及以下3本部门4仅本人5自定义)',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code_tenant (role_code, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- Menu Table
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL COMMENT '主键',
    tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_type TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型(0目录1菜单2按钮)',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(255) COMMENT '组件路径',
    icon VARCHAR(100) COMMENT '图标',
    perms VARCHAR(100) COMMENT '权限标识',
    order_num INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否显示(0否1是)',
    status TINYINT DEFAULT 1 COMMENT '状态(0禁用1正常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    version INT DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- User Role Relation
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- Role Menu Relation
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- Social Login Binding Table
CREATE TABLE IF NOT EXISTS sys_social (
    id BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    platform VARCHAR(20) NOT NULL COMMENT '平台(wetchat/qq/dingtalk)',
    openid VARCHAR(100) NOT NULL COMMENT '第三方OpenId',
    unionid VARCHAR(100) COMMENT '微信UnionId',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_openid_tenant (platform, openid, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方登录绑定表';

-- Insert Default Tenant
INSERT INTO sys_tenant (id, tenant_code, tenant_name, contact, mobile, email, status)
VALUES (1, 'DEFAULT', '默认租户', '管理员', '13800138000', 'admin@example.com', 1);

-- Insert Default Admin User (password: 123456)
INSERT INTO sys_user (id, tenant_id, username, password, nickname, email, mobile, status)
VALUES (1, 1, 'admin', '123456', '管理员', 'admin@example.com', '13800138000', 1);

-- Insert Default Role
INSERT INTO sys_role (id, tenant_id, role_code, role_name, role_sort, status, data_scope)
VALUES (1, 1, 'SUPER_ADMIN', '超级管理员', 1, 1, 1);

-- Insert Default Menus
INSERT INTO sys_menu (id, parent_id, menu_type, menu_name, path, component, icon, perms, order_num) VALUES
(1, 0, 0, '系统管理', '/system', NULL, 'Setting', '', 1),
(2, 1, 1, '用户管理', '/system/user', 'system/user/index', 'User', 'system:user:list', 1),
(3, 1, 1, '角色管理', '/system/role', 'system/role/index', 'Role', 'system:role:list', 2),
(4, 1, 1, '菜单管理', '/system/menu', 'system/menu/index', 'Menu', 'system:menu:list', 3),
(5, 1, 1, '租户管理', '/system/tenant', 'system/tenant/index', 'OfficeBuilding', 'system:tenant:list', 4),
(10, 0, 0, 'Dashboard', '/dashboard', 'dashboard/index', 'HomeFilled', '', 0);

-- Assign Admin to Role
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- Assign All Menus to Super Admin
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE tenant_id = 1;
