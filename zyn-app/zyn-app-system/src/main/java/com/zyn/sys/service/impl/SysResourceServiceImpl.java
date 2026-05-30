package com.zyn.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyn.sys.entity.SysResource;
import com.zyn.sys.mapper.SysResourceMapper;
import com.zyn.sys.service.SysResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements SysResourceService {
}
