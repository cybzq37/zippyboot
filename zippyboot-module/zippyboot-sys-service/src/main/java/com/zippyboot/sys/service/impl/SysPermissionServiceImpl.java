package com.zippyboot.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zippyboot.sys.entity.SysPermission;
import com.zippyboot.sys.mapper.SysPermissionMapper;
import com.zippyboot.sys.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public List<String> getPermCodesByUserId(String userId) {
        return baseMapper.selectPermCodesByUserId(userId);
    }

    @Override
    public List<SysPermission> getPermsByUserId(String userId) {
        return baseMapper.selectPermsByUserId(userId);
    }
}
