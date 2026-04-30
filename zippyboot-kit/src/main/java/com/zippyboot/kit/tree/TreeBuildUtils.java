package com.zippyboot.kit.tree;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用树构建工具：将任意列表映射为 TreeNode 列表并构建树。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TreeBuildUtils {

    public static <S, K> List<TreeNode<K>> build(
            List<S> source,
            Function<S, K> idMapper,
            Function<S, K> parentIdMapper,
            Function<S, String> labelMapper) {
        return build(source, idMapper, parentIdMapper, labelMapper, null, null, null, null);
    }

    public static <S, K> List<TreeNode<K>> build(
            List<S> source,
            Function<S, K> idMapper,
            Function<S, K> parentIdMapper,
            Function<S, String> labelMapper,
            Function<S, String> codeMapper,
            Function<S, Integer> sortMapper,
            BiConsumer<S, TreeNode<K>> customizer,
            Comparator<TreeNode<K>> comparator) {

        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<TreeNode<K>> nodes = new ArrayList<>(source.size());
        for (S item : source) {
            TreeNode<K> node = new TreeNode<>();
            node.setId(idMapper.apply(item));
            node.setPid(parentIdMapper.apply(item));
            node.setLabel(labelMapper.apply(item));
            if (codeMapper != null) {
                node.setCode(codeMapper.apply(item));
            }
            if (sortMapper != null) {
                node.setSort(sortMapper.apply(item));
            }
            if (customizer != null) {
                customizer.accept(item, node);
            }
            nodes.add(node);
        }

        return TreeUtils.buildTree(nodes, null, comparator);
    }
}
