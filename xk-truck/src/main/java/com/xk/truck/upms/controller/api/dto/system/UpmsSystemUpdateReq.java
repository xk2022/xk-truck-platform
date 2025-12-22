package com.xk.truck.upms.controller.api.dto.system;

import lombok.Data;

@Data
public class UpmsSystemUpdateReq {
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
}
