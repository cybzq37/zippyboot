package com.zyn.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyn.sys.entity.SysUser;
import com.zyn.sys.mapper.SysUserMapper;
import com.zyn.sys.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public void updateLoginInfo(String userId, String loginIp) {
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getLoginIp, loginIp)
                .set(SysUser::getLoginTime, LocalDateTime.now())
                .update();
    }
}
