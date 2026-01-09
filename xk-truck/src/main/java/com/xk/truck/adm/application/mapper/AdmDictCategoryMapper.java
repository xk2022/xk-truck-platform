package com.xk.truck.adm.application.mapper;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.adm.controller.api.dto.CreateDictCategoryReq;
import com.xk.truck.adm.controller.api.dto.DictCategoryResp;
import com.xk.truck.adm.domain.model.AdmDictCategory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ===============================================================
 * Mapper Class : AdmDictCategoryMapper
 * Purpose      :
 * - 負責 DictCategory 相關 DTO ↔ Entity 轉換
 * <p>
 * Design Notes
 * - Mapper 僅做資料轉換，不處理任何業務規則
 * - 所有 null-safe 行為集中在此，避免 Service 層重複判斷
 * - 使用 XkBeanUtils 減少樣板程式碼
 * ===============================================================
 */
@Component
public class AdmDictCategoryMapper {

    /* ==========================================================
     * Create
     * ========================================================== */

    /**
     * Create Request → Entity
     */
    public AdmDictCategory toEntity(CreateDictCategoryReq req) {
        return XkBeanUtils.copyProperties(req, AdmDictCategory::new);
    }

    /* ==========================================================
     * Entity → Response
     * ========================================================== */

    /**
     * Single Entity → Response DTO
     */
    public DictCategoryResp toResp(AdmDictCategory category) {
        DictCategoryResp resp =
                XkBeanUtils.copyProperties(category, DictCategoryResp::new);

        resp.setId(category.getUuid());
//        resp.setItemCount(category.getItems().size()); // 未來擴充
        return resp;
    }

    /**
     * Entity List → List Response
     * - null-safe，避免 Service 再判斷
     */
    public List<DictCategoryResp> toListResp(List<AdmDictCategory> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toResp)
                .toList(); // Java 16+
    }
}
