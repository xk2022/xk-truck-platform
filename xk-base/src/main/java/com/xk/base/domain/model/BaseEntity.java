package com.xk.base.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * 🧱 BaseEntity — 通用實體基類
 *
 * 提供：
 * - 審計欄位（createdBy, updatedBy, createdTime, updatedTime）
 * - 可選狀態欄位（status, enabled, locked, deleted, remark）
 * - JPA 審計支援（需在啟動類中啟用 @EnableJpaAuditing）
 *
 * 📍 放置於 xk-base 模組，供所有子模組繼承。
 *
 * 例如：
 * <pre>
 * @Entity
 * public class User extends BaseEntity {
 *     private String username;
 * }
 * </pre>
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 審計欄位 ==========

    @Schema(description = "建立者", example = "admin")
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    @Comment("建立者")
    private String createdBy;

    @Schema(description = "建立時間", example = "2025-01-01T10:00:00+08:00[Asia/Taipei]")
    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Comment("建立時間")
    private ZonedDateTime createdTime;

    @Schema(description = "最後修改者", example = "admin")
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    @Comment("最後修改者")
    private String updatedBy;

    @Schema(description = "最後修改時間", example = "2025-01-01T12:00:00+08:00[Asia/Taipei]")
    @UpdateTimestamp
    @Column(name = "updated_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Comment("最後修改時間")
    private ZonedDateTime updatedTime;

    // ========== 可選欄位（依需求開啟） ==========

//    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
//    @Comment("狀態（1:啟用, 0:停用）")
//    private Boolean enabled = true;
//
//    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
//    @Comment("鎖定狀態（0:正常, 1:鎖定）")
//    private Boolean locked = false;
//
//    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
//    @Comment("刪除標記（0:正常, 1:刪除）")
//    private Boolean deleted = false;
//
//    @Schema(description = "刪除時間", example = "2025-01-01T15:30:00+08:00[Asia/Taipei]")
//    @Column(name = "deleted_time")
//    @Comment("刪除時間(軟刪除)")
//    private ZonedDateTime deletedTime;
//
//    @Column(length = 500)
//    @Comment("備註")
//    private String remark;

    // ========== 分組校驗標記（可用於 Validation） ==========

    /** 建立時驗證組 */
    public @interface Create {}

    /** 更新時驗證組 */
    public @interface Update {}
}
