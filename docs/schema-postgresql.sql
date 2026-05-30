-- ============================================================
-- Zyn System Module - PostgreSQL Schema
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
    login_attempts  INT          DEFAULT 0,
    lock_time       TIMESTAMP,
    remark          VARCHAR(512),
    version         INT          DEFAULT 0,
    deleted         BOOLEAN      DEFAULT FALSE,
    create_by       VARCHAR(64),
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(64),
    update_time     TIMESTAMP,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE INDEX idx_sys_user_phone ON sys_user(phone);
CREATE INDEX idx_sys_user_email ON sys_user(email);
CREATE INDEX idx_sys_user_status ON sys_user(status) WHERE deleted = FALSE;

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
COMMENT ON COLUMN sys_user.login_attempts IS 'Failed login attempts (reset on success)';
COMMENT ON COLUMN sys_user.lock_time      IS 'Account lock time (null = unlocked)';
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

CREATE INDEX idx_sys_role_status ON sys_role(status) WHERE deleted = FALSE;

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

CREATE INDEX idx_sys_permission_parent_id ON sys_permission(parent_id);
CREATE INDEX idx_sys_permission_type ON sys_permission(perm_type) WHERE deleted = FALSE;

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

CREATE INDEX idx_sys_organization_parent_id ON sys_organization(parent_id);

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
    permission_id  VARCHAR(64),
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

CREATE INDEX idx_sys_resource_permission_id ON sys_resource(permission_id);
CREATE INDEX idx_sys_resource_path ON sys_resource(request_path, request_method) WHERE deleted = FALSE;

COMMENT ON TABLE  sys_resource                IS 'API resource table';
COMMENT ON COLUMN sys_resource.id             IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_resource.permission_id  IS 'Associated permission ID';
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
-- Table: sys_user_role (关联表，物理删除)
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    role_id     VARCHAR(64) NOT NULL,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);

COMMENT ON TABLE  sys_user_role            IS 'User-role relation table';
COMMENT ON COLUMN sys_user_role.id         IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_user_role.user_id    IS 'User ID';
COMMENT ON COLUMN sys_user_role.role_id    IS 'Role ID';
COMMENT ON COLUMN sys_user_role.create_by  IS 'Creator ID';
COMMENT ON COLUMN sys_user_role.create_time IS 'Create time';

-- ----------------------------
-- Table: sys_role_permission (关联表，物理删除)
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            VARCHAR(64) PRIMARY KEY,
    role_id       VARCHAR(64) NOT NULL,
    permission_id VARCHAR(64) NOT NULL,
    create_by     VARCHAR(64),
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_sys_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX idx_sys_role_permission_perm_id ON sys_role_permission(permission_id);

COMMENT ON TABLE  sys_role_permission               IS 'Role-permission relation table';
COMMENT ON COLUMN sys_role_permission.id            IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_role_permission.role_id       IS 'Role ID';
COMMENT ON COLUMN sys_role_permission.permission_id IS 'Permission ID';
COMMENT ON COLUMN sys_role_permission.create_by     IS 'Creator ID';
COMMENT ON COLUMN sys_role_permission.create_time   IS 'Create time';

-- ----------------------------
-- Table: sys_user_org (关联表，物理删除)
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_org (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL,
    org_id      VARCHAR(64) NOT NULL,
    create_by   VARCHAR(64),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_org UNIQUE (user_id, org_id)
);

CREATE INDEX idx_sys_user_org_user_id ON sys_user_org(user_id);
CREATE INDEX idx_sys_user_org_org_id ON sys_user_org(org_id);

COMMENT ON TABLE  sys_user_org            IS 'User-organization relation table';
COMMENT ON COLUMN sys_user_org.id         IS 'Primary key (UUID)';
COMMENT ON COLUMN sys_user_org.user_id    IS 'User ID';
COMMENT ON COLUMN sys_user_org.org_id     IS 'Organization ID';
COMMENT ON COLUMN sys_user_org.create_by  IS 'Creator ID';
COMMENT ON COLUMN sys_user_org.create_time IS 'Create time';

-- ----------------------------
-- Table: sys_audit_log (审计日志，只追加不修改)
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id             BIGSERIAL    PRIMARY KEY,
    user_id        VARCHAR(64),
    username       VARCHAR(64),
    operation      VARCHAR(128),
    method         VARCHAR(256),
    params         TEXT,
    ip             VARCHAR(64),
    user_agent     VARCHAR(512),
    status         SMALLINT     DEFAULT 1,
    error_msg      TEXT,
    duration_ms    BIGINT,
    create_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user_id ON sys_audit_log(user_id);
CREATE INDEX idx_audit_log_create_time ON sys_audit_log(create_time);

COMMENT ON TABLE  sys_audit_log             IS 'Audit log table (append-only)';
COMMENT ON COLUMN sys_audit_log.id          IS 'Auto-increment primary key';
COMMENT ON COLUMN sys_audit_log.user_id     IS 'Operator user ID';
COMMENT ON COLUMN sys_audit_log.username    IS 'Operator username';
COMMENT ON COLUMN sys_audit_log.operation   IS 'Operation description';
COMMENT ON COLUMN sys_audit_log.method      IS 'Request method and path';
COMMENT ON COLUMN sys_audit_log.params      IS 'Request parameters (JSON)';
COMMENT ON COLUMN sys_audit_log.ip          IS 'Client IP address';
COMMENT ON COLUMN sys_audit_log.user_agent  IS 'User-Agent string';
COMMENT ON COLUMN sys_audit_log.status      IS 'Result: 0=fail, 1=success';
COMMENT ON COLUMN sys_audit_log.error_msg   IS 'Error message (on failure)';
COMMENT ON COLUMN sys_audit_log.duration_ms IS 'Request duration in milliseconds';
COMMENT ON COLUMN sys_audit_log.create_time IS 'Create time';
