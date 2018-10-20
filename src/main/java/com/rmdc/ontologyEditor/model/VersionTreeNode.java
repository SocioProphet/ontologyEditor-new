package com.rmdc.ontologyEditor.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

public class VersionTreeNode<T> {

    private String id;
    private String text;
    @JsonManagedReference
    private List<VersionTreeNode> children = new ArrayList<>();

    @JsonManagedReference
    private List<String> changes=  new ArrayList<>();
    // private String size="1500";
    @JsonBackReference
    private VersionTreeNode parent = null;

    public VersionTreeNode(String name) {
        this.text = name;
    }

    public VersionTreeNode(String name, String id, List<String> changes) {
        this.text = name;
        this.id =id;
        this.changes = changes;
    }

    public void addChild(VersionTreeNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(String data) {
        VersionTreeNode<T> newChild = new VersionTreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
    }

    public void addChild(String data, String id, List<String> changes) {
        VersionTreeNode<T> newChild = new VersionTreeNode<>(data);
        newChild.setId(id);
        newChild.setParent(this);
        children.add(newChild);
        newChild.changes=changes;
    }

    public void setChildren(List<VersionTreeNode> children) {
        for(VersionTreeNode t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<VersionTreeNode> getChildren() {
        return children;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private void setParent(VersionTreeNode parent) {
        this.parent = parent;
    }

    public VersionTreeNode getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

}
