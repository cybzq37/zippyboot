package com.zyn.sys.controller;

import com.zyn.kit.response.ApiResponse;
import com.zyn.kit.util.BeanUtils;
import com.zyn.sys.entity.SysResource;
import com.zyn.sys.service.SysResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resource")
public class SysResourceController {

    private final SysResourceService resourceService;

    @GetMapping
    public ApiResponse<List<SysResource>> list() {
        List<SysResource> resources = resourceService.list();
        return ApiResponse.ok(resources);
    }

    @GetMapping("/{id}")
    public ApiResponse<SysResource> getById(@PathVariable String id) {
        SysResource resource = resourceService.getById(id);
        return ApiResponse.ok(resource);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody SysResource resource) {
        resourceService.save(resource);
        return ApiResponse.ok(null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody SysResource resource) {
        resourceService.updateById(resource);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        resourceService.removeById(id);
        return ApiResponse.ok(null);
    }
}
