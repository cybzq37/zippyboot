package com.zippyboot.kit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.cglib.core.Converter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * bean拷贝工具(基于 cglib 性能优异)
 *
 * @author lichunqing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtils {

    /**
     * 相同属性名称类型转换
     * @param source 源对象
     * @param actualEditable 目标对象类型
     * @param <T> 目标对象泛型
     * @return 返回目标对象
     */
    public static <T> T map(Object source, Class<T> actualEditable) {
        if (source == null || actualEditable == null) {
            return null;
        }
        try {
            T target = actualEditable.getDeclaredConstructor().newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] getNullPropertyNames (Object source) {
        if (source == null) {
            return new String[0];
        }
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 忽略空字段
     * @param src
     * @param target
     */
    public static void copyPropertiesIgnoreNull(Object src, Object target){
        if (src == null || target == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    /**
     * 单对象基于class创建拷贝
     *
     * @param source 数据来源实体
     * @param desc   描述对象 转换后的对象
     * @return desc
     */
    public static <T, V> V copy(T source, Class<V> desc) {
        if (source == null) {
            return null;
        }
        if (desc == null) {
            return null;
        }
        try {
            final V target = desc.getDeclaredConstructor().newInstance();
            return copy(source, target);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 单对象基于对象创建拷贝
     *
     * @param source 数据来源实体
     * @param desc   转换后的对象
     * @return desc
     */
    public static <T, V> V copy(T source, V desc) {
        if (source == null) {
            return null;
        }
        if (desc == null) {
            return null;
        }
        BeanCopier beanCopier = BeanCopierCache.INSTANCE.get(source.getClass(), desc.getClass(), null);
        beanCopier.copy(source, desc, null);
        return desc;
    }

    /**
     * 列表对象基于class创建拷贝
     *
     * @param sourceList 数据来源实体列表
     * @param desc       描述对象 转换后的对象
     * @return desc
     */
    public static <T, V> List<V> copyList(List<T> sourceList, Class<V> desc) {
        if (sourceList == null || desc == null) {
            return Collections.emptyList();
        }
        if (sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream().map(source -> copy(source, desc)).collect(Collectors.toList());
    }

    /**
     * bean拷贝到map
     *
     * @param bean 数据来源实体
     * @return map对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, Object> copyToMap(T bean) {
        if (bean == null) {
            return Collections.emptyMap();
        }
        return BeanMap.create(bean);
    }

    /**
     * map拷贝到bean
     *
     * @param map       数据来源
     * @param beanClass bean类
     * @return bean对象
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        if (beanClass == null) {
            return null;
        }
        try {
            T bean = beanClass.getDeclaredConstructor().newInstance();
            return mapToBean(map, bean);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * map拷贝到bean
     *
     * @param map  数据来源
     * @param bean bean对象
     * @return bean对象
     */
    public static <T> T mapToBean(Map<String, Object> map, T bean) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        if (bean == null) {
            return null;
        }
        BeanMap.create(bean).putAll(map);
        return bean;
    }

    /**
     * map拷贝到map
     *
     * @param map   数据来源
     * @param clazz 返回的对象类型
     * @return map对象
     */
    public static <T, V> Map<String, V> mapToMap(Map<String, T> map, Class<V> clazz) {
        if (map == null || map.isEmpty() || clazz == null) {
            return Collections.emptyMap();
        }
        Map<String, V> copyMap = new LinkedHashMap<>(map.size());
        map.forEach((k, v) -> copyMap.put(k, copy(v, clazz)));
        return copyMap;
    }

    /**
     * BeanCopier属性缓存<br>
     * 缓存用于防止多次反射造成的性能问题
     *
     * @author Looly
     * @since 5.4.1
     */
    public enum BeanCopierCache {
        /**
         * BeanCopier属性缓存单例
         */
        INSTANCE;

        private final Map<String, BeanCopier> cache = new ConcurrentHashMap<>();

        /**
         * 获得类与转换器生成的key在{@link BeanCopier}的Map中对应的元素
         *
         * @param srcClass    源Bean的类
         * @param targetClass 目标Bean的类
         * @param converter   转换器
         * @return Map中对应的BeanCopier
         */
        public BeanCopier get(Class<?> srcClass, Class<?> targetClass, Converter converter) {
            final String key = genKey(srcClass, targetClass, converter);
            return cache.computeIfAbsent(key, k -> BeanCopier.create(srcClass, targetClass, converter != null));
        }

        /**
         * 获得类与转换器生成的key
         *
         * @param srcClass    源Bean的类
         * @param targetClass 目标Bean的类
         * @param converter   转换器
         * @return 属性名和Map映射的key
         */
        private String genKey(Class<?> srcClass, Class<?> targetClass, Converter converter) {
            final StringBuilder key = new StringBuilder()
                .append(srcClass.getName()).append('#').append(targetClass.getName());
            if(null != converter){
                key.append('#').append(converter.getClass().getName());
            }
            return key.toString();
        }
    }

}
