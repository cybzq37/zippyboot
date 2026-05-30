package com.zyn.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyn.sys.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> getRolesByUserId(String userId);
}
