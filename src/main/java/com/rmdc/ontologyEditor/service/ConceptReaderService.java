package com.rmdc.ontologyEditor.configuration;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.AnnotationKeeper;
import com.rmdc.ontologyEditor.model.ClassKeeper;
import com.rmdc.ontologyEditor.model.RestrictionKeeper;
import com.rmdc.ontologyEditor.service.AxiomHandler;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class ClassConfig {
    private final OWLOntology ontology;

    public ClassConfig(OWLOntology ontology) {
        this.ontology = ontology;
    }

    @Bean
    public Map<String,ClassKeeper> classKeeper(){
        Map<String,ClassKeeper> keeper = new TreeMap<>();
        addTOMap(keeper);
        return keeper;
    }

    public void addTOMap(Map<String,ClassKeeper> keeper){
        Set<OWLClass> classSet = ontology.getClassesInSignature();
        classSet.forEach(c->{
            ClassKeeper ck = new ClassKeeper();
            ck.setClassName(c.getIRI().getShortForm());
            ck.setSubClassRestrictions(getSubClassOfAxioms(c));
            ck.setEqClassRestrictions(getEquivalentClassAxioms(c));
            ck.setDisjointClasses(getDisjointAxioms(c));
            ck.setDomainOf(getDomainOf(c));
            ck.setRangeOf(getRangeOf(c));
            ck.setAnnotations(getAnnotations(c));
            keeper.put(c.getIRI().getShortForm(),ck);
        });
    }

    private List<AnnotationKeeper> getAnnotations(OWLClass owlClass){
        List<AnnotationKeeper> ann = new ArrayList<>();
        Set<OWLAnnotation> annotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(owlClass.getIRI(), ontology);
        annotations.forEach(a->ann.add(new AnnotationKeeper(a.getProperty().toString(),a.getValue().asLiteral().asSet().iterator().next().getLiteral())));

        return ann;
    }

    private List<RestrictionKeeper> getEquivalentClassAxioms(OWLClass owlClass){
        List<RestrictionKeeper> eqRestrictionList = new ArrayList<>();
        Set<OWLEquivalentClassesAxiom> sx = ontology.getEquivalentClassesAxioms(owlClass);
        for(OWLAxiom a:sx){
            eqRestrictionList.add(new RestrictionKeeper(UtilMethods.convertAxiom(a),a));
        }
        return eqRestrictionList;
    }

    private List<RestrictionKeeper> getSubClassOfAxioms(OWLClass owlClass){
        List<RestrictionKeeper> subRestrictionList = new ArrayList<>();
        Set<OWLSubClassOfAxiom> sx = ontology.getSubClassAxiomsForSubClass(owlClass);
        for(OWLAxiom a:sx){
            subRestrictionList.add(new RestrictionKeeper(UtilMethods.convertAxiom(a),a));
        }
        return subRestrictionList;
    }

    private List<String> getDomainOf(OWLClass owlClass){
        List<String> domainOf = new ArrayList<>();
        for(OWLObjectProperty p: ontology.getObjectPropertiesInSignature()){
            Set<OWLObjectPropertyDomainAxiom> da = ontology.getObjectPropertyDomainAxioms(p);
            for(OWLObjectPropertyDomainAxiom a:da){
                if(a.getClassesInSignature().iterator().next().equals(owlClass)){
                    domainOf.add(a.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
                }
            }
        }
        return domainOf;
    }

    private List<String> getRangeOf(OWLClass owlClass){
        List<String> rangeOf = new ArrayList<>();
        for(OWLObjectProperty p: ontology.getObjectPropertiesInSignature()){
            Set<OWLObjectPropertyRangeAxiom> da = ontology.getObjectPropertyRangeAxioms(p);
            for(OWLObjectPropertyRangeAxiom a:da){
                if(a.getClassesInSignature().iterator().next().equals(owlClass)){
                    rangeOf.add(a.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
                }
            }
        }
        return rangeOf;
    }

    private List<String> getDisjointAxioms(OWLClass owlClass){
        List<String> axioms = new ArrayList<>();
        Set<OWLDisjointClassesAxiom> sx = ontology.getDisjointClassesAxioms(owlClass);
        for(OWLDisjointClassesAxiom a:sx){
            Set<OWLClass> djc = a.getClassesInSignature();
            for(OWLClass c:djc){
                if(!c.equals(owlClass)){
                    axioms.add(c.getIRI().getShortForm());
                }
            }

        }
        return axioms;
    }


}
