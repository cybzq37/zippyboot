package com.zippy.api.sys.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
