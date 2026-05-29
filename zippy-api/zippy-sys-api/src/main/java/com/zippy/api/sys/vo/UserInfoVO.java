package com.zippy.api.sys.vo;

import com.zippy.api.sys.dto.permission.MenuTreeDTO;
import com.zippy.api.sys.dto.user.UserDTO;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserInfoVO {

    UserDTO user;
    List<String> roles;
    List<String> permissions;
    List<MenuTreeDTO> menus;
}
