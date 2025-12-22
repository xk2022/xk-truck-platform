package com.xk.truck.tom.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Schema(description = "訂單列表查詢條件（Request Params）")
public class OrderQuery {

    @Schema(description = "訂單類型（IMPORT/EXPORT）")
    private String orderType;

    @Schema(description = "訂單狀態")
    private String orderStatus;

    @Schema(description = "客戶 UUID")
    private String customerUuid;

    @Schema(description = "訂單編號（模糊）")
    private String orderNoLike;

    @Schema(description = "櫃號（模糊）")
    private String containerNoLike;

    @Schema(description = "建立時間起")
    private String createdFrom;

    @Schema(description = "建立時間迄")
    private String createdTo;
}
