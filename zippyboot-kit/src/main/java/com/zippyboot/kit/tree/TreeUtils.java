package com.zippyboot.kit.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TreeUtils {

    private static final String DEFAULT_ROOT_TEXT = "0";

    private TreeUtils() {
    }

    public static <T> List<TreeNode<T>> buildTree(List<TreeNode<T>> nodes) {
        return buildTree(nodes, null, null);
    }

    public static <T> List<TreeNode<T>> buildTree(List<TreeNode<T>> nodes, T rootId) {
        return buildTree(nodes, rootId, null);
    }

    public static <T> List<TreeNode<T>> buildTree(List<TreeNode<T>> nodes, T rootId, Comparator<TreeNode<T>> comparator) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<T, TreeNode<T>> map = new LinkedHashMap<>(nodes.size());
        for (TreeNode<T> current : nodes) {
            if (current == null) {
                continue;
            }
            current.setChildren(new ArrayList<>());
            map.put(current.getId(), current);
        }

        List<TreeNode<T>> roots = new ArrayList<>();
        for (TreeNode<T> current : nodes) {
            if (current == null) {
                continue;
            }
            T parentId = current.getPid();
            if (isRoot(parentId, rootId)) {
                roots.add(current);
                continue;
            }

            TreeNode<T> parent = map.get(parentId);
            if (parent == null) {
                roots.add(current);
                continue;
            }
            parent.addChild(current);
        }

        if (comparator != null) {
            sortRecursively(roots, comparator);
        }
        return roots;
    }

    public static <T> Comparator<TreeNode<T>> sortBySortThenLabel() {
        return Comparator.comparing(TreeNode<T>::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(TreeNode<T>::getLabel, Comparator.nullsLast(String::compareTo));
    }

    private static <T> boolean isRoot(T parentId, T rootId) {
        if (parentId == null) {
            return true;
        }
        if (rootId != null) {
            return Objects.equals(parentId, rootId);
        }
        return DEFAULT_ROOT_TEXT.equals(String.valueOf(parentId));
    }

    private static <T> void sortRecursively(List<TreeNode<T>> nodes, Comparator<TreeNode<T>> comparator) {
        nodes.sort(comparator);
        for (TreeNode<T> node : nodes) {
            List<TreeNode<T>> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                sortRecursively(children, comparator);
            }
        }
    }
}
