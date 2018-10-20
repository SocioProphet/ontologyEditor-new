package com.rmdc.ontologyEditor.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lotus on 8/17/2017.
 */
public class TreeNode<T>{
    private String text;
    @JsonManagedReference
    private List<TreeNode> children = new ArrayList<>();
   // private String size="1500";
    @JsonBackReference
    private TreeNode parent = null;

    public TreeNode(String name) {
        this.text = name;
    }

    public void addChild(TreeNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(String data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
    }

    public void setChildren(List<TreeNode> children) {
        for(TreeNode t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getParent() {
        return parent;
    }

}
