package com.zippyboot.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zippyboot.sys.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = false")
    SysUser selectByUsername(@Param("username") String username);
}
