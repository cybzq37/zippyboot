package com.zyn.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zyn.api.sys.dto.permission.MenuTreeDTO;
import com.zyn.api.sys.dto.permission.PermissionDTO;
import com.zyn.api.sys.dto.permission.PermissionQuery;
import com.zyn.kit.response.ApiResponse;
import com.zyn.kit.util.BeanUtils;
import com.zyn.sys.entity.SysPermission;
import com.zyn.sys.manager.PermissionManager;
import com.zyn.sys.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permission")
public class SysPermissionController {

    private final SysPermissionService permissionService;
    private final PermissionManager permissionManager;

    @GetMapping
    public ApiResponse<List<PermissionDTO>> list(PermissionQuery query) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .like(StringUtils.hasText(query.getPermName()), SysPermission::getPermName, query.getPermName())
                .eq(query.getPermType() != null, SysPermission::getPermType, query.getPermType())
                .eq(query.getStatus() != null, SysPermission::getStatus, query.getStatus())
                .orderByAsc(SysPermission::getSort);
        List<SysPermission> permissions = permissionService.list(wrapper);
        return ApiResponse.ok(BeanUtils.copyList(permissions, PermissionDTO.class));
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeDTO>> tree() {
        return ApiResponse.ok(permissionManager.getPermissionTree());
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionDTO> getById(@PathVariable String id) {
        SysPermission permission = permissionService.getById(id);
        return ApiResponse.ok(BeanUtils.copy(permission, PermissionDTO.class));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody PermissionDTO permissionDTO) {
        SysPermission permission = BeanUtils.copy(permissionDTO, SysPermission.class);
        permissionService.save(permission);
        return ApiResponse.ok(null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody PermissionDTO permissionDTO) {
        SysPermission permission = BeanUtils.copy(permissionDTO, SysPermission.class);
        permissionService.updateById(permission);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        permissionService.removeById(id);
        return ApiResponse.ok(null);
    }
}
