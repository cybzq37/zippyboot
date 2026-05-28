# zippyboot-infra & zippyboot-kit 代码审查与最佳实践建议

> 审查日期：2026-05-28
> 审查范围：zippyboot-infra、zippyboot-kit 两个基础模块
> 审查维度：架构设计、层级划分、命名规范、代码质量、测试覆盖

---

## 最佳实践建议汇总

| # | 类别 | 问题 | 建议 | 优先级 |
|---|------|------|------|--------|
| 1 | 架构 | infra 职责过重，同时承载 Redis/Kafka/ES/MyBatis/Sa-Token/Storage/GIS 七类基础设施 | ~~拆分 geo/storage 为独立模块~~ **已完成** -- 拆为 6 个子模块：infra-redis/kafka/es/mybatis/storage/geo | ~~高~~ |
| 2 | 架构 | Auto-Configuration 注册不一致，Storage/MyBatis 注册到 imports 文件，而 SaToken/Kafka/ES 依赖组件扫描 | ~~统一使用 `@AutoConfiguration` 并注册到 `imports` 文件~~ **已完成** -- SaTokenConfig/KafkaConsumerAutoConfiguration 改为 `@AutoConfiguration`，ES 新增 `ElasticsearchAutoConfiguration` | ~~高~~ |
| 3 | 架构 | kit 强制依赖 `spring-webmvc`，导致非 Web 项目被迫引入 MVC 栈 | ~~将 `spring-webmvc` 设为 `optional`~~ **已完成** -- `GlobalExceptionHandler`/`GlobalResponseBodyAdvice` 及相关类移入 `zippyboot-infra-web`，kit 只保留 `BaseException`/`ErrorResponse`/`ApiResponse` 契约类，`spring-web` 改为 optional | ~~中~~ |
| 4 | 架构 | `GlobalThreadPool` 静态全局单例，core/max/queue 硬编码，无法按业务配置 | 改为 Spring Bean + `@ConfigurationProperties` 配置化 | 中 |
| 5 | 包结构 | Sa-Token 下用 `utils`，MyBatis 下用 `util`，命名不一致 | 统一为 `util` | 低 |
| 6 | 包结构 | `@Xss` 是 Bean Validation 注解却放在 `jackson.plugins.xss` 下，名不副实 | 移到 `com.zippyboot.kit.validation` 包下 | 低 |
| 7 | 包结构 | ES 子模块没有 `config` 包，`ElasticsearchTemplate` 直接放在根包，与其他子模块模式不一致 | 补充 `config` 子包或统一模式 | 低 |
| 8 | 命名 | `com.zippyboot.infra.redis.RedisTemplate` 与 `org.springframework.data.redis.core.RedisTemplate` 同名，内部和使用者都容易混淆 | 重命名为 `RedisHelper` 或 `ZippyRedisTemplate` | 高 |
| 9 | 命名 | `BaseException` 的 `code`(String) 与 `status`(HttpStatusCode) 语义重叠，`ApiResponse` 的 `code` 含义又不同 | `BaseException.code` 改为 `bizCode`，与 HTTP status 区分 | 中 |
| 10 | 命名 | `ApiResponse.fail()` 硬编码 code 为 `"-1"`，与 success 的 `"0"` 风格不一致 | `fail()` 接受 code 参数，或定义有意义的常量 | 低 |
| 11 | 代码 | `HttpTemplate` 保留已废弃的 `getInstance()` 双重检查锁单例，SNAPSHOT 阶段应直接移除 | 删除 `getInstance()` 及相关静态字段 | 中 |
| 12 | 代码 | `RedisTemplate` 的 Object 操作方法每个都有相同的 `objectRedisTemplate == null` 检查 | 提取 `requireObjectTemplate()` 私有方法减少重复 | 低 |
| 13 | 代码 | `KafkaProducerTemplate` 每个方法都纯委托给 `KafkaOperations`，无附加逻辑 | 增加统一异常转换/日志/重试，或移除直接注入 `KafkaOperations` | 中 |
| 14 | 代码 | `ElasticsearchTemplate` 纯委托 `ElasticsearchOperations`，仅 `toPage()` 有增量价值 | 增加统一异常处理/日志，或移除让业务层直接使用 `ElasticsearchOperations` | 中 |
| 15 | 代码 | `GlobalResponseBodyAdvice` 对 String 返回值手动 `ObjectMapper` 序列化，绕过 Spring 消息转换器链 | 文档化说明行为，或建议 Controller 使用 `@IgnoreResponseWrap` | 低 |
| 16 | 代码 | `GeoFormatUtils` 使用 `ThreadLocal<WKTReader/WKTWriter>` 但无清理机制，Web 容器线程复用可能导致内存泄漏 | 改用对象池或每次新建实例 | 中 |
| 17 | 代码 | Storage 异常继承树不一致，`StorageInvalidKeyException` 继承 `IllegalArgumentException` 而非 `StorageException` | 统一继承关系，或明确文档化设计意图 | 低 |
| 18 | 测试 | infra 的 redis/kafka/es/satoken 子模块完全没有测试 | 优先补充 `RedisTemplate`（分布式锁 Lua 脚本）和 `LoginHelper`（缓存逻辑）测试 | 高 |
| 19 | 测试 | kit 的 DateUtils/IdUtils/StringUtils/BeanUtils/ZipUtils/TreeUtils/HttpTemplate 缺少测试 | 补充 `IdUtils` Snowflake、`DateUtils` 多格式解析、`StringUtils` 边界情况测试 | 中 |

---

## 按优先级分组

### 高优先级（建议尽快处理）

1. ~~**infra 模块拆分**~~ **已完成** -- 拆为 infra-redis/kafka/es/mybatis/storage/geo 六个子模块
2. ~~**统一 Auto-Configuration 注册**~~ **已完成** -- 所有配置类统一使用 `@AutoConfiguration` + `imports` 文件
3. **RedisTemplate 重命名** -- 消除与 Spring 同名类的混淆
4. **补充核心测试** -- Redis 分布式锁、Sa-Token 登录缓存等关键逻辑无测试覆盖

### 中优先级（版本迭代中逐步改进）

5. ~~kit 的 `spring-webmvc` 改为 optional~~ **已完成** -- Web 基础设施类移入 `zippyboot-infra-web`
6. `GlobalThreadPool` 配置化
7. 移除 `HttpTemplate.getInstance()` 废弃方法
8. `KafkaProducerTemplate` / `ElasticsearchTemplate` 要么增加附加价值要么移除
9. `BaseException.code` 语义澄清
10. `GeoFormatUtils` ThreadLocal 清理
11. 补充 kit 工具类测试

### 低优先级（有空再改）

12. `utils` vs `util` 包名统一
13. `@Xss` 包结构调整
14. ES 补充 config 子包
15. `ApiResponse.fail()` code 参数化
16. `RedisTemplate` 重复 null 检查
17. `GlobalResponseBodyAdvice` String 处理文档化
18. Storage 异常继承树统一
