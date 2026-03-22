# Cloud Platform Web

基于 Vue 3 + Element Plus 的前端管理系统

## 技术栈

- Vue 3.5
- Vite 6
- Element Plus 2.9
- Pinia (状态管理)
- Vue Router 4
- Axios

## 功能模块

- 登录/登出
- 首页仪表盘
- 用户管理 (CRUD)
- 角色管理 (CRUD)
- 菜单管理 (CRUD)
- 租户管理 (CRUD)

## 安装运行

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build
```

## 项目结构

```
src/
├── api/              # API 接口
├── components/       # 公共组件
├── router/          # 路由配置
├── stores/          # Pinia 状态管理
├── utils/           # 工具函数
└── views/          # 页面组件
    ├── layout/     # 布局组件
    ├── login/      # 登录页面
    └── system/     # 系统管理页面
```

## 配置说明

前端 API 代理到后端网关，默认配置：

- 后端地址: http://localhost:8080
- 前端端口: 3000

可在 `vite.config.js` 中修改代理配置。
