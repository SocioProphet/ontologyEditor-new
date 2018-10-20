package com.rmdc.ontologyEditor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Lotus on 8/16/2017.
 * includes functionality related to OWL classes
 */
@Service
public class ClassService {

    private TreeNode classTree;
    private final OWLReasoner structuralReasoner;
    private final OWLOntology ontology;
    private final AxiomHandler axiomHandler;
    private final ExpressionCreator  expCreator;
    private Map<String,ClassKeeper> classKeeper;
    private final OWLReasoner pelletReasoner;
    private final ConceptReaderService readerService;
    private List<String> classList;
    private List<String> individualList;
   // private List<String> dataTypeList;

    private String treeString=null;
    @Autowired
    public ClassService(OWLReasoner structuralReasoner, OWLOntology ontology,
                        AxiomHandler axiomHandler, ExpressionCreator expCreator, ConceptReaderService readerService,
                        OWLReasoner pelletReasoner) {
        this.structuralReasoner = structuralReasoner;
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;
        this.expCreator = expCreator;
        this.pelletReasoner = pelletReasoner;
        this.readerService = readerService;

        this.classKeeper = readerService.getClassKeeper();
        printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
    }

    private void printHierarchy(OWLClass clazz) {
        if (structuralReasoner.isSatisfiable(clazz)) {
            if(classTree == null){
                classTree = new TreeNode(clazz.getIRI().getShortForm());
            }

            for (OWLClass child : structuralReasoner.getSubClasses(clazz, true).getFlattened()) {
                if (structuralReasoner.isSatisfiable(child)) {
                    UtilMethods.searchTree(
                            clazz.getIRI().getShortForm(),classTree).addChild(child.getIRI().getShortForm());
                    if (!child.equals(clazz)) {
                        printHierarchy(child);
                    }
                }
            }
        }
    }

    public String getClassTree(boolean build){
        if(build){
            classTree=null;
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        }
        treeString = UtilMethods.buildTree((treeString==null|| build), classTree);
        return treeString;
    }

    public ClassKeeper getClassKeeper(String owlClz){
        return classKeeper.get(owlClz);
    }

    public boolean removeAnnotation(int index, String owlClass, String description){
        Set<OWLAnnotation> annotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(
                axiomHandler.getOWLEntity(owlClass,EntityType.CLASS).getIRI(), ontology);
        OWLAnnotation toRemove = annotations.stream().filter(
                a->a.getValue().asLiteral().asSet().iterator().next().getLiteral().equals(
                        classKeeper.get(owlClass).getAnnotations().get(index).getValue())).findFirst().get();
        ontology.getOWLOntologyManager().applyChange(
                new RemoveAxiom(ontology,ontology.getOWLOntologyManager().getOWLDataFactory()
                        .getOWLAnnotationAssertionAxiom(
                        axiomHandler.getOWLEntity(owlClass,EntityType.CLASS).getIRI(), toRemove)));
        try {
            ontology.getOWLOntologyManager().saveOntology(ontology);
            if(axiomHandler.checkConsistency()){
                refreshClassData();

                DBService.updateDBQueue(
                        null,null, new ArrayList<OWLAnnotation>(){{add(toRemove);}},
                        EChangeType.DELETE, owlClass, EConceptType.OWL_CLASS,description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addAnnotation(String key, String value, String owlClass, String description){
        OWLAnnotationProperty property;
        switch (key) {
            case "comment":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSComment();
                break;
            case "isDefinedBy":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSIsDefinedBy();
                break;
            case "label":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSLabel();
                break;
            case "seeAlso":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSSeeAlso();
                break;
            case "bckcw":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLBackwardCompatibleWith();
                break;
            case "deprecated":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeprecated();
                break;
            case "incomp":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLIncompatibleWith();
                break;
            case "versionInfo":
                property = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLVersionInfo();
                break;
            default:
                property = (OWLAnnotationProperty) axiomHandler.getOWLEntity(key, EntityType.ANNOTATION_PROPERTY);
                break;
        }

        OWLClass range = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLLiteral label = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(value);

        OWLAnnotation pA = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(property, label);
       // ontology.getOWLOntologyManager().applyChange(new AddOntologyAnnotation(ontology,pA));
        ontology.getOWLOntologyManager().applyChange(
                new AddAxiom(ontology,ontology.getOWLOntologyManager().getOWLDataFactory()
                        .getOWLAnnotationAssertionAxiom(range.getIRI(), pA)));

        try {
            ontology.getOWLOntologyManager().saveOntology(ontology);
            if(axiomHandler.checkConsistency()){
                refreshClassData();
                DBService.updateDBQueue(
                        null,null, new ArrayList<OWLAnnotation>(){{add(pA);}},
                        EChangeType.ADD, owlClass,EConceptType.OWL_CLASS,description);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addClass(String className, String description){
        if(!ontology.containsEntityInSignature(axiomHandler.getIRI(className))){
            OWLEntity entity = axiomHandler.getOWLEntity(className,EntityType.CLASS);
            OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeclarationAxiom(entity);

            try {
                if( axiomHandler.addAxiom(declare)){
                    refreshClassData();
                    DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{add(declare);}},null,null,
                        EChangeType.DELETE,className, EConceptType.OWL_CLASS, description);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        return false;
    }

    public boolean removeClass(String owlClass, String description){

        Set<OWLAxiom> toRemove = new HashSet<>();
        for (OWLAxiom select : ontology.getAxioms())
        {
            if(select.getSignature().contains(axiomHandler.getOWLEntity(owlClass,EntityType.CLASS)))
            {
                toRemove.add(select);
            }
        }

        ontology.getOWLOntologyManager().removeAxioms(ontology, toRemove);
        try {
            ontology.getOWLOntologyManager().saveOntology(ontology);
            if(axiomHandler.checkConsistency()){
                refreshClassData();
                DBService.updateDBQueue(
                    new ArrayList<OWLAxiom>(){{addAll(toRemove);}},
                    new ArrayList<OWLNamedIndividual>(){{
                        addAll(pelletReasoner.getInstances(
                                (OWLClassExpression) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS),true)
                                .getFlattened());}},
                    new ArrayList<OWLAnnotation>(){{
                        addAll(EntitySearcher.getAnnotations(
                                axiomHandler.getOWLEntity(owlClass,EntityType.CLASS), ontology));}},
                    EChangeType.DELETE,
                    owlClass,
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
    
    public boolean addClassAxiom(DataTransfer transfer, int cOrEq){

        OWLAxiom axiom ;
        OWLClassExpression childC = (OWLClass) axiomHandler.getOWLEntity(transfer.getcConcept(),EntityType.CLASS);
        OWLClassExpression parent=getParent(childC);

        switch (transfer.getPatternType()) {
            case "o1":
                OWLClassExpression parentC = (OWLClass) axiomHandler.getOWLEntity(transfer.getClassList()
                        .get(0),EntityType.CLASS);

                axiom = getOwlAxiom(cOrEq, childC, parent, parentC);
                break;
            case "o2": {
                List<String> classList1 = transfer.getClassList();
                OWLClassExpression[] owlClasses = new OWLClassExpression[classList1.size()+1];
                for (int i = 0; i < classList1.size(); i++) {
                    owlClasses[i] =(OWLClass) axiomHandler.getOWLEntity(classList1.get(i), EntityType.CLASS);
                }

                if (cOrEq == 1) {
                    if (parent != null) {
                        owlClasses[classList1.size()] = parent;
                        axiom = expCreator.createEqAxiom(childC, expCreator.createClassIntersectionExp(parent, 
                                expCreator.createClassUnionExp(owlClasses)));
                    } else {
                        axiom = expCreator.createEqAxiom(childC, expCreator.createClassUnionExp(owlClasses));
                    }
                }else{
                    axiom = expCreator.createSubAxiom(childC, expCreator.createClassUnionExp(owlClasses));
                }
                break;
            }
            case "o3": {
                List<String> classList1 = transfer.getClassList();
                OWLClassExpression[] owlClasses = new OWLClassExpression[classList1.size()+1];
                for (int i = 0; i < classList1.size(); i++) {
                    owlClasses[i] =(OWLClass) axiomHandler.getOWLEntity(classList1.get(i), EntityType.CLASS);
                }

                if (cOrEq == 1) {
                    if (parent != null) {
                        owlClasses[classList1.size()] = parent;
                        axiom = expCreator.createEqAxiom(childC, expCreator.createClassIntersectionExp(owlClasses));
                    } else {
                        axiom = expCreator.createEqAxiom(childC, expCreator.createClassIntersectionExp(owlClasses));
                    }
                }else{
                    axiom = expCreator.createSubAxiom(childC, expCreator.createClassIntersectionExp(owlClasses));
                }

                break;
            }
            case "o4": {
                OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(transfer.getoProperties()
                        .get(0),EntityType.OBJECT_PROPERTY);
                OWLClass owlClass = (OWLClass) axiomHandler.getOWLEntity(transfer.getClassList()
                        .get(0),EntityType.OBJECT_PROPERTY);
                OWLClassExpression someValuesFrom = expCreator.createObjecctSomeValueExp(property, owlClass);

                axiom = getOwlAxiom(cOrEq, childC, parent, someValuesFrom);

                break;
            }
            case "o5": {
                OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(
                        transfer.getoProperties().get(0),EntityType.OBJECT_PROPERTY);
                List<String> classList1 = transfer.getClassList();
                OWLClassExpression[] owlClasses = new OWLClassExpression[classList1.size()+1];
                for (int i = 0; i < classList1.size(); i++) {
                    owlClasses[i] =(OWLClass) axiomHandler.getOWLEntity(classList1.get(i), EntityType.CLASS);
                }
                OWLClassExpression allValuesFrom = expCreator.createObjectAllValueExp(property,
                        expCreator.createClassUnionExp(owlClasses));

                axiom = getOwlAxiom(cOrEq, childC, parent, allValuesFrom);
                break;
            }
            case "o6": {

                OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(transfer.getdProperties()
                        .get(0),EntityType.DATA_PROPERTY);
                OWLDatatype dataType = (OWLDatatype) axiomHandler.getOWLEntity(transfer.getdTypes()
                        .get(0),EntityType.DATATYPE);
                OWLLiteral literal = ontology.getOWLOntologyManager().getOWLDataFactory()
                        .getOWLLiteral(transfer.getLiterals().get(0), dataType);
                OWLClassExpression hasValue = expCreator.createDataHasValueExp(property, literal);


                axiom = getOwlAxiom(cOrEq, childC, parent, hasValue);
                break;
            }
            default: {
                OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(
                        transfer.getoProperties().get(0),EntityType.OBJECT_PROPERTY);
                OWLClassExpression cardinality;
                switch (transfer.getCardinalityType()) {
                    case "min":
                        cardinality = expCreator.createObjectMinCardinalityExp(transfer.getCardinality(), property);
                        break;
                    case "max":
                        cardinality = expCreator.createObjectMaxCardinalityExp(transfer.getCardinality(), property);
                        break;
                    default:
                        cardinality = expCreator.createObjectExactCardinalityExp(transfer.getCardinality(), property);
                        break;
                }

                axiom = getOwlAxiom(cOrEq, childC, parent, cardinality);
                break;
            }
        }

        try {
            if(axiomHandler.addAxiom(axiom)){
                if(cOrEq==0){
                    getClassTree(true);
                }
                refreshClassData();

                DBService.updateDBQueue(
                    new ArrayList<OWLAxiom>(){{add(axiom);}},null,null,EChangeType.ADD,
                    transfer.getcConcept(),EConceptType.OWL_CLASS, transfer.getDescription());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private OWLAxiom getOwlAxiom(int cOrEq, OWLClassExpression childC, OWLClassExpression parent,
                                 OWLClassExpression parentC) {
        OWLAxiom axiom;
        if (cOrEq == 1) {
            if (parent != null) {
                axiom = expCreator.createEqAxiom(childC,
                        expCreator.createClassIntersectionExp(parent,parentC));
            } else {
                axiom = expCreator.createEqAxiom(childC, parentC);
            }
        }else{
            axiom = expCreator.createSubAxiom(childC, parentC);

        }
        return axiom;
    }

    public boolean addDisjointClass(String owlClass, String disjoint, String description){
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLClass dis = (OWLClass) axiomHandler.getOWLEntity(disjoint,EntityType.CLASS);
        OWLAxiom dja = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointClassesAxiom(current,dis);
        try {
            if(axiomHandler.addAxiom(dja)){
                refreshClassData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{add(dja);}},
                        null,null,
                        EChangeType.ADD, owlClass, EConceptType.OWL_CLASS, description );
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeDisjointClass(String owlClass, String disjoint, String description){
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLClass dis = (OWLClass) axiomHandler.getOWLEntity(disjoint,EntityType.CLASS);
        OWLAxiom dja = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointClassesAxiom(current,dis);
        return addAxiomEx(owlClass, description, dja);
    }

    public boolean addDomainOf(String owlClass, String prop, String description){
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLAxiom axiom = getOwlAxiom(prop, current);
        return addAxiomEx2(owlClass, description, axiom);
    }

    public boolean removeDomainOf(String owlClass, String prop, String description){
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLAxiom axiom = getOwlAxiom(prop, current);
        return addAxiomEx(owlClass, description, axiom);
    }

    private OWLAxiom getOwlAxiom(String prop, OWLClass current) {
        OWLAxiom axiom;
        if(ontology.containsObjectPropertyInSignature(axiomHandler.getIRI(prop))){
            OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop, EntityType.OBJECT_PROPERTY);
            axiom = ontology.getOWLOntologyManager().getOWLDataFactory()
                    .getOWLObjectPropertyDomainAxiom(property,current);
        }else{
            OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
            axiom = ontology.getOWLOntologyManager().getOWLDataFactory()
                    .getOWLDataPropertyDomainAxiom(property,current);
        }
        return axiom;
    }


    public boolean addRangeOf(String owlClass, String prop, String description) {
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLAxiom axiom= ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectPropertyRangeAxiom(property,current);
        return addAxiomEx2(owlClass, description, axiom);
    }

    public boolean removeRangeOf(String owlClass, String prop, String description) {
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(owlClass,EntityType.CLASS);
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLAxiom axiom= ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLObjectPropertyRangeAxiom(property,current);
        return addAxiomEx(owlClass, description, axiom);
    }

    public boolean renameIRI(String entity, String newName, String description){

        IRI entityToRename = axiomHandler.getIRI(entity);
        IRI newNameIRI= axiomHandler.getIRI(newName);
        boolean pass = !ontology.containsEntityInSignature(newNameIRI)
                && !newName.equals("Thing")
                && !newName.equals("topObjectProperty")
                && !newName.equals("topDataProperty")
                && !newName.equals("");
        if (pass) {
            OWLEntityRenamer owlEntityRenamer = new OWLEntityRenamer(ontology.getOWLOntologyManager(),
                    ontology.getOWLOntologyManager().getOntologies());

            final List<OWLOntologyChange> changes = owlEntityRenamer.changeIRI(entityToRename, newNameIRI);
            for (OWLOntologyChange ch : changes) {
                System.out.println(ch.getChangeRecord());
            }
            ontology.getOWLOntologyManager().applyChanges(changes);
            try {
                for (OWLOntology on : ontology.getOWLOntologyManager().getOntologies()) {
                    ontology.getOWLOntologyManager().saveOntology(on);
                }
                refreshClassData();
                DBService.updateDBQueue(
                    null,null,null, EChangeType.UPDATE, entity,
                    EConceptType.OWL_CLASS,description);
                return true;
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean removeSubClassOfAxiom(String owlClass, int id, String description){
        OWLAxiom axiom =classKeeper.get(owlClass).getSubClassRestrictions().get(id).getAxiomObj();
        return addAxiomEx(owlClass, description, axiom);

    }

    public boolean removeEqClassOfAxiom(String owlClass, int id, String description){
        OWLAxiom axiom =classKeeper.get(owlClass).getEqClassRestrictions().get(id).getAxiomObj();
        return addAxiomEx(owlClass, description, axiom);
    }

    private List<OWLClass> getChildClasses(OWLClass clazz){
        List<OWLClass> classes = new ArrayList<>();

        if (structuralReasoner.isSatisfiable(clazz)) {
            classes.add(clazz);

            for (OWLClass child : structuralReasoner.getSubClasses(clazz, true).getFlattened()) {
                if (structuralReasoner.isSatisfiable(child)) {
                    if (!child.equals(clazz)) {
                        getChildClasses(child);
                    }
                }
            }
        }
        return  classes;
    }

    public List<String> getAllClasses(){
        if(classList==null){
            List<String> list = new ArrayList<>();
            Set<OWLClass> classes = ontology.getClassesInSignature();
            for(OWLClass owlClass:classes){
                list.add(owlClass.getIRI().getShortForm());
            }
            list.remove("Thing");
            Collections.sort(list);

            this.classList = list;
        }

        return classList;
    }

    public List<String> getAllIndividuals(){

        if(individualList==null){
            List<String> individuals = new ArrayList<>();
            Set<OWLNamedIndividual> properties = ontology.getIndividualsInSignature();
            for(OWLNamedIndividual i:properties){
                individuals.add(i.getIRI().getShortForm());
            }
            Collections.sort(individuals);

            this.individualList=individuals;
        }

        return this.individualList;
    }

//    public List<String> getAllDataTypes(){
//
//        if(dataTypeList==null){
//            List<String> dTypes = new ArrayList<>();
//            Set<OWLDatatype> properties = ontology.getDatatypesInSignature();
//            for(OWLDatatype t:properties){
//                dTypes.add(t.getIRI().getShortForm());
//            }
//            Collections.sort(dTypes);
//            this.dataTypeList=dTypes;
//        }
//
//        return this.dataTypeList;
//    }

    private OWLClass getParent(OWLClassExpression clz) {
        Set<OWLClass> classes = ontology.getClassesInSignature();
        OWLClass parent=null;
        for(OWLClass c:classes){

            for (OWLClass child : structuralReasoner.getSubClasses(c, true).getFlattened()) {
                if (structuralReasoner.isSatisfiable(child)) {
                    if(child.equals(clz)){
                        parent = c;
                    }
                }
            }
        }
        return parent;
    }

    public List<String> getRange(String p){
        List<String> rangeOf = new ArrayList<>();
        Set<OWLObjectPropertyRangeAxiom> da = ontology.getObjectPropertyRangeAxioms((OWLObjectPropertyExpression) axiomHandler.getOWLEntity(p,EntityType.OBJECT_PROPERTY));
        for(OWLObjectPropertyRangeAxiom a:da){
            OWLClass clz = a.getClassesInSignature().iterator().next();
            List<OWLClass> classes = getChildClasses(clz);
            for(OWLClass c:classes){
                rangeOf.add(c.getIRI().getShortForm());
//                Set<OWLNamedIndividual> nodes = structuralReasoner.getInstances(c,true).getFlattened();
//                for(OWLNamedIndividual i:nodes){
//                    rangeOf.add(i.getIRI().getShortForm());
//                }
            }
        }
        return rangeOf;
    }

    private void refreshClassData(){
        readerService.readComponentData();
        this.classKeeper=readerService.getClassKeeper();
    }
    private boolean addAxiomEx2(String owlClass, String description, OWLAxiom axiom) {
        try {
            if(axiomHandler.addAxiom(axiom)){
                refreshClassData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{
                            add(axiom);}},null,null, EChangeType.ADD,
                        owlClass, EConceptType.OWL_CLASS, description);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addAxiomEx(String owlClass, String description, OWLAxiom axiom) {
        try {
            if(axiomHandler.removeAxiom(axiom)){
                refreshClassData();
                DBService.updateDBQueue(
                        new ArrayList<OWLAxiom>(){{add(axiom);}},null,null, EChangeType.DELETE,
                        owlClass, EConceptType.OWL_CLASS, description);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
