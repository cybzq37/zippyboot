package com.zippy.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zippy.sys.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 更新登录信息（登录IP、登录时间）
     */
    void updateLoginInfo(String userId, String loginIp);
}
