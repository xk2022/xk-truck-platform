package com.xk.truck.adm.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.adm.domain.model.DictCategory;
import com.xk.truck.adm.domain.model.DictItem;

import com.xk.truck.adm.domain.service.DictService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ADM Dictionary")
@RestController
@RequestMapping("/api/adm/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // Category
    @PostMapping("/categories")
    public ApiResult<DictCategory> createCategory(@RequestBody DictCategory req) {
        return ApiResult.success(dictService.createCategory(req), "Category created");
    }

    // Item
    @PostMapping("/categories/{categoryId}/items")
    public ApiResult<DictItem> createItem(@PathVariable UUID categoryId, @RequestBody DictItem req) {
        return ApiResult.success(dictService.createItem(categoryId, req), "Item created");
    }

    // Query items by category code (for FMS drop-downs)
    @GetMapping("/categories/{code}/items/enabled")
    public ApiResult<List<DictItem>> listEnabledItems(@PathVariable String code) {
        return ApiResult.success(dictService.listEnabledItemsByCode(code));
    }
}
