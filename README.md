# 许可管理系统 - MQTT架构设计

## 1. 系统架构概览

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              服务端 (Server)                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                       │
│  │   REST API   │  │ Msg Publish  │  │   Business   │                       │
│  │  Controller  │  │   Service    │  │   Service    │                       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                       │
│         │                 │                 │                               │
│         └─────────────────┴─────────────────┘                               │
│                           │                                                 │
│                    MQTT Client (license-server)                             │
└───────────────────────────┼─────────────────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │   EMQX Broker  │
                    │  (P2P Routing) │
                    └───────┬───────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼───────┐   ┌───────▼───────┐   ┌───────▼───────┐
│  Client 01    │   │  Client 02    │   │  Client 03    │
│ (hostname-01) │   │ (hostname-02) │   │ (hostname-03) │
│               │   │               │   │               │
│ MQTT Client   │   │ MQTT Client   │   │ MQTT Client   │
│ Message       │   │ Message       │   │ Message       │
│ Handler       │   │ Handler       │   │ Handler       │
└───────────────┘   └───────────────┘   └───────────────┘
```

### 1.2 EMQX P2P通信架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        EMQX P2P通信流程 ($deliver模式)                        │
└─────────────────────────────────────────────────────────────────────────────┘

发送方 (Server)                EMQX Broker                  接收方 (Client)
┌──────────────┐              ┌──────────────┐              ┌──────────────┐
│              │              │              │              │              │
│  发布消息到   │              │  1.识别$deliver│              │              │
│  $deliver/   │──────────────▶  前缀        │              │              │
│  client-01/  │              │              │              │              │
│  license/    │              │  2.提取目标   │              │              │
│  kickout     │              │  clientId    │              │              │
│              │              │              │              │              │
│              │              │  3.去掉前缀   │              │              │
│              │              │  得到:       │              │              │
│              │              │  license/    │              │              │
│              │              │  kickout     │              │              │
│              │              │              │              │              │
│              │              │  4.直接投递   │              │  订阅:       │
│              │              │  给clientId  │──────────────▶  license/#  │
│              │              │  =client-01  │              │              │
│              │              │              │              │  收到:       │
│              │              │              │              │  license/    │
│              │              │              │              │  kickout     │
└──────────────┘              └──────────────┘              └──────────────┘

关键特性:
✓ 不经过路由表查找
✓ 直接根据clientId投递
✓ 更高效、更可靠
✓ 真正的P2P通信
```

## 2. EMQX配置

### 2.1 ACL配置

EMQX的ACL（访问控制列表）配置，确保客户端可以正常订阅和发布消息：

```bash
# EMQX 5.3.2 ACL配置示例

# 允许所有客户端订阅license/#主题
acl.rule.1 = {allow, all, subscribe, ["license/#"]}

# 允许所有客户端发布$deliver/#主题
acl.rule.2 = {allow, all, publish, ["$deliver/#"]}

# 允许所有客户端连接
acl.rule.3 = {allow, all, connect, []}
```

### 2.2 EMQX配置文件 (emqx.conf)

```properties
# 监听器配置
listeners.tcp.default = 1883
listeners.ssl.default = 8883

# 允许匿名访问（开发环境）
allow_anonymous = true

# 最大连接数
max_connections = 1024000

# 最大订阅数
max_subscriptions = 0

# 消息大小限制
max_packet_size = 1MB

# 保持连接时间
keepalive_multiplier = 1.5

# 会话过期时间
session_expiry_interval = 2h

# 消息队列长度
max_mqueue_len = 10000
```

### 2.3 EMQX Dashboard配置

访问EMQX Dashboard进行可视化配置：

```
URL: http://localhost:18083
默认账号: admin
默认密码: public

配置步骤:
1. 访问控制 -> ACL -> 添加规则
2. 允许所有客户端订阅 license/#
3. 允许所有客户端发布 $deliver/#
4. 保存并应用配置
```

## 3. Topic设计

### 3.1 Topic结构

#### 发送方 (Server)
```
格式: $deliver/{targetClientId}/license/{operation}

示例:
- $deliver/client-01/license/kickout     # 踢出用户
- $deliver/client-01/license/start       # 启动服务
- $deliver/client-01/license/query       # 查询状态
```

#### 接收方 (Client)
```
订阅格式: license/#

说明:
- 使用$deliver前缀时，接收方订阅的是去掉$deliver/{clientId}后的topic
- EMQX会自动去掉$deliver/{clientId}前缀，直接投递给目标客户端
```

#### 接收方收到的Topic
```
格式: license/{operation}

示例:
- license/kickout     # 踢出用户
- license/start       # 启动服务
- license/query       # 查询状态
```

### 3.2 Topic对比

| 特性 | 普通Topic模式 | $deliver模式 |
|------|--------------|--------------|
| 发送Topic | `license/{clientId}/{operation}` | `$deliver/{clientId}/license/{operation}` |
| 订阅Topic | `license/{clientId}/#` | `license/#` |
| 收到Topic | `license/{clientId}/{operation}` | `license/{operation}` |
| 路由方式 | 通过路由表查找 | 直接投递 |
| 性能 | 较低 | 更高 |
| 可靠性 | 一般 | 更高 |
| 适用场景 | 普通发布订阅 | P2P通信 |

### 3.3 操作类型

| 操作代码 | 说明 | Topic示例 |
|---------|------|-----------|
| `kickout` | 踢出用户 | `license/kickout` |
| `start` | 启动服务 | `license/start` |
| `stop` | 停止服务 | `license/stop` |
| `restart` | 重启服务 | `license/restart` |
| `query` | 查询状态 | `license/query` |
| `monitor` | 监控数据 | `license/monitor` |

## 4. 消息模型

### 4.1 请求消息 (LicenseMessage)

```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "businessKey": "flexnet:client-01:kickout:1699123456789:abc123de",
  "operationType": "KICKOUT",
  "softwareType": "FLEXNET",
  "hostname": "license-server",
  "payload": {
    "userId": "user001",
    "username": "testuser",
    "hostId": "host123",
    "displayName": "Test User",
    "featureName": "feature1",
    "featureVersion": "1.0",
    "reason": "test kickout",
    "force": true
  }
}
```

### 4.2 消息字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| messageId | String | 消息唯一ID (UUID) |
| businessKey | String | 业务Key，格式：`{softwareType}:{hostname}:{operation}:{timestamp}:{shortUuid}` |
| operationType | Enum | 操作类型 (KICKOUT, START, STOP等) |
| softwareType | Enum | 软件类型 (FLEXNET, SENTINEL, LMX) |
| hostname | String | 发送方主机名/ClientId |
| payload | Object | 消息载荷，根据操作类型不同而不同 |

## 5. 模块结构

```
license-mq-system/
├── license-common/                 # 公共模块
│   ├── config/                     # 配置类
│   │   └── MqttProperties          # MQTT配置属性
│   ├── enums/                      # 枚举类
│   │   ├── SoftwareType            # 软件类型
│   │   └── OperationType           # 操作类型
│   ├── message/                    # 消息模型
│   │   └── LicenseMessage          # 许可消息
│   ├── payload/                    # 消息载荷
│   │   ├── BasePayload             # 载荷接口
│   │   └── kickout/                # 踢出用户载荷
│   │       ├── FlexnetUserKickoutPayload
│   │       ├── SentinelUserKickoutPayload
│   │       └── LmxUserKickoutPayload
│   ├── mqtt/                       # MQTT核心
│   │   ├── MqttClientService       # MQTT客户端服务
│   │   └── MqttMessageListener     # 消息监听器接口
│   └── exception/                  # 异常类
│       └── MessageProcessException # 消息处理异常
│
├── license-server/                 # 服务端
│   ├── config/                     # 配置类
│   │   └── MqttConfig              # MQTT配置
│   ├── controller/                 # REST API
│   │   └── LicenseMgmtController   # 许可管理控制器
│   └── service/                    # 业务服务
│       ├── LicenseMgmtService      # 许可管理服务
│       └── MqttPublisherService    # MQTT发布服务
│
└── license-client/                 # 客户端
    ├── config/                     # 配置类
    │   └── MqttConfig              # MQTT配置
    ├── handler/                    # 消息处理器
    │   ├── MessageHandler          # 处理器接口
    │   ├── AbstractMessageHandler  # 抽象处理器
    │   └── impl/                   # 具体处理器实现
    │       └── UserKickoutHandler  # 用户踢出处理器
    └── listener/                   # 消息监听器
        └── MqttMessageListener     # MQTT消息监听器
```

## 6. 核心代码实现

### 6.1 MQTT客户端服务 (MqttClientService)

```java
/**
 * MQTT客户端服务
 * 支持P2P通信模式，基于clientId路由
 */
@Slf4j
public class MqttClientService implements MqttCallback {

    private final MqttAsyncClient client;
    private final String clientId;
    
    /**
     * 发送消息到指定客户端（P2P模式）
     * Topic格式: $deliver/{targetClientId}/license/{operation}
     */
    public void sendToClient(String targetClientId, String operation, LicenseMessage<?> message) {
        String topic = buildP2PTopic(targetClientId, operation);
        // 发布消息到EMQX
        client.publish(topic, mqttMessage);
    }
    
    /**
     * 订阅接收消息（P2P模式）
     * 使用$deliver前缀时，接收方订阅的是去掉$deliver/{clientId}后的topic
     * 订阅Topic: license/#
     */
    public void subscribeIncomingMessages() {
        String topic = "license/#";
        client.subscribe(topic, qos);
    }
    
    /**
     * 构建P2P Topic（EMQX直接投递模式）
     * 使用$deliver前缀实现点对点直接投递，不经过路由表
     */
    private String buildP2PTopic(String targetClientId, String operation) {
        return "$deliver/" + targetClientId + "/license/" + operation;
    }
    
    /**
     * 消息到达回调
     * 接收方收到的topic格式: license/{operation}
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String[] parts = topic.split("/");
        String operation = parts.length > 1 ? parts[1] : null;
        
        LicenseMessage<?> licenseMessage = objectMapper.readValue(body, LicenseMessage.class);
        String sourceClientId = licenseMessage.getHostname();
        
        if (messageListener != null) {
            messageListener.onMessage(sourceClientId, operation, licenseMessage);
        }
    }
}
```

### 6.2 服务端发布消息

```java
/**
 * MQTT消息发布服务
 */
@Service
public class MqttPublisherService {

    @Autowired
    private MqttClientService mqttClientService;

    /**
     * 发送消息到指定客户端
     */
    public void send(String targetClientId, OperationType operation, 
                     SoftwareType softwareType, Object payload) {
        LicenseMessage<Object> message = new LicenseMessage<>();
        message.setMessageId(UUID.randomUUID().toString());
        message.setBusinessKey(buildBusinessKey(softwareType, targetClientId, operation));
        message.setOperationType(operation);
        message.setSoftwareType(softwareType);
        message.setHostname(targetClientId);
        message.setPayload(payload);
        
        // 使用$deliver模式发送P2P消息
        mqttClientService.sendToClient(targetClientId, operation.getCode(), message);
    }
}
```

### 6.3 客户端消息处理

```java
/**
 * 用户踢出消息处理器
 */
@Component
@Slf4j
public class UserKickoutHandler extends AbstractMessageHandler {

    @Override
    public void handle(String sourceClientId, LicenseMessage<?> message) {
        log.info("Processing user kickout message from: {}", sourceClientId);
        
        // 获取载荷
        BasePayload payload = (BasePayload) message.getPayload();
        
        // 根据软件类型处理
        switch (message.getSoftwareType()) {
            case FLEXNET:
                handleFlexnetKickout((FlexnetUserKickoutPayload) payload);
                break;
            case SENTINEL:
                handleSentinelKickout((SentinelUserKickoutPayload) payload);
                break;
            case LMX:
                handleLmxKickout((LmxUserKickoutPayload) payload);
                break;
        }
    }
    
    @Override
    public OperationType getSupportedOperation() {
        return OperationType.KICKOUT;
    }
}
```

## 7. 配置说明

### 7.1 服务端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: license-server

mqtt:
  broker-url: tcp://localhost:1883
  connection-timeout: 30
  keep-alive-interval: 60
  auto-reconnect: true
  clean-session: true
  qos: 1
  timeout: 30000

license:
  server:
    id: license-server
```

### 7.2 客户端配置 (application.yml)

```yaml
server:
  port: 8081

spring:
  application:
    name: license-client

mqtt:
  broker-url: tcp://localhost:1883
  connection-timeout: 30
  keep-alive-interval: 60
  auto-reconnect: true
  clean-session: true
  qos: 1
  timeout: 30000

license:
  client:
    hostname: client-01  # 客户端唯一标识，用作MQTT clientId
```

## 8. API示例

### 8.1 踢出用户 - API格式

```bash
POST /api/license/{softwareType}/{hostname}/users/kickout
Content-Type: application/json

{
  "userId": "user001",
  "username": "testuser",
  "hostId": "host123",
  "displayName": "Test User",
  "featureName": "feature1",
  "featureVersion": "1.0",
  "reason": "管理员强制踢出",
  "force": true
}
```

**响应**: 200 OK

### 8.2 命令行调用示例

#### 8.2.1 使用 curl 命令 (Windows/Linux/macOS)

**FlexNet 踢出用户:**
```bash
curl -X POST http://localhost:8080/api/license/FLEXNET/client-01/users/kickout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user001",
    "username": "testuser",
    "hostId": "host123",
    "displayName": "Test User",
    "featureName": "feature1",
    "featureVersion": "1.0",
    "reason": "管理员强制踢出",
    "force": true
  }'
```

**Sentinel 踢出用户:**
```bash
curl -X POST http://localhost:8080/api/license/SENTINEL/client-02/users/kickout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user002",
    "username": "sentineluser",
    "hostId": "host456",
    "displayName": "Sentinel User",
    "featureName": "feature2",
    "featureVersion": "2.0",
    "reason": "测试踢出",
    "force": false
  }'
```

**LMX 踢出用户:**
```bash
curl -X POST http://localhost:8080/api/license/LMX/client-03/users/kickout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user003",
    "username": "lmxuser",
    "hostId": "host789",
    "displayName": "LMX User",
    "featureName": "feature3",
    "featureVersion": "3.0",
    "reason": "LMX测试",
    "force": true
  }'
```

#### 8.2.2 使用 PowerShell 命令 (Windows)

**FlexNet 踢出用户:**
```powershell
$body = @{
    userId = "user001"
    username = "testuser"
    hostId = "host123"
    displayName = "Test User"
    featureName = "feature1"
    featureVersion = "1.0"
    reason = "管理员强制踢出"
    force = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/license/FLEXNET/client-01/users/kickout" `
  -ContentType "application/json" `
  -Body $body
```

**Sentinel 踢出用户:**
```powershell
$body = @{
    userId = "user002"
    username = "sentineluser"
    hostId = "host456"
    displayName = "Sentinel User"
    featureName = "feature2"
    featureVersion = "2.0"
    reason = "测试踢出"
    force = $false
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/license/SENTINEL/client-02/users/kickout" `
  -ContentType "application/json" `
  -Body $body
```

**LMX 踢出用户:**
```powershell
$body = @{
    userId = "user003"
    username = "lmxuser"
    hostId = "host789"
    displayName = "LMX User"
    featureName = "feature3"
    featureVersion = "3.0"
    reason = "LMX测试"
    force = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/license/LMX/client-03/users/kickout" `
  -ContentType "application/json" `
  -Body $body
```

#### 8.2.3 使用 HTTPie 命令 (需要安装 httpie)

**安装 HTTPie:**
```bash
# Windows (使用 pip)
pip install httpie

# macOS (使用 brew)
brew install httpie

# Linux (使用 apt)
sudo apt install httpie
```

**FlexNet 踢出用户:**
```bash
http POST http://localhost:8080/api/license/FLEXNET/client-01/users/kickout \
  userId=user001 \
  username=testuser \
  hostId=host123 \
  displayName="Test User" \
  featureName=feature1 \
  featureVersion=1.0 \
  reason="管理员强制踢出" \
  force=true
```

#### 8.2.4 使用 wget 命令 (Linux)

**FlexNet 踢出用户:**
```bash
wget --post-data='{
  "userId": "user001",
  "username": "testuser",
  "hostId": "host123",
  "displayName": "Test User",
  "featureName": "feature1",
  "featureVersion": "1.0",
  "reason": "管理员强制踢出",
  "force": true
}' \
  --header='Content-Type:application/json' \
  -O - \
  http://localhost:8080/api/license/FLEXNET/client-01/users/kickout
```

### 8.3 API参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| softwareType | Path | 是 | 软件类型: FLEXNET, SENTINEL, LMX |
| hostname | Path | 是 | 目标客户端主机名/ClientId |
| userId | Body | 是 | 用户ID |
| username | Body | 是 | 用户名 |
| hostId | Body | 是 | 主机ID |
| displayName | Body | 否 | 显示名称 |
| featureName | Body | 否 | 功能名称 |
| featureVersion | Body | 否 | 功能版本 |
| reason | Body | 否 | 踢出原因 |
| force | Body | 否 | 是否强制踢出，默认false |

### 8.4 完整测试流程示例

**步骤1: 启动服务**
```bash
# 启动EMQX Broker
emqx start

# 启动License Server (端口8080)
cd license-server
mvn spring-boot:run

# 启动License Client (clientId: client-01)
cd license-client
mvn spring-boot:run
```

**步骤2: 发送测试请求**
```bash
# 使用curl发送踢出用户请求
curl -X POST http://localhost:8080/api/license/FLEXNET/client-01/users/kickout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser001",
    "username": "张三",
    "hostId": "workstation-01",
    "displayName": "张三",
    "featureName": "CAD_FEATURE",
    "featureVersion": "2024",
    "reason": "测试P2P通信",
    "force": true
  }'
```

**步骤3: 查看日志输出**

Server端日志:
```
2026-04-09 00:33:05.123  INFO --- API: Kickout Flexnet user 张三 on client-01
2026-04-09 00:33:05.125  INFO --- Sending P2P message to client-01, operation: kickout
```

Client端日志:
```
2026-04-09 00:33:05.128  INFO --- Received request message, topic: license/kickout, operation: kickout
2026-04-09 00:33:05.130  INFO --- Processing user kickout message from: license-server
2026-04-09 00:33:05.135  INFO --- Flexnet user kicked out: 张三 (testuser001)
```

### 8.5 消息流程

```
1. Server接收REST API请求
   ↓
2. 构建LicenseMessage消息
   ↓
3. 发布到EMQX: $deliver/client-01/license/kickout
   ↓
4. EMQX识别$deliver前缀，直接投递给client-01
   ↓
5. Client收到消息: license/kickout
   ↓
6. UserKickoutHandler处理消息
   ↓
7. 执行踢出用户操作
```

## 9. 部署架构

### 9.1 单机部署

```
┌─────────────────────────────────────────┐
│          单机部署架构                     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│          物理服务器                       │
│  ┌────────────────────────────────────┐ │
│  │      EMQX Broker (1883)            │ │
│  │      Dashboard (18083)             │ │
│  └────────────────────────────────────┘ │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │      License Server (8080)         │ │
│  │      - REST API                    │ │
│  │      - MQTT Publisher              │ │
│  └────────────────────────────────────┘ │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │      License Client (8081)         │ │
│  │      - MQTT Subscriber             │ │
│  │      - Message Handler             │ │
│  └────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 9.2 集群部署

```
┌─────────────────────────────────────────┐
│          集群部署架构                     │
└─────────────────────────────────────────┘

                    ┌──────────────┐
                    │ Load Balancer│
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼───────┐  ┌───────▼───────┐  ┌───────▼───────┐
│ EMQX Node 1   │  │ EMQX Node 2   │  │ EMQX Node 3   │
│ (1883)        │  │ (1883)        │  │ (1883)        │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼───────┐  ┌───────▼───────┐  ┌───────▼───────┐
│ Server Node 1 │  │ Server Node 2 │  │ Server Node 3 │
│ (8080)        │  │ (8080)        │  │ (8080)        │
└───────────────┘  └───────────────┘  └───────────────┘

        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼───────┐  ┌───────▼───────┐  ┌───────▼───────┐
│ Client Node 1 │  │ Client Node 2 │  │ Client Node 3 │
│ (client-01)   │  │ (client-02)   │  │ (client-03)   │
└───────────────┘  └───────────────┘  └───────────────┘
```

### 9.3 EMQX集群配置

```properties
# emqx.conf (Node 1)
node.name = emqx1@192.168.1.101
cluster.name = emqx-cluster
cluster.discovery = static
cluster.static.seeds = emqx1@192.168.1.101,emqx2@192.168.1.102,emqx3@192.168.1.103

# emqx.conf (Node 2)
node.name = emqx2@192.168.1.102
cluster.name = emqx-cluster
cluster.discovery = static
cluster.static.seeds = emqx1@192.168.1.101,emqx2@192.168.1.102,emqx3@192.168.1.103

# emqx.conf (Node 3)
node.name = emqx3@192.168.1.103
cluster.name = emqx-cluster
cluster.discovery = static
cluster.static.seeds = emqx1@192.168.1.101,emqx2@192.168.1.102,emqx3@192.168.1.103
```

## 10. 性能优化

### 10.1 MQTT连接优化

```yaml
mqtt:
  connection-timeout: 30      # 连接超时时间
  keep-alive-interval: 60     # 心跳间隔
  auto-reconnect: true        # 自动重连
  clean-session: true         # 清除会话
  qos: 1                      # 消息质量等级
  timeout: 30000              # 操作超时时间
```

### 10.2 EMQX性能调优

```properties
# 最大连接数
max_connections = 1024000

# 最大订阅数
max_subscriptions = 0

# 消息队列长度
max_mqueue_len = 10000

# 会话过期时间
session_expiry_interval = 2h

# 消息大小限制
max_packet_size = 1MB
```

### 10.3 消息处理优化

```java
// 使用异步处理
@Async
public void handleMessage(LicenseMessage<?> message) {
    // 异步处理消息
}

// 批量处理
public void handleBatch(List<LicenseMessage<?>> messages) {
    // 批量处理消息
}
```

## 11. 监控与运维

### 11.1 EMQX监控指标

- 连接数统计
- 订阅数统计
- 消息吞吐量
- 消息丢弃率
- 客户端在线状态

### 11.2 日志配置

```yaml
logging:
  level:
    com.license: DEBUG
    org.eclipse.paho: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 11.3 健康检查

```bash
# 检查EMQX状态
curl http://localhost:18083/api/v5/brokers

# 检查客户端连接
curl http://localhost:18083/api/v5/clients

# 检查订阅
curl http://localhost:18083/api/v5/subscriptions
```

## 12. 安全配置

### 12.1 TLS/SSL配置

```yaml
mqtt:
  broker-url: ssl://localhost:8883
  ssl:
    enabled: true
    trust-store: classpath:truststore.jks
    trust-store-password: changeit
```

### 12.2 认证配置

```yaml
mqtt:
  username: license-user
  password: license-password
```

### 12.3 ACL规则

```bash
# 只允许特定用户订阅
acl.rule.1 = {allow, {user, "license-user"}, subscribe, ["license/#"]}

# 只允许特定用户发布
acl.rule.2 = {allow, {user, "license-user"}, publish, ["$deliver/#"]}

# 拒绝其他所有操作
acl.rule.3 = {deny, all}
```

## 13. 故障排查

### 13.1 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 客户端连接断开 | ACL配置不正确 | 检查EMQX ACL规则 |
| 消息发送失败 | Topic格式错误 | 检查$deliver前缀 |
| 消息接收不到 | 订阅Topic错误 | 检查订阅的topic是否为license/# |
| 连接超时 | 网络问题 | 检查网络连接和防火墙 |

### 13.2 调试命令

```bash
# 查看EMQX日志
tail -f /var/log/emqx/emqx.log

# 查看客户端连接
mosquitto_sub -h localhost -t '$SYS/#' -v

# 测试消息发布
mosquitto_pub -h localhost -t '$deliver/client-01/license/kickout' -m '{"test":"message"}'

# 测试消息订阅
mosquitto_sub -h localhost -t 'license/#' -v
```

## 14. 扩展性设计

### 14.1 新增软件类型

1. 在 `SoftwareType` 枚举中添加新类型
2. 创建对应的Payload类
3. 创建对应的Handler实现
4. 在Controller中添加新的API endpoint

### 14.2 新增操作类型

1. 在 `OperationType` 枚举中添加新操作
2. 创建对应的Payload类
3. 创建对应的Handler实现
4. 添加新的业务逻辑

### 14.3 水平扩展

- EMQX集群支持水平扩展
- Server端无状态，可水平扩展
- Client端每个许可服务器部署一个实例

## 15. 最佳实践

### 15.1 ClientId命名规范

```
格式: {environment}-{application}-{hostname}

示例:
- prod-license-server-01
- dev-license-client-host01
- test-license-client-192.168.1.100
```

### 15.2 消息设计原则

- 消息体尽量小
- 使用JSON格式
- 包含必要的追踪信息（messageId, businessKey）
- 避免循环引用

### 15.3 错误处理

- 记录详细的错误日志
- 实现重试机制
- 监控消息处理失败率
- 设置告警阈值

---
