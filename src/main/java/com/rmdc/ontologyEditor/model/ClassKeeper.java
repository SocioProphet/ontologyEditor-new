package com.rmdc.ontologyEditor.model;

import java.util.ArrayList;
import java.util.List;

public class ClassKeeper {

    private String className;
    private List<RestrictionKeeper> subClassRestrictions;
    private List<RestrictionKeeper> eqClassRestrictions;
    private List<String> disjointClasses;
    private List<String> domainOf;
    private List<String> rangeOf;
    private List<AnnotationKeeper> annotations;

    public ClassKeeper() {
        this.subClassRestrictions=new ArrayList<>();
        this.eqClassRestrictions=new ArrayList<>();
        this.disjointClasses=new ArrayList<>();
        this.domainOf=new ArrayList<>();
        this.rangeOf=new ArrayList<>();
        this.annotations=new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<RestrictionKeeper> getSubClassRestrictions() {
        return subClassRestrictions;
    }

    public void setSubClassRestrictions(List<RestrictionKeeper> subClassRestrictions) {
        this.subClassRestrictions = subClassRestrictions;
    }

    public List<RestrictionKeeper> getEqClassRestrictions() {
        return eqClassRestrictions;
    }

    public void setEqClassRestrictions(List<RestrictionKeeper> eqClassRestrictions) {
        this.eqClassRestrictions = eqClassRestrictions;
    }

    public List<String> getDisjointClasses() {
        return disjointClasses;
    }

    public void setDisjointClasses(List<String> disjointClasses) {
        this.disjointClasses = disjointClasses;
    }

    public List<String> getDomainOf() {
        return domainOf;
    }

    public void setDomainOf(List<String> domainOf) {
        this.domainOf = domainOf;
    }

    public List<String> getRangeOf() {
        return rangeOf;
    }

    public void setRangeOf(List<String> rangeOf) {
        this.rangeOf = rangeOf;
    }

    public List<AnnotationKeeper> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationKeeper> annotations) {
        this.annotations = annotations;
    }

}
