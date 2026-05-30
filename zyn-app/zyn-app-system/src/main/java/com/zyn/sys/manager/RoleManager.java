package com.zyn.sys.manager;

import com.zyn.sys.entity.SysRole;
import com.zyn.sys.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色领域编排器，委托 SysRoleService 处理角色相关操作。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleManager {

    private final SysRoleService roleService;

    /**
     * 根据用户ID查询角色列表。
     */
    public List<SysRole> getRolesByUserId(String userId) {
        return roleService.getRolesByUserId(userId);
    }

    /**
     * 根据角色ID查询角色。
     */
    public SysRole getById(String roleId) {
        return roleService.getById(roleId);
    }
}
