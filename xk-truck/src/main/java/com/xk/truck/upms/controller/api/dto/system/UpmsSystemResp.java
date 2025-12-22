package com.xk.truck.upms.controller.api.dto.system;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class UpmsSystemResp {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean enabled;

    private ZonedDateTime createdTime;
    private ZonedDateTime updatedTime;

    private String createdBy;
    private String updatedBy;
}
