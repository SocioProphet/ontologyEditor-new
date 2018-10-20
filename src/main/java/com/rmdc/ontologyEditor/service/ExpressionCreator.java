package com.rmdc.ontologyEditor.service;

import org.semanticweb.owlapi.annotations.OwlapiModule;
import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpressionCreator {
    private final OWLOntology ontology;

    @Autowired
    public ExpressionCreator(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OWLClassExpression createClassUnionExp(OWLClassExpression... expList){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectUnionOf(expList);
    }

    public OWLClassExpression createClassIntersectionExp(OWLClassExpression... expList){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectIntersectionOf(expList);
    }

    public OWLClassExpression createObjecctSomeValueExp(OWLObjectPropertyExpression opExp, OWLClassExpression cExp){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectSomeValuesFrom(opExp,cExp);
    }

    public OWLClassExpression createObjectAllValueExp(OWLObjectPropertyExpression opExp, OWLClassExpression cExp){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectAllValuesFrom(opExp,cExp);
    }

    public OWLClassExpression createObectHasValueExp(OWLObjectPropertyExpression opExp, OWLIndividual individual){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectHasValue(opExp,individual);
    }

    public OWLClassExpression createObjectExactCardinalityExp(int cardinality, OWLObjectPropertyExpression opExp){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectExactCardinality(cardinality, opExp);
    }

    public OWLClassExpression createObjectMaxCardinalityExp(int cardinality, OWLObjectPropertyExpression opExp){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectMaxCardinality(cardinality, opExp);
    }

    public OWLClassExpression createObjectMinCardinalityExp(int cardinality, OWLObjectPropertyExpression opExp){
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectMinCardinality(cardinality, opExp);
    }

    public OWLAxiom createSubAxiom(OWLClassExpression exp1, OWLClassExpression exp2){
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(exp1,exp2);
    }

    public OWLAxiom createEqAxiom(OWLClassExpression exp1, OWLClassExpression exp2){
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(exp1,exp2);
    }


    public OWLClassExpression createDataHasValueExp(OWLDataProperty property, OWLLiteral literal) {
        return ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLDataHasValue(property,literal);
    }
}
