package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.TreeNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.springframework.context.MessageSource;
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

    public ObjectPropertyService(OWLReasoner structuralReasoner, OWLReasoner pelletReasoner, OWLOntology ontology, MessageSource messageSource, AxiomHandler axiomHandler) {
        this.structuralReasoner = structuralReasoner;
        this.pelletReasoner = pelletReasoner;
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;
        try {
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopObjectProperty());
        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    public TreeNode getObjectPropertyTree() {
        return objectPropertyTree;
    }

    public void printHierarchy(OWLObjectProperty property) throws OWLException {

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
    

    public boolean addOProperty(String p) throws Exception {        
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeclarationAxiom(property);
        return axiomHandler.addAxiom(declare);
    }

    public boolean removeOProperty(String p) throws Exception {
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
        ontology.getOWLOntologyManager().saveOntology(ontology);

        return axiomHandler.checkConsistency();
    }

    public List<String> getAllOProperties(){
         Set<OWLObjectProperty> propertySet = ontology.getObjectPropertiesInSignature();
         List<String> properties = new ArrayList<>();
         for(OWLObjectProperty p: propertySet){
             properties.add(p.getIRI().getShortForm());
         }
        Collections.sort(properties);
         return properties;
    }

    public boolean addSubOProperty(String p, String pa) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty parent = (OWLObjectProperty) axiomHandler.getOWLEntity(pa,EntityType.OBJECT_PROPERTY);
        OWLSubObjectPropertyOfAxiom sub = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(property,parent);
        return axiomHandler.addAxiom(sub);
    }

    public boolean addFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }
    public boolean addInverseFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }
    public boolean addTransitiveProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }
    public boolean addSymmetricProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }
    public boolean addAsymmetricProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }
    public boolean addReflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }
    public boolean addIreflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }

    public boolean removeFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }
    public boolean removeInverseFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }
    public boolean removeTransitiveProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }
    public boolean removeSymetricProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }
    public boolean removeAsymetricProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }
    public boolean removeReflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }
    public boolean removeIreflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }

    public String getInverseProperty(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        if(ontology.getInverseObjectPropertyAxioms(property).isEmpty()){
            return null;
        }
        String i = ontology.getInverseObjectPropertyAxioms(property).iterator().next().getFirstProperty().getNamedProperty().getIRI().getShortForm();
        if(i.equals(prop)){
            return ontology.getInverseObjectPropertyAxioms(property).iterator().next().getSecondProperty().getNamedProperty().getIRI().getShortForm();
        }
        return i;
    }

    public boolean addInverseProperty(String prop, String iProp) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty iProperty = (OWLObjectProperty) axiomHandler.getOWLEntity(iProp,EntityType.OBJECT_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(property, iProperty);

        return axiomHandler.addAxiom(axiom);

    }

    public boolean removeInverseProperty(String prop) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        Set<OWLInverseObjectPropertiesAxiom> toRemove = ontology.getInverseObjectPropertyAxioms(property);

        ontology.getOWLOntologyManager().removeAxioms(ontology, toRemove);
        ontology.getOWLOntologyManager().saveOntology(ontology);
        return axiomHandler.checkConsistency();
    }

    public boolean isFunctional(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getFunctionalObjectPropertyAxioms(property).isEmpty();

    }

    public boolean isInverseFunctional(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getInverseFunctionalObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isSymmetric(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getSymmetricObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isAsymmetric(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getAsymmetricObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isTransitive(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getTransitiveObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isReflexive(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getReflexiveObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isIrreflexive(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        return !ontology.getIrreflexiveObjectPropertyAxioms(property).isEmpty();
    }

    public List<String> getDisjointProperties(String prop){
        List<String> disjoints = new ArrayList<>();
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        Set<OWLDisjointObjectPropertiesAxiom> disOP = ontology.getDisjointObjectPropertiesAxioms(property);
        for(OWLDisjointObjectPropertiesAxiom p: disOP){
            disjoints.add(p.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
        }
        Collections.sort(disjoints);
        return disjoints;
    }

    public List<String> getNonDisjointProperties(String prop){
        List<String> all = getAllOProperties();
        List<String> dis =getDisjointProperties(prop);
        all.removeAll(dis);
        return all;
    }

    public boolean addDisOProperty(String prop,String dis) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty disP = (OWLObjectProperty) axiomHandler.getOWLEntity(dis,EntityType.OBJECT_PROPERTY);
        OWLDisjointObjectPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return axiomHandler.addAxiom(axiom);
    }

    public boolean removeDisOProperty(String prop,String dis) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLObjectProperty disP = (OWLObjectProperty) axiomHandler.getOWLEntity(dis,EntityType.OBJECT_PROPERTY);
        OWLDisjointObjectPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return axiomHandler.removeAxiom(axiom);
    }

    public List<String> getDomains(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        Set<OWLObjectPropertyDomainAxiom> domainAxioms = ontology.getObjectPropertyDomainAxioms(property);

        List<String> domains = new ArrayList<>();
        for(OWLObjectPropertyDomainAxiom a: domainAxioms){
            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return domains;
    }

    public boolean addDomain(String prop,String domain) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(domain,EntityType.CLASS);
        OWLObjectPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property,d);

        return axiomHandler.addAxiom(axiom);
    }

    public boolean removeDomain(String prop,String domain) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(domain,EntityType.CLASS);
        OWLObjectPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property,d);

        return axiomHandler.removeAxiom(axiom);
    }

    public List<String> getRanges(String prop){
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        Set<OWLObjectPropertyRangeAxiom> domainAxioms = ontology.getObjectPropertyRangeAxioms(property);

        List<String> ranges = new ArrayList<>();
        for(OWLObjectPropertyRangeAxiom a: domainAxioms){
            ranges.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return ranges;
    }

    public boolean addRange(String prop,String range) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass r = (OWLClass) axiomHandler.getOWLEntity(range,EntityType.CLASS);
        OWLObjectPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return axiomHandler.addAxiom(axiom);
    }

    public boolean removeRange(String prop,String range) throws Exception {
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLClass r = (OWLClass) axiomHandler.getOWLEntity(range,EntityType.CLASS);
        OWLObjectPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return axiomHandler.removeAxiom(axiom);
    }

    public List<String> getOPCharacteristics(String property){
        List<String> propertyTypes = new ArrayList<>();
        if(isFunctional(property)){
            propertyTypes.add("F");
        }
        if(isInverseFunctional(property)){
            propertyTypes.add("IF");
        }
        if(isTransitive(property)){
            propertyTypes.add("T");
        }
        if(isSymmetric(property)){
            propertyTypes.add("S");
        }if(isAsymmetric(property)){
            propertyTypes.add("AS");
        }
        if(isReflexive(property)){
            propertyTypes.add("R");
        }if(isIrreflexive(property)){
            propertyTypes.add("IR");
        }
        return propertyTypes;
    }
}
