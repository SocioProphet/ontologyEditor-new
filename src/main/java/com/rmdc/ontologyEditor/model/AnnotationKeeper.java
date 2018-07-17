package com.rmdc.ontologyEditor.model;

public class AnnotationKeeper {
    private String property;
    private String value;

    public AnnotationKeeper() {
    }

    public AnnotationKeeper(String property, String values) {
        this.property = property;
        this.value = values;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
