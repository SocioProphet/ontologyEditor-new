package com.rmdc.ontologyEditor.model;

import java.util.ArrayList;
import java.util.List;

public class OPKeeper {

    private String oPName;

    private List<String> inverseProps;
    private List<String> disjointProps;
    private List<String> domains;
    private List<String> ranges;
    private List<String> nonDisjointProps;

    private List<AnnotationKeeper> annotations;

    private boolean functional;
    private boolean inverseFunctional;
    private boolean transitive;
    private boolean symmetric;
    private boolean asymmetric;
    private boolean reflexive;
    private boolean irreflexive;

    public OPKeeper() {
        this.inverseProps = new ArrayList<>();
        this.disjointProps = new ArrayList<>();
        this.domains = new ArrayList<>();
        this.ranges = new ArrayList<>();
        this.nonDisjointProps = new ArrayList<>();
    }

    public String getoPName() {
        return oPName;
    }

    public void setoPName(String oPName) {
        this.oPName = oPName;
    }

    public List<String> getInverseProps() {
        return inverseProps;
    }

    public void setInverseProps(List<String> inverseProps) {
        this.inverseProps = inverseProps;
    }

    public List<String> getDisjoitProps() {
        return disjointProps;
    }

    public void setDisjoitProps(List<String> disjointProps) {
        this.disjointProps = disjointProps;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<String> getRanges() {
        return ranges;
    }

    public void setRanges(List<String> ranges) {
        this.ranges = ranges;
    }

    public List<AnnotationKeeper> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationKeeper> annotations) {
        this.annotations = annotations;
    }

    public boolean isFunctional() {
        return functional;
    }

    public void setFunctional(boolean functional) {
        this.functional = functional;
    }

    public boolean isInverseFunctional() {
        return inverseFunctional;
    }

    public void setInverseFunctional(boolean inverseFunctional) {
        this.inverseFunctional = inverseFunctional;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    public void setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
    }

    public boolean isAsymmetric() {
        return asymmetric;
    }

    public void setAsymmetric(boolean asymmetric) {
        this.asymmetric = asymmetric;
    }

    public boolean isReflexive() {
        return reflexive;
    }

    public void setReflexive(boolean reflexive) {
        this.reflexive = reflexive;
    }

    public boolean isIrreflexive() {
        return irreflexive;
    }

    public void setIrreflexive(boolean irreflexive) {
        this.irreflexive = irreflexive;
    }

    public List<String> getDisjointProps() {
        return disjointProps;
    }

    public void setDisjointProps(List<String> disjointProps) {
        this.disjointProps = disjointProps;
    }

    public List<String> getNonDisjointProps() {
        return nonDisjointProps;
    }

    public void setNonDisjointProps(List<String> nonDisjointProps) {
        this.nonDisjointProps = nonDisjointProps;
    }
}
