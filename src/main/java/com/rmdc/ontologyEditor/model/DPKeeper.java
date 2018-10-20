package com.rmdc.ontologyEditor.model;

import java.util.ArrayList;
import java.util.List;

public class DPKeeper {

    private String dPName;

    private boolean functional;

    private List<String> disjointProps;
    private List<String> nonDisjointProps;
    private List<String> domains;
    private List<String> ranges;

    public DPKeeper() {
        this.disjointProps = new ArrayList<>();
        this.nonDisjointProps = new ArrayList<>();
        this.domains = new ArrayList<>();
        this.ranges = new ArrayList<>();
    }

    public String getdPName() {
        return dPName;
    }

    public void setdPName(String dPName) {
        this.dPName = dPName;
    }

    public boolean isFunctional() {
        return functional;
    }

    public void setFunctional(boolean functional) {
        this.functional = functional;
    }

    public List<String> getDisjointProps() {
        return disjointProps;
    }

    public void setDisjointProps(List<String> disjointProps) {
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

    public List<String> getNonDisjointProps() {
        return nonDisjointProps;
    }

    public void setNonDisjointProps(List<String> nonDisjointProps) {
        this.nonDisjointProps = nonDisjointProps;
    }
}
