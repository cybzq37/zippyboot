package com.zyn.api.sys.dto.org;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OrgTreeDTO {

    String id;
    String parentId;
    String orgCode;
    String orgName;
    Integer orgType;
    Integer sort;
    List<OrgTreeDTO> children;
}
