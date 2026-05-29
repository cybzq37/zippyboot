package com.zippy.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zippy.sys.entity.SysPermission;

import java.util.List;

public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 根据用户ID查询权限编码列表
     */
    List<String> getPermCodesByUserId(String userId);

    /**
     * 根据用户ID查询权限列表
     */
    List<SysPermission> getPermsByUserId(String userId);
}
