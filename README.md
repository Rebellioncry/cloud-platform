# Cloud Platform 微服务平台

基于 Spring Cloud + Nacos 的微服务架构平台，支持多租户、IoT 设备管理、MQTT 通信、审计日志、第三方登录（微信、QQ、钉钉）。

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS |
| Spring Boot | 3.5.12 | 核心框架 |
| Spring Cloud | 2025.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2025.0.0.0 | Nacos 集成 |
| Nacos | 3.1.1 | 服务发现 / 配置中心 |
| Sa-Token | 1.45.0 | 认证授权 (JWT + Redis) |
| MyBatis-Plus | 3.5.16 | ORM |
| Knife4j | 4.3.0 | API 文档 |
| MySQL | 8.0 | 关系数据库 |
| TDengine | 3.4.1 | 时序数据库 (IoT) |
| Redis | 7.x | 缓存 / 会话 |
| MongoDB | 6.x | 审计日志存储 |
| EMQX | 5.8.6 | MQTT Broker |
| Vue 3 | - | 前端 (Composition API + Element Plus) |

## 模块结构

```
cloud-platform/
├── common/              # 公共模块 (实体、工具、配置)
├── gateway/             # API 网关 (8080)
├── auth-service/        # 认证服务 (8081)
├── system-service/      # 系统管理 (8082)
├── resource-service/    # 资源服务 (8083)
├── sms-service/         # 短信服务 (8084)
├── iot-service/         # IoT 服务 (8090)
├── emqx-auth/           # EMQX HTTP 认证 (8085)
├── web/                 # 前端 (Vue 3)
├── nacos-config/        # Nacos 配置文件
└── docker/              # Docker Compose + 初始化脚本
```

## 中间件 (Docker Compose)

| 服务 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| MySQL | cloud-mysql | 3306 | 主数据库 |
| Redis | cloud-redis | 6379 | 缓存/会话 |
| MongoDB | cloud-mongo | 27017 | 审计日志 |
| Nacos | cloud-nacos | 8848 | 注册/配置中心 |
| EMQX | cloud-emqx | 1883/18083 | MQTT Broker |
| TDengine | cloud-tdengine | 6030/6041 | 时序数据库 |

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.9+
- Podman / Docker + Compose

### 2. 启动中间件

```bash
cd docker
podman-compose up -d
```

### 3. 推送 Nacos 配置

```bash
# Windows PowerShell
.\push-config.ps1

# Linux/Mac
bash push-config.sh
```

配置文件位于 `nacos-config/` 目录。

### 4. 编译项目

```bash
mvn clean install -DskipTests
```

### 5. 启动服务 (按顺序)

1. **gateway** — API 网关
2. **auth-service** — 认证服务
3. **system-service** — 系统管理
4. **iot-service** — IoT 服务
5. **emqx-auth** — EMQX 认证服务

### 6. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8080 | 前端 / API 网关 |
| http://localhost:8848/nacos | Nacos 控制台 (nacos/nacos) |
| http://localhost:18083 | EMQX Dashboard (admin/public) |
| http://localhost:8080/doc.html | Knife4j API 文档 |

### 默认账号

- 用户名: `admin`
- 密码: `123456`
- 角色: SUPER_ADMIN (超级管理员，不限租户)

## 功能模块

### 系统管理
- 用户管理 — CRUD、角色分配、租户绑定
- 角色管理 — CRUD、菜单权限分配
- 菜单管理 — 树形结构、权限标识
- 租户管理 — 多租户隔离
- 审计日志 — 操作记录 (MongoDB)

### IoT 平台
- 产品管理 — 产品定义、物模型 (Thing Model)
- 设备管理 — 设备注册、状态管理、在线/离线
- 设备详情 — 影子状态、功能调用、下发指令
- MQTT 配置 — EMQX 连接管理
- IoT 看板 — 设备统计概览

### 认证授权
- 账号密码登录
- 验证码登录 (短信/邮箱)
- 第三方登录 (微信/QQ/钉钉，需配置 JustAuth)
- Sa-Token JWT + Redis 会话

## IoT 架构

```
设备 → EMQX (MQTT) → IoT Service (指令下发)
                    → TDengine (时序数据存储)
                    → MySQL (设备/产品元数据)
```

- MQTT Topic: `sys/{productKey}/{deviceName}/thing/...`
- 设备认证: EMQX HTTP Auth → emqx-auth 服务 → MySQL 校验 device_key
- 设备 clientId: `{clientIdPrefix}-{UUID}`

## 数据库

- 初始化脚本: `docker/init.sql`
- TDengine 初始化: `docker/tdengine-init.sh`
- 多租户通过 `tenant_id` 字段隔离
- 超级管理员 (admin) 跳过租户过滤

## 项目约定

- 所有 `@RequestParam` / `@PathVariable` 必须显式指定 `value` 属性
- 所有 ID 为 String (UUID)，外键字段也是 String
- Lombok 注解处理器配置在父 POM 的 `annotationProcessorPaths`
- 前端使用 Vue 3 Composition API + `<script setup>` 语法
- 菜单从数据库动态加载，前端不硬编码路由
