package com.rmdc.ontologyEditor.model;

import java.util.List;

/**
 * Created by Lotus on 9/19/2017.
 */
public class DataTransfer {
    private String cConcept;
    private String patternType;
    private List<String> classList;
    private List<String> oProperties;
    private List<String> dProperties;
    private List<String> dTypes;
    private List<String> individuals;
    private List<String> literals;
    private String aProperty;
    private String aValue;
    private int cardinality;
    private String cardinalityType; // min max exact
    private String description;

    public String getcConcept() {
        return cConcept;
    }

    public void setcConcept(String cConcept) {
        this.cConcept = cConcept;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public List<String> getClassList() {
        return classList;
    }

    public void setClassList(List<String> classList) {
        this.classList = classList;
    }

    public List<String> getoProperties() {
        return oProperties;
    }

    public void setoProperties(List<String> oProperties) {
        this.oProperties = oProperties;
    }

    public List<String> getdProperties() {
        return dProperties;
    }

    public void setdProperties(List<String> dProperties) {
        this.dProperties = dProperties;
    }

    public List<String> getdTypes() {
        return dTypes;
    }

    public void setdTypes(List<String> dTypes) {
        this.dTypes = dTypes;
    }

    public List<String> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<String> individuals) {
        this.individuals = individuals;
    }

    public List<String> getLiterals() {
        return literals;
    }

    public void setLiterals(List<String> literals) {
        this.literals = literals;
    }

    public String getaProperty() {
        return aProperty;
    }

    public void setaProperty(String aProperty) {
        this.aProperty = aProperty;
    }

    public String getaValue() {
        return aValue;
    }

    public void setaValue(String aValue) {
        this.aValue = aValue;
    }

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    public String getCardinalityType() {
        return cardinalityType;
    }

    public void setCardinalityType(String cardinalityType) {
        this.cardinalityType = cardinalityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
