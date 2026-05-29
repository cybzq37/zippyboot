package com.zippyboot.api.sys.dto.role;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoleDTO {

    String id;
    String roleCode;
    String roleName;
    Integer roleType;
    Integer sort;
    Integer status;
    Integer dataScope;
    String remark;
}
