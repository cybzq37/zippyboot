package com.zyn.api.sys.dto.user;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class LoginUserDTO {

    String userId;
    String username;
    String nickname;
    String realName;
    String email;
    String phone;
    String avatar;
    Integer status;
    Set<String> roleCodes;
    Set<String> permCodes;
}
