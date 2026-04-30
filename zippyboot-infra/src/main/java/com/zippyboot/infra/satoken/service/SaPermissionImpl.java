package com.navinfo.common.satoken.core.service;

import cn.dev33.satoken.stp.StpInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * sa-token 权限管理实现类
 *
 * @author lichunqing
 */
public class SaPermissionImpl implements StpInterface {

    /**
     * 获取菜单权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
//        LoginUser loginUser = LoginHelper.getLoginUser();
//        UserType userType = UserType.getUserType(loginUser.getUserType());
//        if (userType == UserType.SYS_USER) {
//            return new ArrayList<>(loginUser.getResources());
//        } else if (userType == UserType.APP_USER) {
//            // 其他端 自行根据业务编写
//        }
        return new ArrayList<>();
    }

    /**
     * 获取角色权限列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
//        LoginUser loginUser = LoginHelper.getLoginUser();
//        UserType userType = UserType.getUserType(loginUser.getUserType());
//        if (userType == UserType.SYS_USER) {
//            return new ArrayList<>(loginUser.getRoles());
//        } else if (userType == UserType.APP_USER) {
//            // 其他端 自行根据业务编写
//        }
        return new ArrayList<>();
    }
}
