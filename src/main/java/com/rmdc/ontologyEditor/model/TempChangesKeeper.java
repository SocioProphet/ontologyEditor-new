package com.rmdc.ontologyEditor.model;

import org.semanticweb.owlapi.model.*;

import java.util.Date;
import java.util.List;

public class TempChangesKeeper {
    private List<OWLAxiom> axioms;
    private List<OWLNamedIndividual> individuals;
    private List<OWLAnnotation> annotations;
    private Date timeStamp;
    private EChangeType eChangeType;
    private String conceptName;
    private EConceptType eConceptType;
    private String description;

    public TempChangesKeeper(
            List<OWLAxiom> axioms, List<OWLNamedIndividual> individuals,
            List<OWLAnnotation> annotations, Date timeStamp,
            EChangeType eChangeType, String conceptName,
            EConceptType eConceptType, String description) {
        this.axioms = axioms;
        this.individuals = individuals;
        this.annotations = annotations;
        this.timeStamp = timeStamp;
        this.eChangeType = eChangeType;
        this.conceptName = conceptName;
        this.eConceptType = eConceptType;
        this.description = description;
    }

    public List<OWLAxiom> getAxioms() {
        return axioms;
    }

    public void setAxioms(List<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    public List<OWLNamedIndividual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<OWLNamedIndividual> individuals) {
        this.individuals = individuals;
    }

    public List<OWLAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<OWLAnnotation> annotations) {
        this.annotations = annotations;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public EChangeType geteChangeType() {
        return eChangeType;
    }

    public void seteChangeType(EChangeType eChangeType) {
        this.eChangeType = eChangeType;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public EConceptType geteConceptType() {
        return eConceptType;
    }

    public void seteConceptType(EConceptType eConceptType) {
        this.eConceptType = eConceptType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
