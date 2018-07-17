package com.rmdc.ontologyEditor.model;

public enum EConceptType {
    OWL_CLASS(1), OBJECT_PROPERTY(2), DATA_PROPERTY(3);

    int conceptType;


    EConceptType(int i) {
        conceptType =i;
    }

    public int getConceptType() {
        return conceptType;
    }
}
