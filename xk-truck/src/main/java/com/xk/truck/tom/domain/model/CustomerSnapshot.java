package com.xk.truck.tom.domain.model;

import lombok.Data;

import java.util.UUID;

/**
 * ===============================================================
 * Value Object : CustomerSnapshot
 * Purpose      : 訂單建立當下的客戶快照
 * Notes        :
 * - 避免跨 BC 直接關聯 Customer Aggregate
 * - 保留歷史（即使客戶改名，舊訂單仍正確）
 * ===============================================================
 */
@Data
public class CustomerSnapshot {

    private UUID customerUuid;
    private String customerName;
}
