# zippyboot

Java 21 + Spring Boot 3.5.x

## Modules

- zippyboot-api: 公共接口定义，服务直接通过api相互调用，Feign接口、请求/响应DTO
- zippyboot-app: 主业务应用，主要是controller 和 service
- zippyboot-model: 共享的PO、DTO 和其他 model objects，与组件无关
- zippyboot-infra: 集成组件配置 (Redis/Kafka/Postgres/MyBatis-Plus/GeoTools/Elasticsearch)
- zippyboot-kit: 公共的 utility toolkit
- zippyboot-netty: 单独的netty服务，可通过zippyboot-api 与其他应用交互 (TCP/UDP server)

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Redis
- Kafka
- PostgreSQL
- MyBatis-Plus
- GeoTools
- Elasticsearch
- Sa-Token
- JUnit
- Lombok
- Netty
- SpringDoc
- log4j2
- lombok

## Build

```bash
mvn clean package
```

## Run

```bash
mvn -pl zippyboot-app spring-boot:run
```

```bash
mvn -pl zippyboot-netty spring-boot:run
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

# zippyboot 扩展配置
zippyboot:
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
    com.zippyboot.**.mapper: debug   # 开启 SQL 打印
    # com.zippyboot.**.mapper: info  # 关闭 SQL 打印

# zippyboot 扩展配置
zippyboot:
  mybatis:
    enabled: true   # 是否启用 MyBatis 扩展（默认 true）
```

### Storage

```yaml
zippyboot:
  infra:
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
        bucket: zippyboot
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
zippyboot (root)
├── zippyboot-api
├── zippyboot-app
├── zippyboot-model
├── zippyboot-kit
├── zippyboot-netty
└── zippyboot-infra
    ├── zippyboot-infra-redis
    ├── zippyboot-infra-kafka
    ├── zippyboot-infra-es
    ├── zippyboot-infra-mybatis
    ├── zippyboot-infra-storage
    ├── zippyboot-infra-geo
    ├── zippyboot-infra-web
    └── zippyboot-infra-satoken
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


还缺少定时任务模块


整体以zippy为代号，
zippyboot
zippyframe
zippykit
zippycore
zippystack
zippyhub
zippystarter
zippylanucher
zippygenesis
zippynexus

zippy-api
zippy-model
zippy-kit
zippy-infra
zippy-app


zippyboot-api
zippyboot-model
zippyboot-kit
zippyboot-infra
zippyboot-app




- 现在很多方法仍然直接 throws Exception，后面如果被 controller/service 直接调用，异常边界会比较粗。