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

## versions

子模块保留固定写法 `<version>1.0.0-SNAPSHOT</version>`  
升级版本时只执行一次命令批量更新所有子模块 `mvn -N versions:update-child-modules`