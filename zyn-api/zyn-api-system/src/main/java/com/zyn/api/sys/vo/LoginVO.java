package com.zyn.api.sys.vo;

import com.zyn.api.sys.dto.user.UserDTO;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginVO {

    String token;
    UserDTO userInfo;
}
