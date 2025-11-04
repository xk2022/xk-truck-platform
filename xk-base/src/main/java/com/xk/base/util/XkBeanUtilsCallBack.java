package com.xk.base.util;

/**
 * XkBeanUtils 回調介面，用於自定義拷貝後的額外處理邏輯。
 */
@FunctionalInterface
public interface XkBeanUtilsCallBack<S, T> {

    /**
     * 在每個 source → target 拷貝完成後執行。
     *
     * @param source 原始資料
     * @param target 拷貝後的目標資料
     */
    void callBack(S source, T target);
}
