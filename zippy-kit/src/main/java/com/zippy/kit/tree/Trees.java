package com.zippy.kit.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 树结构工具类。
 * <p>
 * 提供构建、扁平化、搜索、路径查找等通用树操作。
 *
 * <pre>
 * // 构建树
 * List&lt;TreeNode&lt;Long&gt;&gt; tree = Trees.build(nodes, 0L);
 *
 * // 扁平化
 * List&lt;TreeNode&lt;Long&gt;&gt; flat = Trees.flatten(tree);
 *
 * // 查找节点
 * TreeNode&lt;Long&gt; node = Trees.findById(tree, 42L);
 *
 * // 查找路径
 * List&lt;TreeNode&lt;Long&gt;&gt; path = Trees.findPath(tree, 42L);
 * </pre>
 */
public final class Trees {

    private Trees() {
    }

    // ==================== 构建树 ====================

    /**
     * 构建树，pid 为 null 的节点视为根节点。
     */
    public static <T> List<TreeNode<T>> build(List<TreeNode<T>> nodes) {
        T rootId = null;
        return build(nodes, rootId, null);
    }

    /**
     * 构建树，指定根节点 ID。
     *
     * @param nodes  扁平节点列表
     * @param rootId 根节点的 pid 值（pid 等于此值的节点作为根）
     */
    public static <T> List<TreeNode<T>> build(List<TreeNode<T>> nodes, T rootId) {
        return build(nodes, rootId, null);
    }

    /**
     * 构建树，指定根节点 ID 和排序比较器。
     */
    public static <T> List<TreeNode<T>> build(List<TreeNode<T>> nodes, T rootId, Comparator<TreeNode<T>> comparator) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<T, TreeNode<T>> map = new LinkedHashMap<>(nodes.size());
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
            map.put(node.getId(), node);
        }

        List<TreeNode<T>> roots = new ArrayList<>();
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            T pid = node.getPid();
            if (isRoot(pid, rootId)) {
                roots.add(node);
            } else {
                TreeNode<T> parent = map.get(pid);
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    roots.add(node);
                }
            }
        }

        if (comparator != null) {
            sortRecursively(roots, comparator);
        }
        return roots;
    }

    /**
     * 构建树，使用自定义根节点判断条件。
     *
     * @param nodes         扁平节点列表
     * @param rootPredicate 根节点判断条件（返回 true 视为根节点）
     */
    public static <T> List<TreeNode<T>> build(List<TreeNode<T>> nodes, Predicate<TreeNode<T>> rootPredicate) {
        return build(nodes, rootPredicate, null);
    }

    /**
     * 构建树，使用自定义根节点判断条件和排序比较器。
     */
    public static <T> List<TreeNode<T>> build(List<TreeNode<T>> nodes, Predicate<TreeNode<T>> rootPredicate, Comparator<TreeNode<T>> comparator) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<T, TreeNode<T>> map = new LinkedHashMap<>(nodes.size());
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
            map.put(node.getId(), node);
        }

        List<TreeNode<T>> roots = new ArrayList<>();
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            if (rootPredicate.test(node)) {
                roots.add(node);
            } else {
                TreeNode<T> parent = map.get(node.getPid());
                if (parent != null) {
                    parent.addChild(node);
                } else {
                    roots.add(node);
                }
            }
        }

        if (comparator != null) {
            sortRecursively(roots, comparator);
        }
        return roots;
    }

    // ==================== 扁平化 ====================

    /**
     * 将树结构扁平化为列表（深度优先遍历）。
     */
    public static <T> List<TreeNode<T>> flatten(List<TreeNode<T>> tree) {
        if (tree == null || tree.isEmpty()) {
            return Collections.emptyList();
        }
        List<TreeNode<T>> result = new ArrayList<>();
        flattenRecursively(tree, result);
        return result;
    }

    // ==================== 搜索 ====================

    /**
     * 在树中查找指定 ID 的节点。
     *
     * @return 匹配的节点，未找到返回 null
     */
    public static <T> TreeNode<T> findById(List<TreeNode<T>> tree, T id) {
        if (tree == null || tree.isEmpty() || id == null) {
            return null;
        }
        return findByIdRecursively(tree, id);
    }

    /**
     * 在树中查找所有满足条件的节点。
     */
    public static <T> List<TreeNode<T>> find(List<TreeNode<T>> tree, Predicate<TreeNode<T>> predicate) {
        if (tree == null || tree.isEmpty()) {
            return Collections.emptyList();
        }
        List<TreeNode<T>> result = new ArrayList<>();
        findRecursively(tree, predicate, result);
        return result;
    }

    /**
     * 查找从根到指定节点的路径。
     *
     * @param tree 树
     * @param id   目标节点 ID
     * @return 路径列表（从根到目标），未找到返回空列表
     */
    public static <T> List<TreeNode<T>> findPath(List<TreeNode<T>> tree, T id) {
        if (tree == null || tree.isEmpty() || id == null) {
            return Collections.emptyList();
        }
        List<TreeNode<T>> path = new ArrayList<>();
        if (findPathRecursively(tree, id, path)) {
            return path;
        }
        return Collections.emptyList();
    }

    // ==================== 排序 ====================

    /**
     * 按 sort 字段升序排列，sort 相同时按 label 排序。
     */
    public static <T> Comparator<TreeNode<T>> sortBySortThenLabel() {
        return Comparator.comparing(TreeNode<T>::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(TreeNode<T>::getLabel, Comparator.nullsLast(String::compareTo));
    }

    /**
     * 对树递归排序。
     */
    public static <T> void sort(List<TreeNode<T>> tree, Comparator<TreeNode<T>> comparator) {
        if (tree == null || tree.isEmpty() || comparator == null) {
            return;
        }
        sortRecursively(tree, comparator);
    }

    // ==================== 统计 ====================

    /**
     * 统计树的总节点数。
     */
    public static <T> int count(List<TreeNode<T>> tree) {
        if (tree == null || tree.isEmpty()) {
            return 0;
        }
        return flatten(tree).size();
    }

    /**
     * 获取树的最大深度。
     */
    public static <T> int maxDepth(List<TreeNode<T>> tree) {
        if (tree == null || tree.isEmpty()) {
            return 0;
        }
        return maxDepthRecursively(tree, 0);
    }

    // ==================== 内部实现 ====================

    private static <T> boolean isRoot(T pid, T rootId) {
        if (pid == null) {
            return true;
        }
        if (rootId != null) {
            return Objects.equals(pid, rootId);
        }
        return false;
    }

    private static <T> void flattenRecursively(List<TreeNode<T>> nodes, List<TreeNode<T>> result) {
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            result.add(node);
            List<TreeNode<T>> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                flattenRecursively(children, result);
            }
        }
    }

    private static <T> TreeNode<T> findByIdRecursively(List<TreeNode<T>> nodes, T id) {
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            if (Objects.equals(node.getId(), id)) {
                return node;
            }
            List<TreeNode<T>> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                TreeNode<T> found = findByIdRecursively(children, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static <T> void findRecursively(List<TreeNode<T>> nodes, Predicate<TreeNode<T>> predicate, List<TreeNode<T>> result) {
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            if (predicate.test(node)) {
                result.add(node);
            }
            List<TreeNode<T>> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                findRecursively(children, predicate, result);
            }
        }
    }

    private static <T> boolean findPathRecursively(List<TreeNode<T>> nodes, T id, List<TreeNode<T>> path) {
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            path.add(node);
            if (Objects.equals(node.getId(), id)) {
                return true;
            }
            List<TreeNode<T>> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                if (findPathRecursively(children, id, path)) {
                    return true;
                }
            }
            path.remove(path.size() - 1);
        }
        return false;
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

    private static <T> int maxDepthRecursively(List<TreeNode<T>> nodes, int currentDepth) {
        int maxChildDepth = currentDepth;
        for (TreeNode<T> node : nodes) {
            if (node == null) {
                continue;
            }
            List<TreeNode<T>> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                int childDepth = maxDepthRecursively(children, currentDepth + 1);
                maxChildDepth = Math.max(maxChildDepth, childDepth);
            }
        }
        return maxChildDepth;
    }
}
