package com.xk.truck.fms.domain.model;

public enum DispatchStatus {
    CREATED,       // 建立但未派出
    ASSIGNED,      // 已指派（等待司機處理）
    IN_PROGRESS,   // 司機已出發
    SIGNED,        // 司機已完成簽收（照片上傳完成）
    COMPLETED,     // 後台確認無誤 → 結案
    CANCELLED      // 派工取消
}
