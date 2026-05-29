package com.zippyboot.api.sys.vo;

import com.zippyboot.api.sys.dto.user.UserDTO;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginVO {

    String token;
    UserDTO userInfo;
}
