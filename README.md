# zippy

Java 21 + Spring Boot 3.5.x

## Modules

- zippy-kit: 公共 utility toolkit (Jackson / OkHttp / Id 生成器 / 通用 DTO)
- zippy-infra: 基础设施 (Redis / Kafka / MyBatis / ES / Storage / Geo / Web / Sa-Token)
- zippy-api: API 契约 (DTO / HttpExchange 接口)
  - zippy-sys-api: 系统管理 API
- zippy-svc: 服务实现
  - zippy-sys-service: 系统管理服务
  - zippy-netty-service: Netty 服务
- zippy-demo: 示例应用

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Redis (optional)
- Kafka (optional)
- PostgreSQL / H2
- MyBatis-Plus
- GeoTools
- Elasticsearch (optional)
- Sa-Token
- JUnit
- Lombok
- Netty
- SpringDoc
- log4j2

## Build

```bash
mvn clean package
```

## Run

```bash
mvn -pl zippy-demo spring-boot:run
```

```bash
mvn -pl zippy-svc/zippy-netty-service spring-boot:run
```

## API Docs

After startup:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

## Configuration

### Sa-Token

```yaml
# Sa-Token 官方配置
sa-token:
  token-name: Authorization
  timeout: 2592000        # token 有效期（秒），默认30天
  is-concurrent: true     # 是否允许同一账号并发登录
  is-share: false         # 多次登录是否共享 token
  token-style: uuid       # token 风格：uuid/simple-uuid/random-32/random-64/random-128/tik
  is-log: true            # 是否输出操作日志

# zippy 扩展配置
zippy:
  satoken:
    enabled: true           # 是否启用（默认 true）
    login-type: login       # 登录类型标识
    token-prefix: Bearer    # Token 前缀
    root-user-id: 1         # 超级管理员用户ID
```

### MyBatis-Plus

```yaml
# MyBatis-Plus 官方配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true              # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl  # 使用 Slf4j 输出 SQL

# 通过日志级别控制 SQL 打印开关
logging:
  level:
    com.zippy.**.mapper: debug   # 开启 SQL 打印
    # com.zippy.**.mapper: info  # 关闭 SQL 打印

# zippy 扩展配置
zippy:
  mybatis:
    enabled: true   # 是否启用 MyBatis 扩展（默认 true）
```

### Storage

```yaml
zippy:
  storage:
    enabled: true
    # LOCAL | S3
    type: LOCAL
    public-base-url: ""
    date-path-pattern: yyyy/MM/dd
    # UUID | ORIGINAL
    filename-strategy: UUID
    # APPEND_SUFFIX | FAIL | OVERWRITE
    conflict-strategy: APPEND_SUFFIX
    local:
      root-path: ./uploads
      access-path-prefix: /uploads
    s3:
      bucket: zippy
      endpoint: ""
      region: us-east-1
      access-key: ""
      secret-key: ""
      domain: ""
      path-style-access: true
```

## Version Management

项目为三层多模块结构，所有模块共享同一版本号，由根 POM 统一管理：

```
zippy (root)
├── zippy-kit
├── zippy-infra
│   ├── zippy-infra-redis
│   ├── zippy-infra-kafka
│   ├── zippy-infra-es
│   ├── zippy-infra-mybatis
│   ├── zippy-infra-storage
│   ├── zippy-infra-geo
│   ├── zippy-infra-web
│   └── zippy-infra-satoken
├── zippy-api
│   └── zippy-sys-api
├── zippy-svc
│   ├── zippy-sys-service
│   └── zippy-netty-service
└── zippy-demo
```

各子模块通过 `<parent>` 继承版本，内部依赖通过根 POM 的 `dependencyManagement` + `${project.version}` 统一管理，无需在子模块中硬编码版本。

### 升级版本

一条命令更新所有模块（根 POM + 全部子模块）的 version 和 parent version：

```bash
mvn versions:set -DnewVersion=2.0.0-SNAPSHOT
```

确认无误后提交变更：

```bash
mvn versions:commit
```

如需回退：

```bash
mvn versions:revert
```
