package com.xk.truck.fms.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "車輛狀態")
public enum VehicleStatus {

    @Schema(description = "可接單、未執行任何任務（空車）")
    AVAILABLE,

    @Schema(description = "執行中，有指派的任務")
    BUSY,

    @Schema(description = "維修中，不可指派")
    MAINTENANCE,

    @Schema(description = "已預約，未開始執行（例如明天有排程）")
    RESERVED,

    @Schema(description = "報廢 / 永久停用")
    SCRAPPED,

    INACTIVE
}
