package com.zippyboot.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zippyboot.sys.entity.SysRole;
import com.zippyboot.sys.mapper.SysRoleMapper;
import com.zippyboot.sys.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public List<SysRole> getRolesByUserId(String userId) {
        return baseMapper.selectRolesByUserId(userId);
    }
}
