package com.zippyboot.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class HealthDto {
    String status;
    OffsetDateTime time;
}
