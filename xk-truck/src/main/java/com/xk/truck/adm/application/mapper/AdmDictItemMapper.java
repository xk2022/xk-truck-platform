package com.xk.truck.adm.application.mapper;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.adm.controller.api.dto.CreateDictItemReq;
import com.xk.truck.adm.controller.api.dto.DictItemResp;
import com.xk.truck.adm.domain.model.AdmDictItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ===============================================================
 * Mapper : AdmDictItemMapper
 * Purpose:
 * - DictItem DTO ↔ Entity 轉換
 * <p>
 * Notes:
 * - Mapper 不處理 category 檢查、不做唯一性驗證
 * ===============================================================
 */
@Component
public class AdmDictItemMapper {

    /* ----------------------------------------------------------
     * Create
     * ---------------------------------------------------------- */

    public AdmDictItem toEntity(CreateDictItemReq req) {
        return XkBeanUtils.copyProperties(req, AdmDictItem::new);
    }

    /* ----------------------------------------------------------
     * Response
     * ---------------------------------------------------------- */

    public DictItemResp toResp(AdmDictItem entity) {
        if (entity == null) return null;

        DictItemResp resp = XkBeanUtils.copyProperties(entity, DictItemResp::new);
        resp.setId(entity.getUuid());               // ⚠️ PK 對齊
        resp.setCategoryId(entity.getCategoryUuid());
        return resp;
    }

    public List<DictItemResp> toListResp(List<AdmDictItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<DictItemResp> list =
                XkBeanUtils.copyListProperties(entities, DictItemResp::new);

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setId(entities.get(i).getUuid());
            list.get(i).setCategoryId(entities.get(i).getCategoryUuid());
        }

        return list;
    }
}
