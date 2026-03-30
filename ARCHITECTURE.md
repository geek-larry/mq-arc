# 许可管理系统 - 消息队列架构设计

## 1. 系统架构概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              服务端 (Server)                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   REST API   │  │ Msg Publish  │  │   Response   │  │   Web UI     │     │
│  │  Controller  │  │   Service    │  │  Listener    │  │   (可选)      │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────────────┘     │
│         │                 │                 │                               │
│         └─────────────────┴─────────────────┘                               │
│                           │                                                 │
└───────────────────────────┼─────────────────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │   RocketMQ    │
                    │  消息队列集群  │
                    └───────┬───────┘
                            │
┌───────────────────────────┼─────────────────────────────────────────────────┐
│                           │               客户端 (Client)                    │
│  ┌──────────────┐  ┌──────▼───────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Topic      │  │   Message    │  │  Response    │  │   License    │     │
│  │  Listeners   │  │   Handler    │  │   Send       │  │   Server     │     │
│  │  (5 Topics)  │  │   Chain      │  │   Service    │  │   Adapter    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 2. Topic 设计

### 2.1 Topic 粒度划分

| Topic | 说明 | 适用场景 |
|-------|------|----------|
| `license-user-mgmt` | 浮动许可用户管理 | 踢出用户操作 |
| `license-service-mgmt` | 许可服务管理 | 启动/停止/重启服务 |
| `license-whitelist-mgmt` | 许可白名单管理 | 初始化/添加/删除/查询 |
| `license-file-mgmt` | 许可文件及日志管理 | 查询/更新/下载文件 |
| `license-monitor` | 许可监控数据采集 | 使用情况监控 |
| `license-response` | 响应消息Topic | 客户端返回处理结果 |

### 2.2 Tag 设计规范

格式: `{softwareType}:{operation}`

```
示例:
- flexnet:kickout    - FlexNet踢出用户
- sentinel:start     - Sentinel启动服务
- lmx:query          - LMX查询白名单
- *:usage            - 所有软件类型的监控采集
```

### 2.3 通配符支持

- `flexnet:*` - 订阅FlexNet的所有操作
- `*:start` - 订阅所有软件的启动操作
- `*:*` - 订阅所有消息

## 3. BusinessKey 规范

### 3.1 格式

```
{softwareType}:{hostname}:{operation}:{timestamp}:{shortUuid}

示例:
flexnet:server01:kickout:1699123456789:abc123de
```

### 3.2 组成说明

| 字段 | 说明 | 示例 |
|------|------|------|
| softwareType | 软件类型 | flexnet, sentinel, lmx |
| hostname | 目标服务器主机名 | server01, license-host-01 |
| operation | 操作类型 | kickout, start, query |
| timestamp | 时间戳(毫秒) | 1699123456789 |
| shortUuid | 短UUID(8位) | abc123de |

## 4. 消息模型

### 4.1 请求消息 (LicenseMessage)

```java
{
  "messageId": "uuid",
  "businessKey": "flexnet:server01:kickout:1699123456789:abc123de",
  "correlationId": "uuid-for-request-response-mapping",
  "hostname": "server01",
  "softwareType": "FLEXNET",
  "operationType": "KICKOUT",
  "topic": "license-user-mgmt",
  "tag": "flexnet:kickout",
  "status": "PENDING",
  "createTime": "2024-01-15T10:30:00",
  "payload": { ... },
  "retryCount": 0
}
```

### 4.2 响应消息 (LicenseResponse)

```java
{
  "responseId": "uuid",
  "requestMessageId": "uuid",
  "correlationId": "uuid-for-request-response-mapping",
  "businessKey": "flexnet:server01:kickout:1699123456789:abc123de",
  "status": "SUCCESS",
  "data": { ... },
  "processTimeMillis": 250,
  "processedBy": "server01",
  "responseTime": "2024-01-15T10:30:01"
}
```

## 5. 模块结构

```
license-mq-system/
├── license-common/                 # 公共模块
│   ├── constant/                   # 常量定义
│   ├── enums/                      # 枚举类
│   ├── message/                    # 消息模型
│   ├── payload/                    # 消息载荷
│   ├── exception/                  # 异常类
│   └── util/                       # 工具类
│
├── license-server/                 # 服务端
│   ├── config/                     # 配置类
│   ├── controller/                 # REST API
│   ├── service/                    # 业务服务
│   │   ├── MessagePublishService   # 消息发布服务
│   │   └── LicenseMgmtService      # 许可管理服务
│   └── listener/                   # 响应监听器
│
└── license-client/                 # 客户端
    ├── config/                     # 配置类
    ├── handler/                    # 消息处理器
    │   ├── MessageHandler          # 处理器接口
    │   ├── AbstractMessageHandler  # 抽象处理器
    │   └── impl/                   # 具体处理器实现
    └── listener/                   # 消息监听器
        ├── LicenseMessageListener  # 消息监听器
        └── ResponseSendService     # 响应发送服务
```

## 6. 监控数据采集优化

### 6.1 问题场景
- 60个监控对象
- 每5分钟采集一次
- 数据量较大

### 6.2 优化方案

1. **批量采集**
   - 使用 `batchMode=true` 一次性采集多个监控对象
   - 减少消息数量，降低MQ压力

2. **批量发送**
   - 使用 `syncSendBatch` 批量发送消息
   - 减少网络往返次数

3. **增加消费线程**
   - 监控Topic配置 `consumeThreadNumber = 20`
   - 提高并发处理能力

4. **异步采集**
   - 使用 `sendAsync` 异步发送监控请求
   - 不阻塞服务端主流程

5. **数据压缩**
   - 大消息可启用压缩传输
   - 减少网络带宽占用

## 7. 异常处理机制

### 7.1 异常类型

| 异常类 | 说明 | 处理策略 |
|--------|------|----------|
| LicenseException | 基础异常 | 统一处理 |
| MessagePublishException | 消息发布异常 | 重试3次后失败 |
| MessageProcessException | 消息处理异常 | 返回错误响应 |
| LicenseServiceException | 许可服务异常 | 记录日志并告警 |

### 7.2 重试机制

```java
// 消息发布失败重试
producer.setRetryTimesWhenSendFailed(3);
producer.setRetryTimesWhenSendAsyncFailed(3);

// 消息处理失败重试
message.markAsRetrying();
if (message.shouldRetry(MAX_RETRY_TIMES)) {
    // 重新发送或延迟重试
}
```

## 8. 扩展性设计

### 8.1 新增软件类型

1. 在 `SoftwareType` 枚举中添加新类型
2. 处理器自动支持（使用通配符匹配）
3. 无需修改其他代码

### 8.2 新增操作类型

1. 在 `OperationType` 枚举中添加新操作
2. 创建对应的处理器实现 `MessageHandler`
3. 注册为Spring Bean

### 8.3 新增Topic

1. 在 `LicenseConstants` 中添加Topic常量
2. 创建对应的监听器类
3. 在 `MultiTopicListenerConfig` 中注册

## 9. 部署建议

### 9.1 RocketMQ 部署

```yaml
# 生产环境建议
- 部署2个NameServer节点
- 每个Broker主从部署
- 开启消息持久化
- 配置消息过期策略
```

### 9.2 客户端部署

```yaml
# 每个许可服务器部署一个客户端
- 配置唯一hostname
- 配置RocketMQ地址
- 配置消费组名（建议包含hostname）
```

## 10. API 示例

### 10.1 踢出用户

```bash
POST /api/license/flexnet/server01/users/kickout
Content-Type: application/json

{
  "userId": "user001",
  "username": "张三",
  "sessionId": "sess123456",
  "featureCode": "CAD_FEATURE",
  "reason": "管理员强制踢出",
  "force": true
}
```

### 10.2 采集监控数据

```bash
POST /api/license/flexnet/server01/monitor/usage
Content-Type: application/json

{
  "targets": [
    {"softwareName": "AutoCAD", "featureCode": "ACAD", "version": "2024"},
    {"softwareName": "SolidWorks", "featureCode": "SW", "version": "2024"}
  ],
  "batchMode": true,
  "collectTimestamp": 1699123456789
}
```
