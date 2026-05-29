package com.zippy.api.sys.dto.role;

import lombok.Data;

@Data
public class RoleQuery {

    private String roleCode;
    private String roleName;
    private Integer status;
}
