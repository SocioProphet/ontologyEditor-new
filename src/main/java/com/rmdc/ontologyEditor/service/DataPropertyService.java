package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Created by Lotus on 8/20/2017.
 */

@Service
public class DataPropertyService {
    
    private TreeNode dataPropertyTree;
    private final OWLReasoner structuralReasoner;
    private final OWLReasoner pelletReasoner;
    private final ConceptReaderService readerService;
    private final OWLOntology ontology;
    private final AxiomHandler axiomHandler;
    private Map<String,DPKeeper> dpKeeper;
    private String treeString=null;

    public DataPropertyService(OWLReasoner structuralReasoner, OWLReasoner pelletReasoner, ConceptReaderService readerService, OWLOntology ontology, AxiomHandler axiomHandler) {
        this.structuralReasoner = structuralReasoner;
        this.pelletReasoner = pelletReasoner;
        this.readerService = readerService;
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;

        this.dpKeeper = readerService.getDpKeeper();
        printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopDataProperty());
    }

    private void printHierarchy(OWLDataProperty property){

        if(dataPropertyTree == null){
            dataPropertyTree = new TreeNode(property.getIRI().getShortForm());
        }

        for (OWLDataPropertyExpression child : structuralReasoner.getSubDataProperties(property, true).getFlattened()) {
            if(!child.asOWLDataProperty().equals(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLBottomDataProperty())){
                UtilMethods.searchTree(property.asOWLDataProperty().getIRI().getShortForm(), dataPropertyTree).addChild(child.asOWLDataProperty().getIRI().getShortForm());
            }
            if (!child.equals(property)) {
                printHierarchy(child.asOWLDataProperty());
            }
        }
    }

    public String getDPropertyTree(boolean build){
        if(build){
            dataPropertyTree=null;
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopDataProperty());
        }
        treeString = UtilMethods.buildTree((treeString==null || build), dataPropertyTree);
        return treeString;
    }


    public DPKeeper getKeeper(String key){
        return dpKeeper.get(key);
    }

    private void refreshDPData(){
        readerService.readComponentData();
        this.dpKeeper =readerService.getDpKeeper();
    }

    public boolean addDProperty(String p, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeclarationAxiom(property);

        return addAxiomEx(p, description, declare);
    }

    public boolean addSubDProperty(String p, String pa, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLDataProperty parent = (OWLDataProperty) axiomHandler.getOWLEntity(pa,EntityType.DATA_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubDataPropertyOfAxiom(property,parent);
        return addAxiomEx(p, description, axiom);
    }

    public boolean removeDProperty(String p, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
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
            if(axiomHandler.checkConsistency()){
                refreshDPData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{addAll(toRemove);}},
                        new ArrayList<OWLNamedIndividual>(){{
                            addAll(pelletReasoner.getInstances(
                                    (OWLClassExpression) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY),true)
                                    .getFlattened());}},
                        new ArrayList<OWLAnnotation>(){{
                            addAll(EntitySearcher.getAnnotations(
                                    axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY), ontology));}},
                        EChangeType.DELETE,
                        p,
                        EConceptType.OWL_CLASS,
                        description
                );
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<String> getAllDProperties(){
        Set<OWLDataProperty> propertySet = ontology.getDataPropertiesInSignature();
        List<String> properties = new ArrayList<>();
        for(OWLDataProperty p: propertySet){
            properties.add(p.getIRI().getShortForm());
        }
        Collections.sort(properties);
        return properties;
    }

    public boolean addFunctionalDProperty(String p, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalDataPropertyAxiom(property);
        return addAxiomEx(p, description, axiom);
    }

    public boolean removeFunctionalDProperty(String p, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalDataPropertyAxiom(property);
        return addAxiomEx2(p, description, axiom);
    }

    public boolean addDisDProperty(String prop,String dis, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDataProperty disP = (OWLDataProperty) axiomHandler.getOWLEntity(dis,EntityType.DATA_PROPERTY);
        OWLDisjointDataPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(property,disP);
        return addAxiomEx(prop, description, axiom);
    }

    public boolean removeDisDProperty(String prop,String dis, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDataProperty disP = (OWLDataProperty) axiomHandler.getOWLEntity(dis,EntityType.DATA_PROPERTY);
        OWLAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(property,disP);
        return addAxiomEx2(prop, description, axiom);
    }

    public boolean addDPDomain(String prop,String domain, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(domain,EntityType.CLASS);
        OWLDataPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyDomainAxiom(property,d);

        return addAxiomEx(prop, description, axiom);
    }

    public boolean removeDPDomain(String prop,String domain, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(domain,EntityType.CLASS);
        OWLDataPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyDomainAxiom(property,d);

        return addAxiomEx2(prop, description, axiom);
    }

    public boolean addDPRange(String prop,String range, String description){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDatatype r = (OWLDatatype) axiomHandler.getOWLEntity(range,EntityType.DATATYPE);
        OWLDataPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyRangeAxiom(property,r);
        return addAxiomEx(prop, description, axiom);
    }

    public boolean removeDPRange(String prop,String range, String description){
        String mRange;
        switch (range) {
            case "Literal":
                mRange = "rdfs:Literal";
                break;
            case "XMLLiteral":
            case "PlainLiteral":
                mRange = "rdf:" + range;
                break;
            case "real":
            case "rational":
                mRange = "owl:" + range;
                break;
            default:
                mRange = "xsd:" + range;
                break;
        }
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDatatype r = (OWLDatatype) axiomHandler.getOWLEntity(mRange,EntityType.DATATYPE);
        OWLDataPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyRangeAxiom(property,r);
        return addAxiomEx2(prop, description, axiom);
    }

    public List<String> getDataTypes(){
        List<String> dTypes  = new ArrayList<>();
        dTypes.add("owl:rational");
        dTypes.add("owl:real");
        dTypes.add("rdf:PlainLiteral");
        dTypes.add("rdf:XMLLiteral");
        dTypes.add("rdfs:Literal");
        dTypes.add("xsd:boolean");
        dTypes.add("xsd:dateTime");
        dTypes.add("xsd:dateTimeStamp");
        dTypes.add("xsd:decimal");
        dTypes.add("xsd:int");
        dTypes.add("xsd:float");
        dTypes.add("xsd:language");
        dTypes.add("xsd:Name");
        dTypes.add("xsd:negativeInteger");
        dTypes.add("xsd:nonNegativeInteger");
        dTypes.add("xsd:nonPositiveInteger");
        dTypes.add("xsd:positiveInteger");
        dTypes.add("xsd:string");
        dTypes.add("xsd:string");

        return dTypes;

    }

    private boolean addAxiomEx(String p, String description, OWLAxiom axiom) {
        try {
            if(axiomHandler.addAxiom(axiom)){
                refreshDPData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{
                            add(axiom);}},null,null,
                        EChangeType.ADD, p, EConceptType.DATA_PROPERTY, description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addAxiomEx2(String prop, String description, OWLAxiom axiom) {
        try {
            if(axiomHandler.removeAxiom(axiom)){
                refreshDPData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{
                            add(axiom);}},null,null,
                        EChangeType.DELETE, prop, EConceptType.DATA_PROPERTY, description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
