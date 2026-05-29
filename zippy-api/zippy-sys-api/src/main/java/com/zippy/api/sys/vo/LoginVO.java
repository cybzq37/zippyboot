package com.zippy.api.sys.vo;

import com.zippy.api.sys.dto.user.UserDTO;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginVO {

    String token;
    UserDTO userInfo;
}
