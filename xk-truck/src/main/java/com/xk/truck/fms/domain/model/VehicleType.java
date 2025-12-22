package com.xk.truck.fms.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "車輛類型")
public enum VehicleType {

    @Schema(description = "牽引車頭")
    TRACTOR,                // 車頭（牽引車）

    @Schema(description = "20 尺貨櫃板車 / 拖板")
    TRAILER_FLATBED_20,     // 20 尺板車

    @Schema(description = "40 尺貨櫃板車 / 拖板")
    TRAILER_FLATBED_40,     // 40 尺板車

    @Schema(description = "小貨車 / 三噸半等")
    TRUCK_SMALL,            // 小貨車

    @Schema(description = "一體式貨車（中型、重型）")
    TRUCK_MEDIUM,           // 中型貨車（可依需求調整）

    @Schema(description = "廂型貨車（黑貓、宅配車常用）")
    VAN,                    // 廂型車

    @Schema(description = "冷凍車（保溫 / 冷凍配送）")
    REEFER,                 // 冷凍車

    @Schema(description = "其他未分類車型")
    OTHER
}
