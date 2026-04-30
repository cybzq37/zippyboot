package com.zippyboot.infra.postgres.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zippyboot.infra.postgres.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
