package com.zippy.api.sys.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
