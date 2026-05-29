# Zippy SYS 模块实施计划

## 1. 目标

新增通用的用户管理模块，包含 用户、角色、权限、组织机构、资源 五大能力，采用标准 RBAC0 模型。

## 2. 模块结构

```
zippy (root)
├── zippy-common           ← 公共基础 (BaseEntity / 工具类 / 异常)
├── zippy-infra            ← 基础设施 (mybatis / redis / satoken)
├── apis/                      ← API 契约库
│   └── zippy-sys-api      ← DTO / VO / Feign 接口
├── services/                  ← 可独立运行的服务
│   └── zippy-sys-service  ← 【新建】系统管理服务 (entity / service / mapper / controller / main)
└── zippy-app              ← 其他应用，通过 sys-api 调用 sys-service
```

**依赖链路：**
```
zippy-sys-service (独立运行)
  ├── zippy-common
  ├── zippy-infra-mybatis
  ├── zippy-infra-redis
  └── zippy-infra-satoken

zippy-app (其他应用)
  └── zippy-sys-api          ← 通过 Feign 调用 sys-service
```

**设计决策：**
- zippy-sys-service 可独立启动，同时暴露 REST API 供其他应用调用
- zippy-sys-api 只放对外契约 (DTO/VO/FeignClient)，不包含 Entity
- Entity 是 sys-service 内部实现，不暴露给外部；Controller 中做 Entity ↔ DTO 转换
- Controller 和 FeignClient 各自独立定义，通过 HTTP 路径和返回类型对齐（松耦合），不通过代码级接口实现绑定
- 其他应用依赖 sys-api 即可通过 Feign 调用，无需感知数据库模型

---

## 3. 数据库表设计

### 3.1 ER 关系

```
User       N──N Role         (sys_user_role)
Role       N──N Permission   (sys_role_permission)
User       N──N Organization (sys_user_org)
Organization 1──N self       (parent_id 树形)
Permission   1──N self       (parent_id 树形)
```

### 3.2 主表

#### sys_user

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| username | VARCHAR(64) NOT NULL UNIQUE | 登录账号 |
| password | VARCHAR(256) NOT NULL | BCrypt 密码 |
| nickname | VARCHAR(64) | 昵称 |
| real_name | VARCHAR(64) | 真实姓名 |
| email | VARCHAR(128) | 邮箱 |
| phone | VARCHAR(32) | 手机号 |
| avatar | VARCHAR(512) | 头像URL |
| gender | SMALLINT DEFAULT 0 | 0=未知 1=男 2=女 |
| status | SMALLINT DEFAULT 1 | 1=正常 0=停用 2=锁定 |
| login_ip | VARCHAR(64) | 最后登录IP |
| login_time | TIMESTAMP | 最后登录时间 |
| pwd_update_time | TIMESTAMP | 密码最后修改时间 |
| remark | VARCHAR(512) | 备注 |
| version / deleted / create_by / create_time / update_by / update_time | | 公共字段 |

#### sys_role

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| role_code | VARCHAR(64) NOT NULL UNIQUE | 角色编码 (如 ADMIN, USER) |
| role_name | VARCHAR(128) NOT NULL | 角色名称 |
| role_type | SMALLINT DEFAULT 1 | 1=自定义 0=系统内置 (不可删) |
| sort | INT DEFAULT 0 | 排序 |
| status | SMALLINT DEFAULT 1 | 1=正常 0=停用 |
| data_scope | SMALLINT DEFAULT 1 | 数据权限范围 (见下表) |
| remark | VARCHAR(512) | 备注 |
| version / deleted / ... | | 公共字段 |

**data_scope 枚举：**
| 值 | 含义 |
|----|------|
| 1 | 全部数据 |
| 2 | 本部门及以下 |
| 3 | 本部门 |
| 4 | 仅本人 |

#### sys_permission

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| parent_id | VARCHAR(64) DEFAULT '0' | 父权限ID ('0'=顶级) |
| perm_code | VARCHAR(128) NOT NULL UNIQUE | 权限标识 (如 user:list, user:add) |
| perm_name | VARCHAR(128) NOT NULL | 权限名称 |
| perm_type | SMALLINT NOT NULL | 1=目录 2=菜单 3=按钮/操作 4=API |
| path | VARCHAR(256) | 路由路径 (菜单用) |
| component | VARCHAR(256) | 前端组件路径 (菜单用) |
| icon | VARCHAR(128) | 图标 |
| sort | INT DEFAULT 0 | 排序 |
| visible | BOOLEAN DEFAULT TRUE | 是否可见 |
| status | SMALLINT DEFAULT 1 | 1=正常 0=停用 |
| remark | VARCHAR(512) | 备注 |
| version / deleted / ... | | 公共字段 |

#### sys_organization

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| parent_id | VARCHAR(64) DEFAULT '0' | 父机构ID ('0'=顶级) |
| org_code | VARCHAR(64) NOT NULL UNIQUE | 机构编码 |
| org_name | VARCHAR(128) NOT NULL | 机构名称 |
| org_type | SMALLINT DEFAULT 1 | 1=总公司 2=分公司 3=部门 4=小组 |
| leader_id | VARCHAR(64) | 负责人用户ID |
| phone | VARCHAR(32) | 联系电话 |
| email | VARCHAR(128) | 邮箱 |
| sort | INT DEFAULT 0 | 排序 |
| status | SMALLINT DEFAULT 1 | 1=正常 0=停用 |
| remark | VARCHAR(512) | 备注 |
| version / deleted / ... | | 公共字段 |

#### sys_resource

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| res_name | VARCHAR(128) NOT NULL | 资源名称 |
| res_type | SMALLINT DEFAULT 1 | 1=API接口 2=文件 3=数据 |
| request_method | VARCHAR(16) | HTTP方法 (GET/POST/PUT/DELETE) |
| request_path | VARCHAR(256) | API路径 (如 /api/v1/users/**) |
| status | SMALLINT DEFAULT 1 | 1=正常 0=停用 |
| remark | VARCHAR(512) | 备注 |
| version / deleted / ... | | 公共字段 |

### 3.3 关联表

#### sys_user_role

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| user_id | VARCHAR(64) NOT NULL | 用户ID |
| role_id | VARCHAR(64) NOT NULL | 角色ID |

UNIQUE: (user_id, role_id)

#### sys_role_permission

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| role_id | VARCHAR(64) NOT NULL | 角色ID |
| permission_id | VARCHAR(64) NOT NULL | 权限ID |

UNIQUE: (role_id, permission_id)

#### sys_user_org

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) PK | UUID |
| user_id | VARCHAR(64) NOT NULL | 用户ID |
| org_id | VARCHAR(64) NOT NULL | 机构ID |

UNIQUE: (user_id, org_id)

---

## 4. 包结构

### 4.1 zippy-sys-service — `com.zippy.sys`

Entity 为内部实现，不暴露给外部模块。

```
entity/
  SysUser.java
  SysRole.java
  SysPermission.java
  SysOrganization.java
  SysResource.java
  SysUserRole.java
  SysRolePermission.java
  SysUserOrg.java
enums/
  UserStatus.java       (1=正常 0=停用 2=锁定)
  GenderType.java       (0=未知 1=男 2=女)
  RoleType.java         (1=自定义 0=系统内置)
  PermType.java         (1=目录 2=菜单 3=按钮 4=API)
  OrgType.java          (1=总公司 2=分公司 3=部门 4=小组)
  DataScopeType.java    (1=全部 2=本部门及以下 3=本部门 4=仅本人)
  ResourceType.java     (1=API 2=文件 3=数据)
config/
  SysAutoConfiguration.java          @AutoConfiguration
manager/
  UserManager.java                   用户领域编排 (含缓存)
  RoleManager.java
  PermissionManager.java             权限查询 + 缓存
  OrgManager.java
service/
  SysUserService.java                接口
  SysRoleService.java
  SysPermissionService.java
  SysOrganizationService.java
  SysResourceService.java
  SysUserRoleService.java
  SysRolePermissionService.java
  impl/
    SysUserServiceImpl.java
    SysRoleServiceImpl.java
    SysPermissionServiceImpl.java
    SysOrganizationServiceImpl.java
    SysResourceServiceImpl.java
    SysUserRoleServiceImpl.java
    SysRolePermissionServiceImpl.java
mapper/
  SysUserMapper.java                 extends BaseMapper<SysUser>
  SysRoleMapper.java
  SysPermissionMapper.java
  SysOrganizationMapper.java
  SysResourceMapper.java
  SysUserRoleMapper.java
  SysRolePermissionMapper.java
  SysUserOrgMapper.java
stp/
  SaPermissionDelegate.java          implements StpInterface (替换空实现)
util/
  PasswordUtils.java                 BCrypt 编码/验证
  PermConstants.java                 权限常量 (*:*:*, 分隔符)
controller/
  AuthController.java                登录/登出/获取当前用户
  SysUserController.java             用户 CRUD (Entity ↔ UserDTO 转换)
  SysRoleController.java            角色 CRUD (Entity ↔ RoleDTO 转换)
  SysPermissionController.java      权限/菜单 CRUD (Entity ↔ PermissionDTO 转换)
  SysOrganizationController.java    组织机构 CRUD (Entity ↔ OrgDTO 转换)
  SysResourceController.java        资源 CRUD
```

公共字段抽取 `BaseEntity`（id, version, deleted, createBy, createTime, updateBy, updateTime），所有实体继承。

### 4.2 apis/zippy-sys-api — `com.zippy.api.sys`

只放对外契约，不包含 Entity。

```
dto/
  user/
    UserDTO.java
    LoginUserDTO.java        (存入 Sa-Token Session)
    UserQuery.java
    UserPageQuery.java
  role/
    RoleDTO.java
    RoleQuery.java
  permission/
    PermissionDTO.java
    MenuTreeDTO.java
    PermissionQuery.java
  org/
    OrgDTO.java
    OrgTreeDTO.java
vo/
  LoginVO.java               (token + 用户信息)
  UserInfoVO.java            (当前用户详情，含角色/权限/菜单)
feign/
  RemoteUserService.java     (@FeignClient)
```

---

## 5. 与现有模块的集成

### 5.1 Sa-Token 集成

**现状：** `SaTokenConfig.stpInterface()` 使用 `@ConditionalOnMissingBean(StpInterface.class)` 注册 `SaPermissionImpl`（返回空列表）。

**方案：** 在 `SysAutoConfiguration` 中注册真实的 `StpInterface`，由于 Spring Boot 自动配置顺序，sys-service 的 Bean 会优先生效，`SaPermissionImpl` 被跳过。

```java
@AutoConfiguration(after = SaTokenConfig.class)
public class SysAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface(PermissionManager permissionManager) {
        return new SaPermissionDelegate(permissionManager);
    }
}
```

`SaPermissionDelegate` 核心逻辑：
- `getPermissionList()`: root 用户返回 `*:*:*`，普通用户查 Redis 缓存 → DB
- `getRoleList()`: root 用户返回 `admin`，普通用户查 Redis 缓存 → DB

### 5.2 Redis 缓存

| Key | 内容 | TTL | 失效时机 |
|-----|------|-----|---------|
| `sys:perm:user:{userId}` | Set\<String\> 权限码 | 30min | 角色/权限变更 |
| `sys:role:user:{userId}` | Set\<String\> 角色码 | 30min | 用户角色变更 |
| `sys:menu:user:{userId}` | List\<MenuTreeDTO\> JSON | 30min | 权限变更 |
| `sys:user:info:{userId}` | LoginUserDTO JSON | 30min | 用户信息变更 |

使用 `zippy-infra-redis` 的 `RedisClient` 操作。

### 5.3 LoginHelper 复用

SYS 登录流程直接调用现有 `LoginHelper.login(loginId, userId, loginUserDTO)`，无需修改 infra-satoken 代码。`LoginUserDTO` 存入 Sa-Token Session，`LoginHelper.getLoginUser()` 可直接取回。

---

## 6. 核心业务流程

### 6.1 登录

```
1. POST /auth/login { username, password }
2. SysUserMapper.selectByUsername(username) → SysUser
3. BCrypt 验证密码
4. 检查 user.status == 1
5. 构建 LoginUserDTO (userId, username, nickname, avatar...)
6. LoginHelper.login(loginId, userId, loginUserDTO)
7. 异步更新 login_ip, login_time
8. 返回 LoginVO { token, userInfo }
```

### 6.2 权限校验

```
1. 请求 → SaInterceptor 拦截
2. @SaCheckPermission("user:add") → StpInterface.getPermissionList()
3. SaPermissionDelegate → Redis 缓存查询
4. 缓存未命中 → DB: sys_user → sys_user_role → sys_role_permission → sys_permission
5. 写入 Redis 缓存
6. Sa-Token 通配符匹配 (*:*:*, user:*, user:add)
7. 通过 → 放行；不通过 → 403
```

### 6.3 菜单树查询

```
1. 获取当前用户权限ID列表
2. 查 sys_permission (perm_type IN 1,2 且 status=1)
3. TreeBuilder.buildTree() 构建前端路由菜单树
4. 缓存到 Redis
```

### 6.4 组织机构树查询

```
1. 查所有 sys_organization
2. TreeBuilder.buildTree() 构建树
3. 返回 List<TreeNode<String>>
```

---

## 7. 关键 Mapper 示例

```java
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = false")
    SysUser selectByUsername(@Param("username") String username);

    @Select("""
        SELECT r.role_code FROM sys_role r
        INNER JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = false
    """)
    List<String> selectRoleCodesByUserId(@Param("userId") String userId);

    @Select("""
        SELECT DISTINCT p.perm_code FROM sys_permission p
        INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
        INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id
        WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = false
    """)
    List<String> selectPermCodesByUserId(@Param("userId") String userId);
}
```

---

## 8. POM 配置

### 8.1 apis/zippy-sys-api/pom.xml

```xml
<parent>
    <groupId>com.zippy</groupId>
    <artifactId>zippy</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
<artifactId>zippy-sys-api</artifactId>

<dependencies>
    <dependency><groupId>org.springframework.cloud</groupId><artifactId>spring-cloud-starter-openfeign</artifactId></dependency>
</dependencies>
```

### 8.2 services/zippy-sys-service/pom.xml

```xml
<parent>
    <groupId>com.zippy</groupId>
    <artifactId>zippy</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
<artifactId>zippy-sys-service</artifactId>

<dependencies>
    <dependency><groupId>com.zippy</groupId><artifactId>zippy-sys-api</artifactId></dependency>
    <dependency><groupId>com.zippy</groupId><artifactId>zippy-infra-mybatis</artifactId></dependency>
    <dependency><groupId>com.zippy</groupId><artifactId>zippy-infra-redis</artifactId></dependency>
    <dependency><groupId>com.zippy</groupId><artifactId>zippy-infra-satoken</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-crypto</artifactId></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
</dependencies>
```

### 8.3 根 pom.xml 变更

```xml
<!-- modules 中新增 -->
<module>apis/zippy-sys-api</module>
<module>services/zippy-sys-service</module>

<!-- dependencyManagement 中新增 -->
<dependency>
    <groupId>com.zippy</groupId>
    <artifactId>zippy-sys-api</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.zippy</groupId>
    <artifactId>zippy-sys-service</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 9. 实施阶段

| Phase | 内容 | 产出 |
|-------|------|------|
| **1** | 建表 SQL + model 实体 + 枚举 | SQL 脚本, 8 个实体, 7 个枚举, BaseEntity |
| **2** | 模块骨架 | zippy-sys-service POM, SysAutoConfiguration, 8 个 Mapper 接口 |
| **3** | 用户管理 | SysUserService (CRUD/注册/登录), PasswordUtils, LoginHelper 集成 |
| **4** | 角色 + 权限管理 | SysRoleService, SysPermissionService, SaPermissionDelegate |
| **5** | 组织机构 | SysOrganizationService + TreeBuilder 集成 |
| **6** | 资源管理 | SysResourceService |
| **7** | 缓存层 | PermissionManager 缓存 + 失效策略 |
| **8** | API 层 | zippy-sys-api DTO/VO/Feign + Controller |

---

## 10. 风险与注意事项

1. **密码安全** — 使用 `BCryptPasswordEncoder`，字段长度 VARCHAR(256) 兼容未来算法升级。

2. **Sa-Token Session 数据量** — `LoginUserDTO` 序列化存入 Redis Session，应精简字段，避免存入大对象。

3. **现有 MyBatis Mapper 扫描** — sys-service 的 Mapper 需要被 `@MapperScan` 覆盖。检查 infra-mybatis 的扫描路径是否包含 `com.zippy.sys.mapper`，否则需在 SysAutoConfiguration 中额外配置。
