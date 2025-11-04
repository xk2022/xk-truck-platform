package com.xk.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * 🔧 XkBeanUtils 工具類
 * 提供對象屬性拷貝的通用方法，包括單個對象和集合拷貝，
 * 支援：
 *  - 忽略 null 屬性
 *  - 常見型別自動轉換（String ↔ Long / Integer / Boolean / UUID）
 *  - 集合拷貝與回調自訂邏輯
 *
 * 適用於：
 *  - DTO ↔ Entity ↔ VO 轉換
 *  - 部分更新時忽略 null 欄位
 *
 *  // 單一物件拷貝
 * UserResp resp = XkBeanUtils.copyProperties(userEntity, UserResp::new);
 * // 集合拷貝
 * List<UserResp> list = XkBeanUtils.copyListProperties(userEntities, UserResp::new);
 * // 集合拷貝 + 回調
 * List<UserResp> list = XkBeanUtils.copyListProperties(userEntities, UserResp::new,
 *     (entity, resp) -> resp.setDisplayName(entity.getUsername().toUpperCase()));
 *
 * @author yuan
 */
@Slf4j
public class XkBeanUtils {

    private XkBeanUtils() {
    }

    // =========================
    // 🧩 單一對象拷貝
    // =========================

    /**
     * 單個對象的屬性拷貝（含型別自動轉換）
     */
    public static <S, T> T copyProperties(S source, Supplier<T> targetSupplier) {
        if (source == null) return null;
        T target = targetSupplier.get();

        try {
            BeanUtils.copyProperties(source, target);
            copyPropertiesAutoConvert(source, target);
        } catch (Exception e) {
            log.error("❌ Bean copy failed: {} → {}", source.getClass().getSimpleName(),
                    target.getClass().getSimpleName(), e);
        }

        return target;
    }

    /**
     * 單個對象的屬性拷貝（忽略 null 屬性）
     */
    public static void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) return;

        String[] nullProps = getNullPropertyNames(source);
        BeanUtils.copyProperties(source, target, nullProps);
    }

    // =========================
    // 🧮 集合拷貝
    // =========================

    /**
     * 集合拷貝（自動型別轉換）
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> targetSupplier) {
        if (sources == null || sources.isEmpty()) return new ArrayList<>();

        List<T> result = new ArrayList<>(sources.size());
        for (S source : sources) {
            result.add(copyProperties(source, targetSupplier));
        }
        return result;
    }

    /**
     * 集合拷貝（帶回調）
     */
    public static <S, T> List<T> copyListProperties(List<S> sources,
                                                    Supplier<T> targetSupplier,
                                                    XkBeanUtilsCallBack<S, T> callback) {
        if (sources == null || sources.isEmpty()) return new ArrayList<>();

        List<T> result = new ArrayList<>(sources.size());
        for (S source : sources) {
            T target = copyProperties(source, targetSupplier);
            if (callback != null) {
                callback.callBack(source, target);
            }
            result.add(target);
        }
        return result;
    }

    // =========================
    // 🔄 型別轉換與輔助工具
    // =========================

    /**
     * 自動型別轉換（String <-> Long / Integer / Boolean / UUID）
     */
    public static void copyPropertiesAutoConvert(Object source, Object target, String... nullPropertyNames) {
        if (source == null || target == null) return;

        Field[] sourceFields = source.getClass().getDeclaredFields();
        Field[] targetFields = target.getClass().getDeclaredFields();
        String[] excludedProps = getNullPropertyNames(source, nullPropertyNames);

        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            try {
                Object sourceValue = sourceField.get(source);
                if (sourceValue == null || StringUtils.isBlank(String.valueOf(sourceValue))) continue;

                for (Field targetField : targetFields) {
                    targetField.setAccessible(true);
                    if (sourceField.getName().equals(targetField.getName())
                            && !isExcluded(sourceField.getName(), excludedProps)) {
                        convertAndSet(target, sourceValue, targetField, sourceField);
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Skip field {} due to error: {}", sourceField.getName(), e.getMessage());
            }
        }
    }

    /**
     * 屬性型別自動轉換（常見型別）
     */
    private static void convertAndSet(Object target, Object sourceValue, Field targetField, Field sourceField)
            throws IllegalAccessException {
        Class<?> t = targetField.getType();
        Class<?> s = sourceField.getType();

        if (t.equals(String.class) && s.equals(Long.class))
            targetField.set(target, sourceValue.toString());
        else if (t.equals(Long.class) && s.equals(String.class))
            targetField.set(target, Long.valueOf((String) sourceValue));
        else if (t.equals(String.class) && s.equals(Integer.class))
            targetField.set(target, sourceValue.toString());
        else if (t.equals(Integer.class) && s.equals(String.class))
            targetField.set(target, Integer.valueOf((String) sourceValue));
        else if (t.equals(String.class) && s.equals(Boolean.class))
            targetField.set(target, sourceValue.toString());
        else if (t.equals(Boolean.class) && s.equals(String.class))
            targetField.set(target, Boolean.parseBoolean((String) sourceValue));
        else if (t.equals(String.class) && s.equals(UUID.class))
            targetField.set(target, sourceValue.toString());
        else if (t.equals(UUID.class) && s.equals(String.class))
            targetField.set(target, UUID.fromString((String) sourceValue));
    }

    // =========================
    // 🧰 私有輔助方法
    // =========================

    private static boolean isExcluded(String fieldName, String[] excludedProperties) {
        if (excludedProperties == null) return false;
        for (String exclude : excludedProperties) {
            if (fieldName.equals(exclude)) return true;
        }
        return false;
    }

    private static String[] getNullPropertyNames(Object source, String... additionalExcludes) {
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(source.getClass())) {
            try {
                Field field = getFieldRecursively(source.getClass(), pd.getName());
                if (field != null) {
                    field.setAccessible(true);
                    if (field.get(source) == null) emptyNames.add(pd.getName());
                }
            } catch (Exception ignored) {
            }
        }
        if (additionalExcludes != null) emptyNames.addAll(Arrays.asList(additionalExcludes));
        return emptyNames.toArray(new String[0]);
    }

    private static Field getFieldRecursively(Class<?> clazz, String fieldName) {
        if (clazz == null) return null;
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getFieldRecursively(clazz.getSuperclass(), fieldName);
        }
    }

    private static String[] getNullPropertyNames(Object source) {
        return getNullPropertyNames(source, (String[]) null);
    }

}
