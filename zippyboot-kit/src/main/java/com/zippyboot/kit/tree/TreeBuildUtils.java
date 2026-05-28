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

    public static <S, K> Builder<S, K> builder(List<S> source,
                                                Function<S, K> idMapper,
                                                Function<S, K> parentIdMapper,
                                                Function<S, String> labelMapper) {
        return new Builder<>(source, idMapper, parentIdMapper, labelMapper);
    }

    public static <S, K> List<TreeNode<K>> build(
            List<S> source,
            Function<S, K> idMapper,
            Function<S, K> parentIdMapper,
            Function<S, String> labelMapper) {
        return builder(source, idMapper, parentIdMapper, labelMapper).build();
    }

    public static final class Builder<S, K> {
        private final List<S> source;
        private final Function<S, K> idMapper;
        private final Function<S, K> parentIdMapper;
        private final Function<S, String> labelMapper;
        private Function<S, String> codeMapper;
        private Function<S, Integer> sortMapper;
        private BiConsumer<S, TreeNode<K>> customizer;
        private Comparator<TreeNode<K>> comparator;

        private Builder(List<S> source,
                        Function<S, K> idMapper,
                        Function<S, K> parentIdMapper,
                        Function<S, String> labelMapper) {
            this.source = source;
            this.idMapper = idMapper;
            this.parentIdMapper = parentIdMapper;
            this.labelMapper = labelMapper;
        }

        public Builder<S, K> codeMapper(Function<S, String> codeMapper) {
            this.codeMapper = codeMapper;
            return this;
        }

        public Builder<S, K> sortMapper(Function<S, Integer> sortMapper) {
            this.sortMapper = sortMapper;
            return this;
        }

        public Builder<S, K> customizer(BiConsumer<S, TreeNode<K>> customizer) {
            this.customizer = customizer;
            return this;
        }

        public Builder<S, K> comparator(Comparator<TreeNode<K>> comparator) {
            this.comparator = comparator;
            return this;
        }

        public List<TreeNode<K>> build() {
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
}
