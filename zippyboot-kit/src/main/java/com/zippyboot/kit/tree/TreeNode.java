package com.zippyboot.kit.tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public TreeNode() {
    }

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

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public T getPid() {
        return pid;
    }

    public void setPid(T pid) {
        this.pid = pid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }
}
