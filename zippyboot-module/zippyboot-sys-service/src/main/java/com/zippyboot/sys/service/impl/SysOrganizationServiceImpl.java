package com.zippyboot.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zippyboot.sys.entity.SysOrganization;
import com.zippyboot.sys.mapper.SysOrganizationMapper;
import com.zippyboot.sys.service.SysOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysOrganizationServiceImpl extends ServiceImpl<SysOrganizationMapper, SysOrganization> implements SysOrganizationService {
}
