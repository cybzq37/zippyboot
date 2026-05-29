package com.zippyboot.api.sys.dto.org;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
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
