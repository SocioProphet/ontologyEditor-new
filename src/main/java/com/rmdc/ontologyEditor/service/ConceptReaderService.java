package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConceptReaderService {
    private final OWLOntology ontology;

    private Map<String,ClassKeeper> classKeeper;
    private Map<String,OPKeeper> opKeeper;
    private Map<String,DPKeeper> dpKeeper;

    public ConceptReaderService(OWLOntology ontology) {

        this.ontology = ontology;
        readComponentData();
    }

    private void readClassData(){
        classKeeper = new HashMap<>();
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
            classKeeper.put(c.getIRI().getShortForm(),ck);
        });
    }

    private void readOpData(){
        opKeeper = new HashMap<>();
        Set<OWLObjectProperty> opSet = ontology.getObjectPropertiesInSignature();
        opSet.forEach(o->{
            OPKeeper opk = new OPKeeper();
            opk.setoPName(o.getIRI().getShortForm());
            opk.setInverseProps(getInverseOProperties(o));
            opk.setDisjoitProps(getDisjointOProperties(o));
            opk.setDomains(getOPDomains(o));
            opk.setRanges(getOPRanges(o));
            opk.setAnnotations(getAnnotations(o));

            opk.setFunctional(isOPFunctional(o));
            opk.setInverseFunctional(isOPInverseFunctional(o));
            opk.setTransitive(isTransitive(o));
            opk.setSymmetric(isOPSymmetric(o));
            opk.setAsymmetric(isOPAsymmetric(o));
            opk.setReflexive(isOPReflexive(o));
            opk.setIrreflexive(isOPIrreflexive(o));
            opk.setNonDisjointProps(getNonDisjointOProperties(o));
            opKeeper.put(o.getIRI().getShortForm(),opk);
        });
    }

    private void readDpData(){
        dpKeeper= new HashMap<>();
        Set<OWLDataProperty> dps = ontology.getDataPropertiesInSignature();
        dps.forEach(d->{
            DPKeeper dpk = new DPKeeper();
            dpk.setdPName(d.getIRI().getShortForm());
            dpk.setFunctional(isDPFunctional(d));
            dpk.setDisjointProps(getDisjointDProperties(d));
            dpk.setDomains(getDPDomains(d));
            dpk.setRanges(getDPRanges(d));
            dpk.setNonDisjointProps(getNonDisjointDProperties(d));
            dpKeeper.put(d.getIRI().getShortForm(),dpk);
        });
    }

    public void readComponentData(){
        readClassData();
        readOpData();
        readDpData();
    }

    public Map<String, ClassKeeper> getClassKeeper() {
        return classKeeper;
    }

    public Map<String, OPKeeper> getOpKeeper() {
        return opKeeper;
    }

    public Map<String, DPKeeper> getDpKeeper() {
        return dpKeeper;
    }

    private List<AnnotationKeeper> getAnnotations(OWLEntity owlEntity){
        List<AnnotationKeeper> ann = new ArrayList<>();
        Set<OWLAnnotation> annotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(owlEntity.getIRI(), ontology);
        annotations.forEach(a->ann.add(new AnnotationKeeper(a.getProperty().toString(),a.getValue().asLiteral().asSet().iterator().next().getLiteral())));
        return ann;
    }

    private List<RestrictionKeeper> getEquivalentClassAxioms(OWLClass owlClass){
        List<RestrictionKeeper> eqRestrictionList = new ArrayList<>();
        Set<OWLEquivalentClassesAxiom> sx = ontology.getEquivalentClassesAxioms(owlClass);
        for(OWLAxiom a:sx){
            eqRestrictionList.add(new RestrictionKeeper(UtilMethods.toManchesterFormat(a),a));
        }
        return eqRestrictionList;
    }

    private List<RestrictionKeeper> getSubClassOfAxioms(OWLClass owlClass){
        List<RestrictionKeeper> subRestrictionList = new ArrayList<>();
        Set<OWLSubClassOfAxiom> sx = ontology.getSubClassAxiomsForSubClass(owlClass);
        for(OWLAxiom a:sx){
            subRestrictionList.add(new RestrictionKeeper(UtilMethods.toManchesterFormat(a),a));
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

    //OP

    public List<String> getInverseOProperties(OWLObjectProperty property){
        List<String> properties = new ArrayList<>();
        Set<OWLInverseObjectPropertiesAxiom> ops = ontology.getInverseObjectPropertyAxioms(property);
        ops.forEach(a->{
            if (a.getFirstProperty().asOWLObjectProperty() == property) {
                properties.add(a.getSecondProperty().asOWLObjectProperty().getIRI().getShortForm());
            } else {
                properties.add(a.getFirstProperty().asOWLObjectProperty().getIRI().getShortForm());
            }
        });

        return properties;
    }

    public List<String> getDisjointOProperties(OWLObjectProperty property){
        List<String> disjoints = new ArrayList<>();
        Set<OWLDisjointObjectPropertiesAxiom> disOP = ontology.getDisjointObjectPropertiesAxioms(property);
        for(OWLDisjointObjectPropertiesAxiom p: disOP){
            disjoints.add(p.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
        }
        Collections.sort(disjoints);
        return disjoints;
    }

    public List<String> getOPDomains(OWLObjectProperty property){
        Set<OWLObjectPropertyDomainAxiom> domainAxioms = ontology.getObjectPropertyDomainAxioms(property);

        List<String> domains = new ArrayList<>();
        for(OWLObjectPropertyDomainAxiom a: domainAxioms){
            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return domains;
    }

    public List<String> getOPRanges(OWLObjectProperty property){
        Set<OWLObjectPropertyRangeAxiom> domainAxioms = ontology.getObjectPropertyRangeAxioms(property);

        List<String> ranges = new ArrayList<>();
        for(OWLObjectPropertyRangeAxiom a: domainAxioms){
            ranges.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return ranges;
    }

    public boolean isOPFunctional(OWLObjectProperty property){
        return !ontology.getFunctionalObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isOPInverseFunctional(OWLObjectProperty property){
        return !ontology.getInverseFunctionalObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isOPSymmetric(OWLObjectProperty property){
        return !ontology.getSymmetricObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isOPAsymmetric(OWLObjectProperty property){
        return !ontology.getAsymmetricObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isTransitive(OWLObjectProperty property){
        return !ontology.getTransitiveObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isOPReflexive(OWLObjectProperty property){
        return !ontology.getReflexiveObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isOPIrreflexive(OWLObjectProperty property){
        return !ontology.getIrreflexiveObjectPropertyAxioms(property).isEmpty();
    }


    //DP

    public boolean isDPFunctional(OWLDataProperty property){
        return !ontology.getFunctionalDataPropertyAxioms(property).isEmpty();

    }

    public List<String> getDisjointDProperties(OWLDataProperty property){
        List<String> disjoints = new ArrayList<>();
        Set<OWLDisjointDataPropertiesAxiom> disOP = ontology.getDisjointDataPropertiesAxioms(property);
        for(OWLDisjointDataPropertiesAxiom p: disOP){
            for(OWLDataProperty pr:p.getDataPropertiesInSignature()){
                if(!pr.equals(property)){
                    disjoints.add(pr.getIRI().getShortForm());
                }
            }
        }
        Collections.sort(disjoints);
        //disjoints.remove(property);
        return disjoints;
    }

    public List<String> getDPRanges(OWLDataProperty property){
        Set<OWLDataPropertyRangeAxiom> domainAxioms = ontology.getDataPropertyRangeAxioms(property);

        List<String> ranges = new ArrayList<>();
        for(OWLDataPropertyRangeAxiom a: domainAxioms){
            ranges.add(a.getDatatypesInSignature().iterator().next().getIRI().getShortForm());
        }
        return ranges;
    }

    public List<String> getDPDomains( OWLDataProperty property){
        Set<OWLDataPropertyDomainAxiom> domainAxioms = ontology.getDataPropertyDomainAxioms(property);

        List<String> domains = new ArrayList<>();
        for(OWLDataPropertyDomainAxiom a: domainAxioms){
            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return domains;
    }

    public List<String> getOProperties(){
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();
        List<String> propertyStrings = new ArrayList<>();
        properties.forEach(p->propertyStrings.add(p.getIRI().getShortForm()));
        return propertyStrings;
    }

    public List<String> getDProperties(){
        Set<OWLDataProperty> properties = ontology.getDataPropertiesInSignature();
        List<String> propertyStrings = new ArrayList<>();
        properties.forEach(p->propertyStrings.add(p.getIRI().getShortForm()));
        return propertyStrings;
    }

    private List<String> getNonDisjointOProperties(OWLObjectProperty property){
        List<String> all = getOProperties();
        all.removeAll(getDisjointOProperties(property));
        return all;
    }

    private List<String> getNonDisjointDProperties(OWLDataProperty property){
        List<String> all = getDProperties();
        all.removeAll(getDisjointDProperties(property));
        return all;
    }



}
