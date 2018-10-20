package com.rmdc.ontologyEditor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.EChangeType;
import com.rmdc.ontologyEditor.model.EConceptType;
import com.rmdc.ontologyEditor.model.OPKeeper;
import com.rmdc.ontologyEditor.model.TreeNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Lotus on 8/20/2017.
 */

@Service
public class ObjectPropertyService {

    private TreeNode objectPropertyTree;
    private final OWLReasoner structuralReasoner;
    private final OWLReasoner pelletReasoner;
    private final OWLOntology ontology;
    private final AxiomHandler axiomHandler;
    private final ConceptReaderService readerService;
    private String treeString=null;
    private Map<String,OPKeeper> opKeeper;

    public ObjectPropertyService(OWLReasoner structuralReasoner, OWLReasoner pelletReasoner,
                                 OWLOntology ontology, AxiomHandler axiomHandler, ConceptReaderService readerService) {
        this.structuralReasoner = structuralReasoner;
        this.pelletReasoner = pelletReasoner;
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;
        this.readerService = readerService;
        this.opKeeper = readerService.getOpKeeper();
        printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopObjectProperty());
    }
    public OPKeeper getKeeper(String key){
        return opKeeper.get(key);
    }
    private void printHierarchy(OWLObjectProperty property) {

        if(objectPropertyTree == null){
            objectPropertyTree = new TreeNode(property.getIRI().getShortForm());
        }

        for (OWLObjectPropertyExpression child : structuralReasoner.getSubObjectProperties(property, true).getFlattened()) {
            if(!child.isAnonymous()){
                if(!child.asOWLObjectProperty().equals(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLBottomObjectProperty())) {
                    UtilMethods.searchTree(property.asOWLObjectProperty().getIRI().getShortForm(), objectPropertyTree).addChild(child.asOWLObjectProperty().getIRI().getShortForm());
                }
            }
            if (!child.equals(property) &&!child.isAnonymous()) {
                printHierarchy(child.asOWLObjectProperty());
            }
        }
        
    }

    public List<String> getObjectProperties(){
        return readerService.getOProperties();
    }

    public String getObjectPropertyTree(boolean build){
        if(build){
            objectPropertyTree=null;
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopObjectProperty());
        }
        treeString = UtilMethods.buildTree((treeString==null|| build), objectPropertyTree);
        return treeString;
    }

    public boolean addOProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeclarationAxiom(property);
        try {
            if(axiomHandler.addAxiom(declare)){

                refreshOPropertyData();

                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{add(declare);}},null,null,
                        EChangeType.DELETE,p, EConceptType.OBJECT_PROPERTY, description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void refreshOPropertyData() {
        readerService.readComponentData();
        this.opKeeper=readerService.getOpKeeper();
    }

    public boolean removeOProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        Set<OWLAxiom> toRemove = new HashSet<>();
        for (OWLAxiom select : ontology.getAxioms())
        {
            if(select.getSignature().contains(property))
            {
                toRemove.add(select);
            }
        }
        ontology.getOWLOntologyManager().removeAxioms(ontology, toRemove);
        try {
            ontology.getOWLOntologyManager().saveOntology(ontology);
            if (axiomHandler.checkConsistency()){
                refreshOPropertyData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{addAll(toRemove);}},
                        new ArrayList<OWLNamedIndividual>(){{
                            addAll(pelletReasoner.getInstances(
                                    (OWLClassExpression) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY),true)
                                    .getFlattened());}},
                        new ArrayList<OWLAnnotation>(){{
                            addAll(EntitySearcher.getAnnotations(
                                    axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY), ontology));}},
                        EChangeType.DELETE,
                        p,
                        EConceptType.OBJECT_PROPERTY,
                        description
                );
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean addSubOProperty(String p, String pa, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty parent = (OWLObjectProperty) axiomHandler.getOWLEntity(pa,EntityType.OBJECT_PROPERTY);
        OWLSubObjectPropertyOfAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(property,parent);
        return addAxiomEx(p, axiom, description);
    }

    public boolean addFunctionalProperty(String p, String description) {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return addAxiomEx(p, axiom, description);
    }
    public boolean addInverseFunctionalProperty(String p,String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return addAxiomEx(p, declare, description);
    }
    public boolean addTransitiveProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return addAxiomEx(p, declare, description);
    }
    public boolean addSymmetricProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return addAxiomEx(p, declare, description);
    }
    public boolean addAsymmetricProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return addAxiomEx(p, declare, description);
    }
    public boolean addReflexiveProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return addAxiomEx(p, declare, description);
    }
    public boolean addIreflexiveProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return addAxiomEx(p, declare, description);
    }

    public boolean removeFunctionalProperty(String p,String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }
    public boolean removeInverseFunctionalProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }
    public boolean removeTransitiveProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }

    public boolean removeSymmetricProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }
    public boolean removeAsymmetricProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }
    public boolean removeReflexiveProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }
    public boolean removeIreflexiveProperty(String p, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return addAxiomEx2(p, declare, description);
    }

    public boolean addInverseProperty(String prop, String iProp, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty iProperty = (OWLObjectProperty) axiomHandler.getOWLEntity(iProp,EntityType.OBJECT_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(property, iProperty);
        return addAxiomEx(prop, axiom, description);

    }

    public boolean removeInverseProperty(String prop,String inverse, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        Set<OWLInverseObjectPropertiesAxiom> toRemove = ontology.getInverseObjectPropertyAxioms(property);
        toRemove.removeIf(
                a->!(a.getFirstProperty().asOWLObjectProperty().getIRI().getShortForm().equals(inverse)
                ||a.getSecondProperty().asOWLObjectProperty().getIRI().getShortForm().equals(inverse)));
        ontology.getOWLOntologyManager().removeAxioms(ontology, toRemove);
        try {
            ontology.getOWLOntologyManager().saveOntology(ontology);
            if(axiomHandler.checkConsistency()){
                refreshOPropertyData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{
                            addAll(toRemove);}},null,null,
                        EChangeType.ADD,prop, EConceptType.OBJECT_PROPERTY, description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addDisOProperty(String prop,String dis,String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty disP = (OWLObjectProperty) axiomHandler.getOWLEntity(dis,EntityType.OBJECT_PROPERTY);
        OWLDisjointObjectPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return addAxiomEx(prop,axiom,description);
    }

    public boolean removeDisOProperty(String prop,String dis, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty disP = (OWLObjectProperty) axiomHandler.getOWLEntity(dis,EntityType.OBJECT_PROPERTY);
        OWLDisjointObjectPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return addAxiomEx2(prop,axiom,description);
    }

//    public List<String> getDomains(String prop){
//        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
//        Set<OWLObjectPropertyDomainAxiom> domainAxioms = ontology.getObjectPropertyDomainAxioms(property);
//
//        List<String> domains = new ArrayList<>();
//        for(OWLObjectPropertyDomainAxiom a: domainAxioms){
//            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
//        }
//        return domains;
//    }

    public boolean addDomain(String prop,String domain, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(domain,EntityType.CLASS);
        OWLObjectPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property,d);
        return addAxiomEx(prop,axiom,description);
    }

    public boolean removeDomain(String prop,String domain, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(domain,EntityType.CLASS);
        OWLObjectPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property,d);
        return addAxiomEx2(prop,axiom,description);
    }

//    public List<String> getRanges(String prop){
//        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
//        Set<OWLObjectPropertyRangeAxiom> domainAxioms = ontology.getObjectPropertyRangeAxioms(property);
//
//        List<String> ranges = new ArrayList<>();
//        for(OWLObjectPropertyRangeAxiom a: domainAxioms){
//            ranges.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
//        }
//        return ranges;
//    }

    public boolean addRange(String prop,String range, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass r = (OWLClass) axiomHandler.getOWLEntity(range,EntityType.CLASS);
        OWLObjectPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return addAxiomEx(prop,axiom,description);
    }

    public boolean removeRange(String prop,String range, String description){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass r = (OWLClass) axiomHandler.getOWLEntity(range,EntityType.CLASS);
        OWLObjectPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return addAxiomEx2(prop,axiom,description);
    }

    private boolean addAxiomEx(String p, OWLAxiom axiom, String description) {
        try {
            if(axiomHandler.addAxiom(axiom)){
                refreshOPropertyData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{
                            add(axiom);}},null,null,
                        EChangeType.ADD,p, EConceptType.OBJECT_PROPERTY, description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addAxiomEx2(String p, OWLAxiom axiom, String description) {
        try {
            if(axiomHandler.removeAxiom(axiom)){
                refreshOPropertyData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{
                            add(axiom);}},null,null,
                        EChangeType.DELETE,p, EConceptType.OBJECT_PROPERTY, description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
