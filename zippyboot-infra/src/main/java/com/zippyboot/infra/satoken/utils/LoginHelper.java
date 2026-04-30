package com.zippyboot.infra.satoken.utils;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 登录鉴权助手
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app
 * deivce 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致
 * 可以组成 多用户类型表与多设备类型 分别控制权限
 *
 * @author lichunqing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class LoginHelper {

    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String USER_KEY = "userId";

    /**
     * 登录系统（通用）
     */
    public static void login(Object loginId, Long userId, Object loginUser) {
        loginByDevice(loginId, userId, loginUser, null);
    }

    /**
     * 登录系统 基于 设备类型
     * 针对相同用户体系不同设备
     */
    public static void loginByDevice(Object loginId, Long userId, Object loginUser, String device) {
        if (loginId == null) {
            throw new IllegalArgumentException("loginId must not be null");
        }
        SaStorage storage = SaHolder.getStorage();
        storage.set(LOGIN_USER_KEY, loginUser);
        storage.set(USER_KEY, userId);
        SaLoginModel model = new SaLoginModel();
        if (device != null && !device.trim().isEmpty()) {
            model.setDevice(device);
        }
        // 自定义分配, 不设置默认走全局 yml 配置
        // model.setTimeout(86400).setActiveTimeout(1800);
        StpUtil.login(loginId, model.setExtra(USER_KEY, userId));
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取用户(多级缓存)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getLoginUser() {
        T loginUser = (T) SaHolder.getStorage().get(LOGIN_USER_KEY);
        if (loginUser != null) {
            return loginUser;
        }
        SaSession session = StpUtil.getTokenSession();
        if (session == null) {
            return null;
        }
        loginUser = (T) session.get(LOGIN_USER_KEY);
        SaHolder.getStorage().set(LOGIN_USER_KEY, loginUser);
        return loginUser;
    }

    /**
     * 获取用户基于token
     */
    @SuppressWarnings("unchecked")
    public static <T> T getLoginUser(String token) {
        SaSession session = StpUtil.getTokenSessionByToken(token);
        if (session == null) {
            return null;
        }
        return (T) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户id
     */
    public static Long getUserId() {
        Object userIdObj;
        try {
            userIdObj = SaHolder.getStorage().get(USER_KEY);
            if (userIdObj == null) {
                userIdObj = StpUtil.getExtra(USER_KEY);
                SaHolder.getStorage().set(USER_KEY, userIdObj);
            }
        } catch (Exception e) {
            log.warn("get userId failed", e);
            return null;
        }
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(userIdObj));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isRoot(Long userId) {
        return userId != null && userId == 1L;
    }

    public static boolean isRoot() {
        return isRoot(getUserId());
    }

}
