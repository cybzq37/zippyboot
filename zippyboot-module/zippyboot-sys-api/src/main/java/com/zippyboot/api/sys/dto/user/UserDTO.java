package com.zippyboot.api.sys.dto.user;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDTO {

    String id;
    String username;
    String nickname;
    String realName;
    String email;
    String phone;
    String avatar;
    Integer gender;
    Integer status;
}
