package com.rmdc.ontologyEditor.service;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AxiomHandler {

    private final OWLOntology ontology;
    private final OWLReasoner pelletReasoner;
    private final MessageSource messageSource;
    private List<OWLAxiom> axiomsQueue;

    public AxiomHandler(OWLOntology ontology, OWLReasoner pelletReasoner, MessageSource messageSource) {
        this.ontology = ontology;
        this.pelletReasoner = pelletReasoner;
        this.messageSource = messageSource;
        this.axiomsQueue= new ArrayList<>();
    }


    public boolean checkConsistency() throws Exception {
        ontology.getOWLOntologyManager().addOntologyChangeListener((OWLOntologyChangeListener) pelletReasoner);

        // perform initial consistency check
        long s = System.currentTimeMillis();
        boolean consistent = pelletReasoner.isConsistent();
        long e = System.currentTimeMillis();
        System.out.println( "Consistent? " + consistent + " (" + (e - s) + "ms)" );

        return consistent;
    }

    public OWLEntity getOWLEntity(String name,EntityType entityType){
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEntity(entityType, IRI.create(messageSource.getMessage("baseIRI",null, Locale.getDefault()), name));
    }

    public IRI getIRI(String name){
        return IRI.create(messageSource.getMessage("baseIRI",null, Locale.getDefault()), name);
    }
    public boolean removeAxiom(OWLAxiom axiom) throws Exception {

        RemoveAxiom removeAxiom = new RemoveAxiom(ontology, axiom);
        ontology.getOWLOntologyManager().applyChange(removeAxiom);
        boolean consistent = checkConsistency();
        if (!consistent) {
            AddAxiom addAxiom = new AddAxiom(ontology, axiom);
            ontology.getOWLOntologyManager().applyChange(addAxiom);
        } else {
            axiomsQueue.add(axiom);//todo: this should be changed for use in undo operation, add opposite axiom type to queue
        }
        ontology.getOWLOntologyManager().saveOntology(ontology);
        return consistent;
    }

    public boolean addAxiom(OWLAxiom axiom) throws Exception {
        AddAxiom addAxiom = new AddAxiom(ontology, axiom);
        ontology.getOWLOntologyManager().applyChange(addAxiom);
        long startTime = System.currentTimeMillis();
        boolean consistent = checkConsistency();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
        if (!consistent) {
            RemoveAxiom removeAxiom = new RemoveAxiom(ontology, axiom);
            ontology.getOWLOntologyManager().applyChange(removeAxiom);
        }else{
            axiomsQueue = new ArrayList<>();
            axiomsQueue.add(axiom);

         }
        ontology.getOWLOntologyManager().saveOntology(ontology);
        return consistent;
    }

    public void addToAxiomQueue(Set<OWLAxiom> axioms){
        axiomsQueue.addAll(axioms);
    }

    public void removeFromAxiomQueue(Set<OWLAxiom> axioms){
        axiomsQueue.removeAll(axioms);
    }

    public List<OWLAxiom> getAxiomsQueue() {
        return axiomsQueue;
    }
}
