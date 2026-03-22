# Cloud Platform 微服务平台

## 项目简介

基于 Spring Cloud + Nacos 的微服务架构平台，支持多租户、SSO单点登录、第三方登录（微信、QQ、钉钉）。

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Maven | 3.9.9 | 构建工具 |
| Spring Boot | 3.5.12 | 核心框架 |
| Spring Cloud | 2025.0.3 | 微服务框架 |
| Spring Cloud Alibaba | 2025.0.0.0 | 阿里组件 |
| Nacos | 3.0.3 | 服务发现/配置中心 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.x | 缓存/会话 |
| MongoDB | 6.x | 日志存储 |
| Sa-Token | 1.45.0 | 认证授权 |
| JustAuth | 1.16.7 | 第三方登录 |
| MyBatis-Plus | 3.5.16 | ORM |

## 模块结构

```
cloud-platform/
├── common/              # 公共模块
├── gateway/             # 网关服务 (8080)
├── auth-service/        # 认证服务 (8081)
├── system-service/      # 系统服务 (8082)
├── resource-service/    # 资源服务 (8083)
├── nacos-config/        # Nacos 配置文件
└── docker/              # Docker 配置
```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.9+
- Docker & Docker Compose

### 2. 启动中间件

```bash
cd docker
docker-compose up -d
```

### 3. 编译项目

```bash
mvn clean install -DskipTests
```

### 4. 启动服务

按顺序启动：
1. gateway
2. auth-service
3. system-service
4. resource-service

### 5. 访问服务

- 网关地址: http://localhost:8080
- Nacos 控制台: http://localhost:8848/nacos (nacos/nacos)

## 默认账号

- 用户名: admin
- 密码: 123456

## API 接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 用户登录 |
| POST | /auth/logout | 登出 |
| GET | /auth/userinfo | 获取用户信息 |

### 系统管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /system/tenant/list | 租户列表 |
| POST | /system/tenant | 新增租户 |
| PUT | /system/tenant | 修改租户 |
| DELETE | /system/tenant/{id} | 删除租户 |
| GET | /system/user/list | 用户列表 |
| POST | /system/user | 新增用户 |
| PUT | /system/user | 修改用户 |
| DELETE | /system/user/{id} | 删除用户 |
| GET | /system/role/list | 角色列表 |
| POST | /system/role | 新增角色 |
| PUT | /system/role | 修改角色 |
| DELETE | /system/role/{id} | 删除角色 |
| GET | /system/menu/list | 菜单列表 |
| GET | /system/menu/tree | 菜单树 |
| POST | /system/menu | 新增菜单 |
| PUT | /system/menu | 修改菜单 |
| DELETE | /system/menu/{id} | 删除菜单 |

## 第三方登录配置

在 Nacos 配置中心或环境变量中配置：

```yaml
justauth:
  clients:
    WECHAT:
      client-id: your-app-id
      client-secret: your-app-secret
      redirect-uri: http://localhost:8080/auth/social/wetchat/callback
    QQ:
      client-id: your-app-id
      client-secret: your-app-secret
      redirect-uri: http://localhost:8080/auth/social/qq/callback
    DINGTALK:
      client-id: your-app-id
      client-secret: your-app-secret
      redirect-uri: http://localhost:8080/auth/social/dingtalk/callback
```

## 数据库

数据库初始化脚本位于 `docker/init.sql`

## License

MIT
