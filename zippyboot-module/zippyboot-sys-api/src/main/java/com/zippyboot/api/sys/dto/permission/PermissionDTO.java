package com.zippyboot.api.sys.dto.permission;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionDTO {

    String id;
    String parentId;
    String permCode;
    String permName;
    Integer permType;
    String path;
    String component;
    String icon;
    Integer sort;
    Boolean visible;
    Integer status;
}
