package com.zippyboot.sys.controller;

import com.zippyboot.api.sys.dto.org.OrgDTO;
import com.zippyboot.api.sys.dto.org.OrgTreeDTO;
import com.zippyboot.kit.response.ApiResponse;
import com.zippyboot.kit.util.BeanUtils;
import com.zippyboot.sys.entity.SysOrganization;
import com.zippyboot.sys.manager.OrgManager;
import com.zippyboot.sys.service.SysOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/org")
public class SysOrganizationController {

    private final SysOrganizationService organizationService;
    private final OrgManager orgManager;

    @GetMapping
    public ApiResponse<List<OrgDTO>> list() {
        List<SysOrganization> orgs = organizationService.list();
        return ApiResponse.ok(BeanUtils.copyList(orgs, OrgDTO.class));
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeDTO>> tree() {
        return ApiResponse.ok(orgManager.getOrgTree());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrgDTO> getById(@PathVariable String id) {
        SysOrganization org = organizationService.getById(id);
        return ApiResponse.ok(BeanUtils.copy(org, OrgDTO.class));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody OrgDTO orgDTO) {
        SysOrganization org = BeanUtils.copy(orgDTO, SysOrganization.class);
        organizationService.save(org);
        return ApiResponse.ok(null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody OrgDTO orgDTO) {
        SysOrganization org = BeanUtils.copy(orgDTO, SysOrganization.class);
        organizationService.updateById(org);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        organizationService.removeById(id);
        return ApiResponse.ok(null);
    }
}
