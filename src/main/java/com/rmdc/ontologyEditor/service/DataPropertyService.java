package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.TreeNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
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
    private final OWLOntology ontology;
    private final AxiomHandler axiomHandler;


    public DataPropertyService(OWLReasoner structuralReasoner, OWLReasoner pelletReasoner, OWLOntology ontology, AxiomHandler axiomHandler) {
        this.structuralReasoner = structuralReasoner;
        this.pelletReasoner = pelletReasoner;
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;
        try {
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopDataProperty());
        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    public TreeNode getDataPropertyTree() {
        return dataPropertyTree;
    }

    private void printHierarchy(OWLDataProperty property)
            throws OWLException {

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

    public boolean addDProperty(String p) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeclarationAxiom(property);
        return axiomHandler.addAxiom(declare);
    }

    public boolean addSubDProperty(String p, String pa) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLDataProperty parent = (OWLDataProperty) axiomHandler.getOWLEntity(pa,EntityType.DATA_PROPERTY);
        OWLSubDataPropertyOfAxiom sub = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubDataPropertyOfAxiom(property,parent);
        return axiomHandler.addAxiom(sub);
    }

    public boolean removeDProperty(String p) throws Exception {
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
        ontology.getOWLOntologyManager().saveOntology(ontology);
        return axiomHandler.checkConsistency();
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

    public boolean addFunctionalDProperty(String p) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalDataPropertyAxiom(property);
        return axiomHandler.addAxiom(declare);
    }

    public boolean removeFunctionalDProperty(String p) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(p,EntityType.DATA_PROPERTY);
        OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLFunctionalDataPropertyAxiom(property);
        return axiomHandler.removeAxiom(declare);
    }

    public boolean isFunctional(String prop){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        return !ontology.getFunctionalDataPropertyAxioms(property).isEmpty();

    }

    public List<String> getDisjointDProperties(String prop){
        List<String> disjoints = new ArrayList<>();
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        Set<OWLDisjointDataPropertiesAxiom> disOP = ontology.getDisjointDataPropertiesAxioms(property);
        for(OWLDisjointDataPropertiesAxiom p: disOP){
            for(OWLDataProperty pr:p.getDataPropertiesInSignature()){
                String sf = pr.getIRI().getShortForm();
                if(!sf.equals(prop)){
                    disjoints.add(sf);
                }
            }
        }
        Collections.sort(disjoints);
        disjoints.remove(prop);
        return disjoints;
    }

    public List<String> getNonDisjointDProperties(String prop){
        List<String> all = getAllDProperties();
        List<String> dis =getDisjointDProperties(prop);
        all.removeAll(dis);
        return all;
    }

    public boolean addDisDProperty(String prop,String dis) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDataProperty disP = (OWLDataProperty) axiomHandler.getOWLEntity(dis,EntityType.DATA_PROPERTY);
        OWLDisjointDataPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(property,disP);
        return axiomHandler.addAxiom(axiom);
    }

    public boolean removeDisDProperty(String prop,String dis) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDataProperty disP = (OWLDataProperty) axiomHandler.getOWLEntity(dis,EntityType.DATA_PROPERTY);
        OWLDisjointDataPropertiesAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(property,disP);
        return axiomHandler.removeAxiom(axiom);
    }

    public List<String> getDPDomains(String prop){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        Set<OWLDataPropertyDomainAxiom> domainAxioms = ontology.getDataPropertyDomainAxioms(property);

        List<String> domains = new ArrayList<>();
        for(OWLDataPropertyDomainAxiom a: domainAxioms){
            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return domains;
    }

    public boolean addDPDomain(String prop,String doamin) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(doamin,EntityType.CLASS);
        OWLDataPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyDomainAxiom(property,d);

        return axiomHandler.addAxiom(axiom);
    }

    public boolean removeDPDomain(String prop,String doamin) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLClass d = (OWLClass) axiomHandler.getOWLEntity(doamin,EntityType.CLASS);
        OWLDataPropertyDomainAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyDomainAxiom(property,d);

        return axiomHandler.removeAxiom(axiom);
    }

    public List<String> getDPRanges(String prop){
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        Set<OWLDataPropertyRangeAxiom> domainAxioms = ontology.getDataPropertyRangeAxioms(property);

        List<String> ranges = new ArrayList<>();
        for(OWLDataPropertyRangeAxiom a: domainAxioms){
            ranges.add(a.getDatatypesInSignature().iterator().next().getIRI().getShortForm());
        }
        return ranges;
    }

    public boolean addDPRange(String prop,String range) throws Exception {
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDatatype r = (OWLDatatype) axiomHandler.getOWLEntity(range,EntityType.DATATYPE);
        OWLDataPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyRangeAxiom(property,r);
        return axiomHandler.addAxiom(axiom);
    }

    public boolean removeDPRange(String prop,String range) throws Exception {
        String mRange=null;
        if(range.equals("Literal")){
            mRange="rdfs:Literal";
        }else if(range.equals("XMLLiteral") ||range.equals("PlainLiteral")){
            mRange="rdf:"+range;
        }else if(range.equals("real") || range.equals("rational")){
            mRange="owl:"+range;
        }else{
            mRange="xsd:"+range;
        }
        OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
        OWLDatatype r = (OWLDatatype) axiomHandler.getOWLEntity(mRange,EntityType.DATATYPE);
        OWLDataPropertyRangeAxiom axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyRangeAxiom(property,r);
        return axiomHandler.removeAxiom(axiom);
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
}
