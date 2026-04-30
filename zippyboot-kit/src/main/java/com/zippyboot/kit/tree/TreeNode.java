package com.zippyboot.kit.tree;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class TreeNode<T> {

    /** 当前节点 ID。 */
    private T id;

    /** 父节点 ID。 */
    private T pid;

    /** 节点编码（业务可选字段）。 */
    private String code;

    /** 节点名称（通用展示字段）。 */
    private String label;

    /** 节点排序值（值越小越靠前）。 */
    private Integer sort;

    /** 节点扩展属性（业务字段透传）。 */
    private Map<String, Object> ext = new LinkedHashMap<>();

    /** 子节点。 */
    private List<TreeNode<T>> children = new ArrayList<>();

    public TreeNode(T id, T pid, String label) {
        this.id = id;
        this.pid = pid;
        this.label = label;
    }

    public void addChild(TreeNode<T> child) {
        if (child != null) {
            children.add(child);
        }
    }

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }
}
