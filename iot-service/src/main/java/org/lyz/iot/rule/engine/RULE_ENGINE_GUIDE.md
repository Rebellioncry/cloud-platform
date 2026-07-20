# IoT 规则引擎使用说明

## 概述

规则引擎采用可视化流程编排，通过拖拽节点、连线来定义设备数据的处理逻辑。支持设备事件触发、条件判断、数据转换、多种输出目标。

## 规则链路

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  设备事件    │────▶│  条件/处理   │────▶│  输出节点    │
│  触发器      │     │  节点       │     │  (Sink)     │
└─────────────┘     └─────────────┘     └─────────────┘
```

一条规则至少包含：一个**触发器** + 一个**输出节点**，中间可以添加多个处理节点。

---

## 节点说明

### 触发器

| 节点 | 说明 | 配置项 |
|------|------|--------|
| 📡 设备事件 | 监听设备上报数据 | `产品Key`: 留空匹配所有设备；`事件类型`: 全部/属性上报/事件上报 |

### 处理节点

| 节点 | 说明 | 配置项 |
|------|------|--------|
| ⚡ 条件判断 | Aviator 表达式，结果为 true 放行，false 停止 | `条件表达式` |
| 📝 脚本处理 | Aviator 表达式，处理数据 | `脚本` |
| 🔄 数据转换 | 字段提取 + 表达式计算 | `源字段`、`目标字段`、`转换表达式` |
| 📋 日志输出 | 打印日志 | `日志级别`、`日志消息` |
| 🔔 告警通知 | 生成告警记录 | `告警级别`、`告警标题`、`告警内容` |

### 输出节点（Sink）

| 节点 | 说明 | 配置项 |
|------|------|--------|
| 📡 MQTT | 发布到 MQTT Broker | `Broker`、`端口`、`Topic`、`用户名`、`密码`、`QoS`、`KeepAlive` |
| 🌐 HTTP请求 | 发送 HTTP 请求 | `URL`、`Method`、`Content-Type`、`超时`、`Token` |
| 🔴 Redis | 写入 Redis | `Host`、`端口`、`Key`、`密码`、`数据库` |
| 📨 Kafka | 发送到 Kafka（桩模式） | `Bootstrap Servers`、`Topic`、`Group ID` |
| 🚀 RocketMQ | 发送到 RocketMQ（桩模式） | `NameServer`、`Topic`、`Producer Group` |
| 🌀 Pulsar | 发送到 Pulsar（桩模式） | `Service URL`、`Topic`、`Tenant`、`Namespace` |

---

## 表达式语法（Aviator）

规则引擎使用 [Aviator](https://github.com/killme2008/aviatorscript) 作为表达式引擎。

### 可用变量

设备上报的数据会映射为以下变量：

```
productKey     — 产品标识
deviceName     — 设备名称
method         — 上报方法（如 thing.property.post）
eventType      — 事件类型（property / event 名称）
payload        — 完整上报数据（Map 结构）
```

### 访问嵌套字段

上报数据中 `params` 等嵌套结构，使用 `[]` 访问：

```
payload['params']['hr']       — 获取 hr 值
payload['params']['br']       — 获取 br 值
```

### 常用示例

#### 条件判断

```
# 判断 br 等于 30
payload['params']['br'] == 30

# 判断 hr 大于 60
payload['params']['hr'] > 60

# 组合条件
payload['params']['br'] == 30 && payload['params']['hr'] > 60

# 判断产品
productKey == "smart-band-01"
```

#### 脚本处理

```
# 计算心率是否异常
payload['params']['hr'] > 100

# 拼接字符串
"设备 " + deviceName + " 心率: " + payload['params']['hr']
```

#### 数据转换

| 字段 | 示例值 | 说明 |
|------|--------|------|
| 源字段 | `payload['params']['hr']` | 要提取的字段路径 |
| 目标字段 | `heart_rate` | 提取后存入的新字段名 |
| 转换表达式 | `sourceValue * 1.0` | 用 `sourceValue` 代表源字段值 |

#### 告警内容

```
"设备 " + deviceName + " 心率异常: " + payload['params']['hr']
```

---

## 典型场景

### 场景一：心率异常 → MQTT 输出

```
📡 设备事件 ──▶ ⚡ 条件(br==30) ──▶ 📡 MQTT输出
```

当设备上报 br=30 时，将数据转发到指定 MQTT Topic。

### 场景二：数据过滤 + 告警

```
📡 设备事件 ──▶ ⚡ 条件(hr>80) ──▶ 🔔 告警(严重) ──▶ 📋 日志
```

当心率超过 80 时触发严重告警并记录日志。

### 场景三：数据转换 + Redis 存储

```
📡 设备事件 ──▶ 🔄 数据转换 ──▶ 🔴 Redis写入
```

提取设备数据中的关键字段，转换后存储到 Redis。

---

## 操作说明

1. **拖拽节点**: 从左侧面板拖拽节点到画布
2. **连线**: 从节点的锚点（圆点）拖出连线到另一个节点
3. **配置属性**: 点击节点，在右侧面板配置属性
4. **保存**: 点击顶部「保存」按钮
5. **启动规则**: 在规则列表中点击「启动」
6. **测试触发**: 使用 API `POST /rule/{id}/test-trigger` 手动触发

## API 测试触发

```bash
curl -X POST http://localhost:8080/rule/{规则ID}/test-trigger \
  -d "productKey=你的产品Key&deviceName=你的设备名"
```
