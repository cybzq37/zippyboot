package com.zippyboot.api.sys.dto.org;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgDTO {

    String id;
    String parentId;
    String orgCode;
    String orgName;
    Integer orgType;
    String leaderId;
    String phone;
    String email;
    Integer sort;
    Integer status;
}
