package com.zippyboot.kit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;

import java.util.Collections;
import java.util.LinkedHashMap;
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

	private static final Map<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();

	/**
	 * 对象映射(同名属性拷贝)。
	 */
	public static <T> T map(Object source, Class<T> actualEditable) {
		return copy(source, actualEditable);
	}

	/**
	 * 新建目标对象并执行属性拷贝。
	 */
	public static <T> T copy(Object source, Class<T> targetClass) {
		if (source == null) {
			return null;
		}
		T target = newInstance(targetClass);
		copyProperties(source, target);
		return target;
	}

	/**
	 * 拷贝到已存在的目标对象。
	 */
	public static <T, V> V copy(T source, V target) {
		if (source == null || target == null) {
			return target;
		}
		copyProperties(source, target);
		return target;
	}

	/**
	 * 新建目标对象并忽略源对象中的 null 属性。
	 */
	public static <T> T copyIgnoreNull(Object source, Class<T> targetClass) {
		if (source == null) {
			return null;
		}
		T target = newInstance(targetClass);
		copyPropertiesIgnoreNull(source, target);
		return target;
	}

	/**
	 * 普通属性拷贝(不忽略 null)。
	 */
	public static void copyProperties(Object source, Object target) {
		if (source == null || target == null) {
			return;
		}
		BeanCopier copier = getBeanCopier(source.getClass(), target.getClass());
		copier.copy(source, target, null);
	}

	/**
	 * 普通属性拷贝，可忽略指定字段。
	 */
	public static void copyProperties(Object source, Object target, String... ignoreProperties) {
		if (source == null || target == null) {
			return;
		}
		Set<String> ignoreSet = toIgnoreSet(ignoreProperties);
		BeanMap sourceMap = BeanMap.create(source);
		BeanMap targetMap = BeanMap.create(target);
		for (Object keyObj : sourceMap.keySet()) {
			String key = String.valueOf(keyObj);
			if ("class".equals(key) || ignoreSet.contains(key) || !targetMap.containsKey(key)) {
				continue;
			}
			try {
				targetMap.put(key, sourceMap.get(key));
			} catch (RuntimeException ignored) {
				// 类型不匹配时忽略，保持与多数Bean拷贝工具一致
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
		if (source == null || target == null) {
			return;
		}
		Set<String> ignoreSet = toIgnoreSet(ignoreProperties);
		BeanMap sourceMap = BeanMap.create(source);
		BeanMap targetMap = BeanMap.create(target);
		for (Object keyObj : sourceMap.keySet()) {
			String key = String.valueOf(keyObj);
			if ("class".equals(key) || ignoreSet.contains(key) || !targetMap.containsKey(key)) {
				continue;
			}
			Object value = sourceMap.get(key);
			if (value == null) {
				continue;
			}
			try {
				targetMap.put(key, value);
			} catch (RuntimeException ignored) {
				// 类型不匹配时忽略
			}
		}
	}

	/**
	 * 列表拷贝到新列表。
	 */
	public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
		if (sourceList == null || sourceList.isEmpty()) {
			return Collections.emptyList();
		}
		return sourceList.stream()
				.map(source -> copy(source, targetClass))
				.collect(Collectors.toList());
	}

	/**
	 * 列表拷贝到新列表，忽略 null 属性。
	 */
	public static <T> List<T> copyListIgnoreNull(List<?> sourceList, Class<T> targetClass) {
		if (sourceList == null || sourceList.isEmpty()) {
			return Collections.emptyList();
		}
		return sourceList.stream()
				.map(source -> copyIgnoreNull(source, targetClass))
				.collect(Collectors.toList());
	}

	/**
	 * 将源列表按索引拷贝到已存在的目标列表。
	 * 实际拷贝数量为两者长度的最小值，遇到空元素会跳过。
	 */
	public static <S, T> List<T> copyList(List<S> sourceList, List<T> targetList) {
		if (sourceList == null || sourceList.isEmpty() || targetList == null || targetList.isEmpty()) {
			return targetList == null ? Collections.emptyList() : targetList;
		}
		int size = Math.min(sourceList.size(), targetList.size());
		for (int i = 0; i < size; i++) {
			S source = sourceList.get(i);
			T target = targetList.get(i);
			if (source == null || target == null) {
				continue;
			}
			copyProperties(source, target);
		}
		return targetList;
	}

	/**
	 * Bean 转 Map(包含 null 值字段)。
	 */
	public static Map<String, Object> beanToMap(Object bean) {
		if (bean == null) {
			return Collections.emptyMap();
		}
		BeanMap beanMap = BeanMap.create(bean);
		Map<String, Object> result = new LinkedHashMap<>();
		for (Object keyObj : beanMap.keySet()) {
			String key = String.valueOf(keyObj);
			if ("class".equals(key)) {
				continue;
			}
			result.put(key, beanMap.get(key));
		}
		return result;
	}

	/**
	 * Bean 转 Map 别名方法。
	 */
	public static Map<String, Object> copyToMap(Object bean) {
		return beanToMap(bean);
	}

	/**
	 * Bean 转 Map，过滤 null 值字段。
	 */
	public static Map<String, Object> beanToMapIgnoreNull(Object bean) {
		if (bean == null) {
			return Collections.emptyMap();
		}
		BeanMap beanMap = BeanMap.create(bean);
		Map<String, Object> result = new LinkedHashMap<>();
		for (Object keyObj : beanMap.keySet()) {
			String key = String.valueOf(keyObj);
			if ("class".equals(key)) {
				continue;
			}
			Object value = beanMap.get(key);
			if (value != null) {
				result.put(key, value);
			}
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
		if (map == null || map.isEmpty() || target == null) {
			return;
		}
		BeanWrapper targetWrapper = new BeanWrapperImpl(target);
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			String propertyName = entry.getKey();
			if (!targetWrapper.isWritableProperty(propertyName)) {
				continue;
			}
			try {
				targetWrapper.setPropertyValue(propertyName, entry.getValue());
			} catch (RuntimeException ignored) {
				// 转换失败的字段忽略
			}
		}
	}

	/**
	 * 创建一个新的 Map 副本。
	 */
	public static Map<String, Object> copyToNewMap(Map<String, ?> source) {
		if (source == null || source.isEmpty()) {
			return Collections.emptyMap();
		}
		return new LinkedHashMap<>(source);
	}

	/**
	 * Map value 按指定类型进行对象拷贝转换。
	 */
	public static <T, V> Map<String, V> mapToMap(Map<String, T> map, Class<V> clazz) {
		if (map == null || map.isEmpty() || clazz == null) {
			return Collections.emptyMap();
		}
		Map<String, V> copyMap = new LinkedHashMap<>(map.size());
		map.forEach((key, value) -> copyMap.put(key, copy(value, clazz)));
		return copyMap;
	}

	/**
	 * 获取对象中值为 null 的属性名集合。
	 */
	public static Set<String> getNullPropertyNames(Object source) {
		if (source == null) {
			return Collections.emptySet();
		}
		BeanWrapper src = new BeanWrapperImpl(source);
		Set<String> emptyNames = new HashSet<>();
		for (java.beans.PropertyDescriptor propertyDescriptor : src.getPropertyDescriptors()) {
			String propertyName = propertyDescriptor.getName();
			if ("class".equals(propertyName)) {
				continue;
			}
			Object srcValue = src.getPropertyValue(propertyName);
			if (srcValue == null) {
				emptyNames.add(propertyName);
			}
		}
		return emptyNames;
	}

	private static BeanCopier getBeanCopier(Class<?> sourceClass, Class<?> targetClass) {
		String cacheKey = sourceClass.getName() + "->" + targetClass.getName();
		return BEAN_COPIER_CACHE.computeIfAbsent(cacheKey,
				key -> BeanCopier.create(sourceClass, targetClass, false));
	}

	private static Set<String> toIgnoreSet(String... ignoreProperties) {
		if (ignoreProperties == null || ignoreProperties.length == 0) {
			return Collections.emptySet();
		}
		Set<String> set = new HashSet<>();
		Collections.addAll(set, ignoreProperties);
		return set;
	}

	private static <T> T newInstance(Class<T> targetClass) {
		if (targetClass == null) {
			throw new IllegalArgumentException("targetClass cannot be null");
		}
		try {
			return targetClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot instantiate target class: " + targetClass.getName(), e);
		}
	}

}
