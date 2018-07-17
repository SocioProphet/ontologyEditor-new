package com.rmdc.ontologyEditor.model;

import org.semanticweb.owlapi.model.OWLAxiom;

public class RestrictionKeeper {
    private String axiom;
    private OWLAxiom axiomObj;

    public RestrictionKeeper(String axiom, OWLAxiom axiomObj) {
        this.axiom = axiom;
        this.axiomObj = axiomObj;
    }

    public String getAxiom() {
        return axiom;
    }

    public void setAxiom(String axiom) {
        this.axiom = axiom;
    }

    public OWLAxiom getAxiomObj() {
        return axiomObj;
    }

    public void setAxiomObj(OWLAxiom axiomObj) {
        this.axiomObj = axiomObj;
    }
}
