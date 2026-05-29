-- ============================================================
-- Zippy System Module - PostgreSQL Schema
-- ============================================================

-- ----------------------------
-- Table: sys_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id              VARCHAR(64)  PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    password        VARCHAR(256),
    nickname        VARCHAR(64),
    real_name       VARCHAR(64),
    email           VARCHAR(128),
    phone           VARCHAR(32),
    avatar          VARCHAR(512),
    gender          SMALLINT     DEFAULT 0,
    status          SMALLINT     DEFAULT 1,
    login_ip        VARCHAR(64),
    login_time      TIMESTAMP,
    pwd_update_time TIMESTAMP,
    remark          VARCHAR(512),
    version         INT          DEFAULT 0,
    deleted         BOOLEAN      DEFAULT FALSE,
    create_by       VARCHAR(64),
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(64),
    update_time     TIMESTAMP,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

COMMENT ON TABLE  sys_user                IS 'User table';
COMMENT ON COLUMN sys_user.id             IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_user.username       IS 'Login username';
COMMENT ON COLUMN sys_user.password       IS 'Encrypted password';
COMMENT ON COLUMN sys_user.nickname       IS 'Display nickname';
COMMENT ON COLUMN sys_user.real_name      IS 'Real name';
COMMENT ON COLUMN sys_user.email          IS 'Email address';
COMMENT ON COLUMN sys_user.phone          IS 'Phone number';
COMMENT ON COLUMN sys_user.avatar         IS 'Avatar URL';
COMMENT ON COLUMN sys_user.gender         IS 'Gender: 0=unknown, 1=male, 2=female';
COMMENT ON COLUMN sys_user.status         IS 'Status: 0=disabled, 1=enabled';
COMMENT ON COLUMN sys_user.login_ip       IS 'Last login IP';
COMMENT ON COLUMN sys_user.login_time     IS 'Last login time';
COMMENT ON COLUMN sys_user.pwd_update_time IS 'Password last update time';
COMMENT ON COLUMN sys_user.remark         IS 'Remark';
COMMENT ON COLUMN sys_user.version        IS 'Optimistic lock version';
COMMENT ON COLUMN sys_user.deleted        IS 'Logical delete flag';
COMMENT ON COLUMN sys_user.create_by      IS 'Creator ID';
COMMENT ON COLUMN sys_user.create_time    IS 'Create time';
COMMENT ON COLUMN sys_user.update_by      IS 'Updater ID';
COMMENT ON COLUMN sys_user.update_time    IS 'Update time';

-- ----------------------------
-- Table: sys_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          VARCHAR(64)  PRIMARY KEY,
    role_code   VARCHAR(64)  NOT NULL,
    role_name   VARCHAR(128),
    role_type   SMALLINT     DEFAULT 1,
    sort        INT          DEFAULT 0,
    status      SMALLINT     DEFAULT 1,
    data_scope  SMALLINT     DEFAULT 1,
    remark      VARCHAR(512),
    version     INT          DEFAULT 0,
    deleted     BOOLEAN      DEFAULT FALSE,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    CONSTRAINT uk_sys_role_role_code UNIQUE (role_code)
);

COMMENT ON TABLE  sys_role             IS 'Role table';
COMMENT ON COLUMN sys_role.id          IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_role.role_code   IS 'Role code (unique)';
COMMENT ON COLUMN sys_role.role_name   IS 'Role name';
COMMENT ON COLUMN sys_role.role_type   IS 'Role type: 1=custom, 2=built-in';
COMMENT ON COLUMN sys_role.sort        IS 'Sort order';
COMMENT ON COLUMN sys_role.status      IS 'Status: 0=disabled, 1=enabled';
COMMENT ON COLUMN sys_role.data_scope  IS 'Data scope: 1=all, 2=dept, 3=dept and below, 4=self';
COMMENT ON COLUMN sys_role.remark      IS 'Remark';
COMMENT ON COLUMN sys_role.version     IS 'Optimistic lock version';
COMMENT ON COLUMN sys_role.deleted     IS 'Logical delete flag';
COMMENT ON COLUMN sys_role.create_by   IS 'Creator ID';
COMMENT ON COLUMN sys_role.create_time IS 'Create time';
COMMENT ON COLUMN sys_role.update_by   IS 'Updater ID';
COMMENT ON COLUMN sys_role.update_time IS 'Update time';

-- ----------------------------
-- Table: sys_permission
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_permission (
    id          VARCHAR(64)  PRIMARY KEY,
    parent_id   VARCHAR(64)  DEFAULT '0',
    perm_code   VARCHAR(128) NOT NULL,
    perm_name   VARCHAR(128),
    perm_type   SMALLINT     NOT NULL,
    path        VARCHAR(256),
    component   VARCHAR(256),
    icon        VARCHAR(128),
    sort        INT          DEFAULT 0,
    visible     BOOLEAN      DEFAULT TRUE,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(512),
    version     INT          DEFAULT 0,
    deleted     BOOLEAN      DEFAULT FALSE,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    CONSTRAINT uk_sys_permission_perm_code UNIQUE (perm_code)
);

COMMENT ON TABLE  sys_permission             IS 'Permission / menu table';
COMMENT ON COLUMN sys_permission.id          IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_permission.parent_id   IS 'Parent permission ID';
COMMENT ON COLUMN sys_permission.perm_code   IS 'Permission code (unique)';
COMMENT ON COLUMN sys_permission.perm_name   IS 'Permission name';
COMMENT ON COLUMN sys_permission.perm_type   IS 'Permission type: 1=directory, 2=menu, 3=button';
COMMENT ON COLUMN sys_permission.path        IS 'Route path';
COMMENT ON COLUMN sys_permission.component   IS 'Frontend component';
COMMENT ON COLUMN sys_permission.icon        IS 'Menu icon';
COMMENT ON COLUMN sys_permission.sort        IS 'Sort order';
COMMENT ON COLUMN sys_permission.visible     IS 'Whether visible in menu';
COMMENT ON COLUMN sys_permission.status      IS 'Status: 0=disabled, 1=enabled';
COMMENT ON COLUMN sys_permission.remark      IS 'Remark';
COMMENT ON COLUMN sys_permission.version     IS 'Optimistic lock version';
COMMENT ON COLUMN sys_permission.deleted     IS 'Logical delete flag';
COMMENT ON COLUMN sys_permission.create_by   IS 'Creator ID';
COMMENT ON COLUMN sys_permission.create_time IS 'Create time';
COMMENT ON COLUMN sys_permission.update_by   IS 'Updater ID';
COMMENT ON COLUMN sys_permission.update_time IS 'Update time';

-- ----------------------------
-- Table: sys_organization
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_organization (
    id          VARCHAR(64)  PRIMARY KEY,
    parent_id   VARCHAR(64)  DEFAULT '0',
    org_code    VARCHAR(64)  NOT NULL,
    org_name    VARCHAR(128),
    org_type    SMALLINT     DEFAULT 1,
    leader_id   VARCHAR(64),
    phone       VARCHAR(32),
    email       VARCHAR(128),
    sort        INT          DEFAULT 0,
    status      SMALLINT     DEFAULT 1,
    remark      VARCHAR(512),
    version     INT          DEFAULT 0,
    deleted     BOOLEAN      DEFAULT FALSE,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    CONSTRAINT uk_sys_organization_org_code UNIQUE (org_code)
);

COMMENT ON TABLE  sys_organization             IS 'Organization / department table';
COMMENT ON COLUMN sys_organization.id          IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_organization.parent_id   IS 'Parent organization ID';
COMMENT ON COLUMN sys_organization.org_code    IS 'Organization code (unique)';
COMMENT ON COLUMN sys_organization.org_name    IS 'Organization name';
COMMENT ON COLUMN sys_organization.org_type    IS 'Organization type: 1=company, 2=department, 3=team';
COMMENT ON COLUMN sys_organization.leader_id   IS 'Leader user ID';
COMMENT ON COLUMN sys_organization.phone       IS 'Contact phone';
COMMENT ON COLUMN sys_organization.email       IS 'Contact email';
COMMENT ON COLUMN sys_organization.sort        IS 'Sort order';
COMMENT ON COLUMN sys_organization.status      IS 'Status: 0=disabled, 1=enabled';
COMMENT ON COLUMN sys_organization.remark      IS 'Remark';
COMMENT ON COLUMN sys_organization.version     IS 'Optimistic lock version';
COMMENT ON COLUMN sys_organization.deleted     IS 'Logical delete flag';
COMMENT ON COLUMN sys_organization.create_by   IS 'Creator ID';
COMMENT ON COLUMN sys_organization.create_time IS 'Create time';
COMMENT ON COLUMN sys_organization.update_by   IS 'Updater ID';
COMMENT ON COLUMN sys_organization.update_time IS 'Update time';

-- ----------------------------
-- Table: sys_resource
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_resource (
    id             VARCHAR(64)  PRIMARY KEY,
    res_name       VARCHAR(128),
    res_type       SMALLINT     DEFAULT 1,
    request_method VARCHAR(16),
    request_path   VARCHAR(256),
    status         SMALLINT     DEFAULT 1,
    remark         VARCHAR(512),
    version        INT          DEFAULT 0,
    deleted        BOOLEAN      DEFAULT FALSE,
    create_by      VARCHAR(64),
    create_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by      VARCHAR(64),
    update_time    TIMESTAMP
);

COMMENT ON TABLE  sys_resource                IS 'API resource table';
COMMENT ON COLUMN sys_resource.id             IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_resource.res_name       IS 'Resource name';
COMMENT ON COLUMN sys_resource.res_type       IS 'Resource type: 1=api, 2=button';
COMMENT ON COLUMN sys_resource.request_method IS 'HTTP request method';
COMMENT ON COLUMN sys_resource.request_path   IS 'API request path';
COMMENT ON COLUMN sys_resource.status         IS 'Status: 0=disabled, 1=enabled';
COMMENT ON COLUMN sys_resource.remark         IS 'Remark';
COMMENT ON COLUMN sys_resource.version        IS 'Optimistic lock version';
COMMENT ON COLUMN sys_resource.deleted        IS 'Logical delete flag';
COMMENT ON COLUMN sys_resource.create_by      IS 'Creator ID';
COMMENT ON COLUMN sys_resource.create_time    IS 'Create time';
COMMENT ON COLUMN sys_resource.update_by      IS 'Updater ID';
COMMENT ON COLUMN sys_resource.update_time    IS 'Update time';

-- ----------------------------
-- Table: sys_user_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    role_id     VARCHAR(64) NOT NULL,
    version     INT          DEFAULT 0,
    deleted     BOOLEAN      DEFAULT FALSE,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

COMMENT ON TABLE  sys_user_role            IS 'User-role relation table';
COMMENT ON COLUMN sys_user_role.id         IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_user_role.user_id    IS 'User ID';
COMMENT ON COLUMN sys_user_role.role_id    IS 'Role ID';
COMMENT ON COLUMN sys_user_role.version    IS 'Optimistic lock version';
COMMENT ON COLUMN sys_user_role.deleted    IS 'Logical delete flag';
COMMENT ON COLUMN sys_user_role.create_by  IS 'Creator ID';
COMMENT ON COLUMN sys_user_role.create_time IS 'Create time';
COMMENT ON COLUMN sys_user_role.update_by  IS 'Updater ID';
COMMENT ON COLUMN sys_user_role.update_time IS 'Update time';

-- ----------------------------
-- Table: sys_role_permission
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            VARCHAR(64) PRIMARY KEY,
    role_id       VARCHAR(64) NOT NULL,
    permission_id VARCHAR(64) NOT NULL,
    version       INT          DEFAULT 0,
    deleted       BOOLEAN      DEFAULT FALSE,
    create_by     VARCHAR(64),
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(64),
    update_time   TIMESTAMP,
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE  sys_role_permission               IS 'Role-permission relation table';
COMMENT ON COLUMN sys_role_permission.id            IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_role_permission.role_id       IS 'Role ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS 'Permission ID';
COMMENT ON COLUMN sys_role_permission.version       IS 'Optimistic lock version';
COMMENT ON COLUMN sys_role_permission.deleted       IS 'Logical delete flag';
COMMENT ON COLUMN sys_role_permission.create_by     IS 'Creator ID';
COMMENT ON COLUMN sys_role_permission.create_time   IS 'Create time';
COMMENT ON COLUMN sys_role_permission.update_by     IS 'Updater ID';
COMMENT ON COLUMN sys_role_permission.update_time   IS 'Update time';

-- ----------------------------
-- Table: sys_user_org
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_org (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    org_id      VARCHAR(64) NOT NULL,
    version     INT          DEFAULT 0,
    deleted     BOOLEAN      DEFAULT FALSE,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(64),
    update_time TIMESTAMP,
    CONSTRAINT uk_sys_user_org UNIQUE (user_id, org_id)
);

COMMENT ON TABLE  sys_user_org            IS 'User-organization relation table';
COMMENT ON COLUMN sys_user_org.id         IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_user_org.user_id    IS 'User ID';
COMMENT ON COLUMN sys_user_org.org_id     IS 'Organization ID';
COMMENT ON COLUMN sys_user_org.version    IS 'Optimistic lock version';
COMMENT ON COLUMN sys_user_org.deleted    IS 'Logical delete flag';
COMMENT ON COLUMN sys_user_org.create_by  IS 'Creator ID';
COMMENT ON COLUMN sys_user_org.create_time IS 'Create time';
COMMENT ON COLUMN sys_user_org.update_by  IS 'Updater ID';
COMMENT ON COLUMN sys_user_org.update_time IS 'Update time';
