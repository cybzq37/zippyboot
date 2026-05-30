package com.zyn.kit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean 拷贝工具（基于 cglib BeanCopier，高性能）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtils {

    private static final Map<String, BeanCopier> COPIER_CACHE = new ConcurrentHashMap<>();

    // ==================== 拷贝 ====================

    /**
     * 新建目标对象并执行属性拷贝。
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) return null;
        T target = newInstance(targetClass);
        copyProperties(source, target);
        return target;
    }

    /**
     * 拷贝到已存在的目标对象。
     */
    public static <T, V> V copy(T source, V target) {
        if (source == null || target == null) return target;
        copyProperties(source, target);
        return target;
    }

    /**
     * 新建目标对象并忽略源对象中的 null 属性。
     */
    public static <T> T copyIgnoreNull(Object source, Class<T> targetClass) {
        if (source == null) return null;
        T target = newInstance(targetClass);
        copyPropertiesIgnoreNull(source, target);
        return target;
    }

    /**
     * 普通属性拷贝（不忽略 null）。
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) return;
        getCopier(source.getClass(), target.getClass()).copy(source, target, null);
    }

    /**
     * 属性拷贝，忽略指定字段。
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) return;
        if (ignoreProperties == null || ignoreProperties.length == 0) {
            copyProperties(source, target);
            return;
        }
        Set<String> ignoreSet = toSet(ignoreProperties);
        BeanMap sourceMap = BeanMap.create(source);
        BeanMap targetMap = BeanMap.create(target);
        for (Object keyObj : sourceMap.keySet()) {
            String key = String.valueOf(keyObj);
            if ("class".equals(key) || ignoreSet.contains(key) || !targetMap.containsKey(key)) continue;
            try {
                targetMap.put(key, sourceMap.get(key));
            } catch (RuntimeException ignored) {
            }
        }
    }

    /**
     * 忽略 null 的属性拷贝。
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        copyPropertiesIgnoreNull(source, target, new String[0]);
    }

    /**
     * 忽略 null 的属性拷贝，并支持忽略指定字段。
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) return;
        Set<String> ignoreSet = toSet(ignoreProperties);
        BeanMap sourceMap = BeanMap.create(source);
        BeanMap targetMap = BeanMap.create(target);
        for (Object keyObj : sourceMap.keySet()) {
            String key = String.valueOf(keyObj);
            if ("class".equals(key) || ignoreSet.contains(key) || !targetMap.containsKey(key)) continue;
            Object value = sourceMap.get(key);
            if (value == null) continue;
            try {
                targetMap.put(key, value);
            } catch (RuntimeException ignored) {
            }
        }
    }

    // ==================== 列表拷贝 ====================

    /**
     * 列表拷贝到新列表。
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) return Collections.emptyList();
        return sourceList.stream().map(s -> copy(s, targetClass)).collect(Collectors.toList());
    }

    /**
     * 列表拷贝到新列表，忽略 null 属性。
     */
    public static <T> List<T> copyListIgnoreNull(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) return Collections.emptyList();
        return sourceList.stream().map(s -> copyIgnoreNull(s, targetClass)).collect(Collectors.toList());
    }

    /**
     * 列表按索引拷贝到已存在的目标列表。
     */
    public static <S, T> List<T> copyList(List<S> sourceList, List<T> targetList) {
        if (sourceList == null || sourceList.isEmpty() || targetList == null || targetList.isEmpty()) {
            return targetList == null ? Collections.emptyList() : targetList;
        }
        int size = Math.min(sourceList.size(), targetList.size());
        for (int i = 0; i < size; i++) {
            S source = sourceList.get(i);
            T target = targetList.get(i);
            if (source != null && target != null) copyProperties(source, target);
        }
        return targetList;
    }

    /**
     * 列表拷贝，支持自定义转换函数。
     */
    public static <S, T> List<T> mapList(List<S> sourceList, Function<S, T> mapper) {
        if (sourceList == null || sourceList.isEmpty()) return Collections.emptyList();
        return sourceList.stream().filter(Objects::nonNull).map(mapper).collect(Collectors.toList());
    }

    // ==================== Bean ↔ Map ====================

    /**
     * Bean 转 Map（包含 null 值字段）。
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) return Collections.emptyMap();
        BeanMap beanMap = BeanMap.create(bean);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Object keyObj : beanMap.keySet()) {
            String key = String.valueOf(keyObj);
            if ("class".equals(key)) continue;
            result.put(key, beanMap.get(key));
        }
        return result;
    }

    /**
     * Bean 转 Map，过滤 null 值字段。
     */
    public static Map<String, Object> beanToMapIgnoreNull(Object bean) {
        if (bean == null) return Collections.emptyMap();
        BeanMap beanMap = BeanMap.create(bean);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Object keyObj : beanMap.keySet()) {
            String key = String.valueOf(keyObj);
            if ("class".equals(key)) continue;
            Object value = beanMap.get(key);
            if (value != null) result.put(key, value);
        }
        return result;
    }

    /**
     * Map 转新建 Bean。
     */
    public static <T> T mapToBean(Map<String, ?> map, Class<T> targetClass) {
        T target = newInstance(targetClass);
        mapToBean(map, target);
        return target;
    }

    /**
     * Map 拷贝到已存在 Bean。
     */
    public static void mapToBean(Map<String, ?> map, Object target) {
        if (map == null || map.isEmpty() || target == null) return;
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!wrapper.isWritableProperty(entry.getKey())) continue;
            try {
                wrapper.setPropertyValue(entry.getKey(), entry.getValue());
            } catch (RuntimeException ignored) {
            }
        }
    }

    /**
     * Map 拷贝到已存在 Bean，忽略 null 值。
     */
    public static void mapToBeanIgnoreNull(Map<String, ?> map, Object target) {
        if (map == null || map.isEmpty() || target == null) return;
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() == null) continue;
            if (!wrapper.isWritableProperty(entry.getKey())) continue;
            try {
                wrapper.setPropertyValue(entry.getKey(), entry.getValue());
            } catch (RuntimeException ignored) {
            }
        }
    }

    // ==================== Map 工具 ====================

    /**
     * 创建一个新的 Map 副本。
     */
    public static Map<String, Object> copyMap(Map<String, ?> source) {
        if (source == null || source.isEmpty()) return Collections.emptyMap();
        return new LinkedHashMap<>(source);
    }

    /**
     * Map value 按指定类型进行对象拷贝转换。
     */
    public static <T, V> Map<String, V> mapToMap(Map<String, T> map, Class<V> clazz) {
        if (map == null || map.isEmpty() || clazz == null) return Collections.emptyMap();
        Map<String, V> result = new LinkedHashMap<>(map.size());
        map.forEach((key, value) -> result.put(key, copy(value, clazz)));
        return result;
    }

    /**
     * 合并多个 Map，后者覆盖前者。
     */
    @SafeVarargs
    public static <K, V> Map<K, V> mergeMaps(Map<K, V>... maps) {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map<K, V> map : maps) {
            if (map != null) result.putAll(map);
        }
        return result;
    }

    // ==================== 属性提取 ====================

    /**
     * 获取对象中值为 null 的属性名集合。
     */
    public static Set<String> getNullPropertyNames(Object source) {
        if (source == null) return Collections.emptySet();
        BeanWrapper wrapper = new BeanWrapperImpl(source);
        Set<String> names = new HashSet<>();
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if ("class".equals(pd.getName())) continue;
            if (wrapper.getPropertyValue(pd.getName()) == null) names.add(pd.getName());
        }
        return names;
    }

    /**
     * 获取指定属性值。
     */
    public static Object getProperty(Object bean, String propertyName) {
        if (bean == null || propertyName == null) return null;
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        if (!wrapper.isReadableProperty(propertyName)) return null;
        return wrapper.getPropertyValue(propertyName);
    }

    /**
     * 设置指定属性值。
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        if (bean == null || propertyName == null) return;
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        if (!wrapper.isWritableProperty(propertyName)) return;
        wrapper.setPropertyValue(propertyName, value);
    }

    /**
     * 获取 Bean 的所有属性名。
     */
    public static Set<String> getPropertyNames(Object bean) {
        if (bean == null) return Collections.emptySet();
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        Set<String> names = new LinkedHashSet<>();
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (!"class".equals(pd.getName())) names.add(pd.getName());
        }
        return names;
    }

    /**
     * 判断两个 Bean 属性值是否全部相等。
     */
    public static boolean propertiesEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return beanToMap(a).equals(beanToMap(b));
    }

    // ==================== 内部 ====================

    private static BeanCopier getCopier(Class<?> sourceClass, Class<?> targetClass) {
        String key = sourceClass.getName() + "->" + targetClass.getName();
        return COPIER_CACHE.computeIfAbsent(key, k -> BeanCopier.create(sourceClass, targetClass, false));
    }

    private static Set<String> toSet(String... values) {
        if (values == null || values.length == 0) return Collections.emptySet();
        Set<String> set = new HashSet<>();
        Collections.addAll(set, values);
        return set;
    }

    private static <T> T newInstance(Class<T> targetClass) {
        if (targetClass == null) throw new IllegalArgumentException("targetClass cannot be null");
        try {
            return targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot instantiate: " + targetClass.getName(), e);
        }
    }
}
