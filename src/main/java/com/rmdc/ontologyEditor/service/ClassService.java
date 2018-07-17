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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.time.LocalDateTime;
import java.util.*;


/**
 * Created by Lotus on 8/16/2017.
 */
@Service
public class ClassService {

    private TreeNode classTree;
    private final OWLReasoner structuralReasoner;
    private final OWLOntology ontology;
    private final AxiomHandler axiomHandler;
    private final Map<String,ClassKeeper> classKeeper;

    private List<String> classList;
    private List<String> individualList;
    private List<String> dataTypeList;

    private String treeString=null;

    @Autowired
    public ClassService(OWLReasoner structuralReasoner,OWLOntology ontology, AxiomHandler axiomHandler, Map<String, ClassKeeper> classKeeper) {
        this.structuralReasoner = structuralReasoner;
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;
        this.classKeeper = classKeeper;
        try {
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    public ClassKeeper getClassKeeper(String owlClz){
        return classKeeper.get(owlClz);
    }

    public boolean removeAnnotation(int index, String owlClass) throws Exception {
        Set<OWLAnnotation> annotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(axiomHandler.getOWLEntity(owlClass,EntityType.CLASS).getIRI(), ontology);
        OWLAnnotation toRemove = annotations.stream().filter(a->a.getValue().asLiteral().asSet().iterator().next().getLiteral().equals(classKeeper.get(owlClass).getAnnotations().get(index).getValue())).findFirst().get();
        ontology.getOWLOntologyManager().applyChange(new RemoveAxiom(ontology,ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(axiomHandler.getOWLEntity(owlClass,EntityType.CLASS).getIRI(), toRemove)));
        ontology.getOWLOntologyManager().saveOntology(ontology);
        classKeeper.get(owlClass).getAnnotations().remove(index);

        TempChangesKeeper changesKeeper = new TempChangesKeeper(
                null,
                null,
                new ArrayList<OWLAnnotation>().add(toRemove),
                LocalDateTime.now(),
                EChangeType.ADD,
                owlClass,
                EConceptType.OWL_CLASS,
                description
                );

        return axiomHandler.checkConsistency();
    }

    public boolean addAnnotation(String key, String value, String owlClass) throws Exception {
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
        ontology.getOWLOntologyManager().applyChange(new AddAxiom(ontology,ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(range.getIRI(), pA)));

        ontology.getOWLOntologyManager().saveOntology(ontology);
        classKeeper.get(owlClass).getAnnotations().add(new AnnotationKeeper(property.getIRI().getShortForm(), value));
        return axiomHandler.checkConsistency();
    }

    private void printHierarchy(OWLClass clazz) throws OWLException {
        if (structuralReasoner.isSatisfiable(clazz)) {
            if(classTree == null){
                classTree = new TreeNode(clazz.getIRI().getShortForm());
            }

            for (OWLClass child : structuralReasoner.getSubClasses(clazz, true).getFlattened()) {
                if (structuralReasoner.isSatisfiable(child)) {
                    UtilMethods.searchTree(clazz.getIRI().getShortForm(),classTree).addChild(child.getIRI().getShortForm());
                    if (!child.equals(clazz)) {
                        printHierarchy(child);
                    }
                }
            }
        }
    }

    public String getClassTree(boolean build) throws JsonProcessingException, OWLException {
        if(build){
            printHierarchy(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        }
        if(treeString==null|| build){
            ObjectMapper mapper = new ObjectMapper();
            treeString = mapper.writeValueAsString(classTree);
        }
        return treeString;
    }

    private List<OWLClass> getChildClasses(OWLClass clazz) throws OWLException {
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

    public List<String> getAllDataTypes(){

        if(dataTypeList==null){
            List<String> dTypes = new ArrayList<>();
            Set<OWLDatatype> properties = ontology.getDatatypesInSignature();
            for(OWLDatatype t:properties){
                dTypes.add(t.getIRI().getShortForm());
            }
            Collections.sort(dTypes);
            this.dataTypeList=dTypes;
        }

        return this.dataTypeList;
    }


    public boolean addClass(String className) throws Exception {
        if(!ontology.getOWLOntologyManager().contains(axiomHandler.getIRI(className))){
            OWLEntity entity = axiomHandler.getOWLEntity(className,EntityType.CLASS);
            OWLAxiom declare = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDeclarationAxiom(entity);
            ClassKeeper keeper  =new ClassKeeper();
            keeper.setClassName(className);
            classKeeper.put(className,keeper);

            //TempChangesKeeper changesKeeper  =new TempChangesKeeper();
            return axiomHandler.addAxiom(declare);
        }

        return false;
    }

    

    public boolean deleteClass(String owlClass) throws Exception {

        Set<OWLAxiom> toRemove = new HashSet<>();
        for (OWLAxiom select : ontology.getAxioms())
        {
            if(select.getSignature().contains(axiomHandler.getOWLEntity(owlClass,EntityType.CLASS)))
            {
                toRemove.add(select);
            }
        }
       
        axiomHandler.addToAxiomQueue(toRemove);


//        UtilMethods.removedInstances = pelletReasoner.getInstances(owlClass,true).getFlattened();
//        UtilMethods.removedAnnotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(owlClass, ontology);

        ontology.getOWLOntologyManager().removeAxioms(ontology, toRemove);
        ontology.getOWLOntologyManager().saveOntology(ontology);

        classKeeper.remove(owlClass);
        getClassTree(true);
        return axiomHandler.checkConsistency();
    }

    public boolean addClassAxiom(DataTransfer transfer, int cOrEq) throws Exception {

        OWLAxiom axiom ;
        OWLClass childC = (OWLClass) axiomHandler.getOWLEntity(transfer.getCurrentClass(),EntityType.CLASS);
        OWLClass parent=getParent(childC);

        switch (transfer.getPatternType()) {
            case "o1":
                OWLClass parentC = (OWLClass) axiomHandler.getOWLEntity(transfer.getClassList().get(0),EntityType.CLASS);

                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(parent, parentC);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, parentC);
                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, parentC);

                }

                break;
            case "o2": {
                Set<OWLClass> owlClasses = new HashSet<>();
                for (String s : transfer.getClassList()) {
                    owlClasses.add((OWLClass) axiomHandler.getOWLEntity(s,EntityType.CLASS));
                }
                OWLObjectUnionOf unionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectUnionOf(owlClasses);

                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(parent, unionOf);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, unionOf);
                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, unionOf);
                }
                break;
            }
            case "o3": {
                Set<OWLClass> owlClasses = new HashSet<>();
                for (String s : transfer.getClassList()) {

                    owlClasses.add((OWLClass) axiomHandler.getOWLEntity(s,EntityType.CLASS));
                }
                OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(owlClasses);


                if (cOrEq == 1) {
                    if (parent != null) {
                        owlClasses.add(parent);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, parent);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);

                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, intersectionOf);
                }
                System.out.println(axiom);
                break;
            }
            case "o4": {
                OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(transfer.getoProperties().get(0),EntityType.OBJECT_PROPERTY);
                OWLClass owlClass = (OWLClass) axiomHandler.getOWLEntity(transfer.getClassList().get(0),EntityType.OBJECT_PROPERTY);
                OWLObjectSomeValuesFrom someValuesFrom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectSomeValuesFrom(property, owlClass);


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(parent, someValuesFrom);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, someValuesFrom);
                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, someValuesFrom);
                }

                break;
            }
            case "o5": {
                OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(transfer.getoProperties().get(0),EntityType.OBJECT_PROPERTY);
                Set<OWLClass> owlClasses = new HashSet<>();
                for (String s : transfer.getClassList()) {
                    owlClasses.add((OWLClass) axiomHandler.getOWLEntity(s,EntityType.CLASS));
                }
                OWLObjectUnionOf unionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectUnionOf(owlClasses);

                OWLObjectAllValuesFrom allValuesFrom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectAllValuesFrom(property, unionOf);


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(parent, allValuesFrom);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, allValuesFrom);
                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, allValuesFrom);
                }
                break;
            }
            case "o6": {

                OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(transfer.getdProperties().get(0),EntityType.DATA_PROPERTY);
                OWLDatatype dataType = (OWLDatatype) axiomHandler.getOWLEntity(transfer.getdTypes().get(0),EntityType.DATATYPE);
                OWLLiteral literal = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(transfer.getLiterals().get(0), dataType);
                OWLDataHasValue hasValue = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataHasValue(property, literal);


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(parent, hasValue);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, hasValue);
                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, hasValue);
                }
                break;
            }
            default: {
                System.out.println("come");

                OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(transfer.getoProperties().get(0),EntityType.OBJECT_PROPERTY);
                OWLObjectCardinalityRestriction cardinality;
                if (transfer.getCardinalityType().equals("min")) {
                    cardinality = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectMinCardinality(transfer.getCardinality(), property);
                } else if (transfer.getCardinalityType().equals("max")) {
                    cardinality = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectMaxCardinality(transfer.getCardinality(), property);
                } else {
                    cardinality = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectExactCardinality(transfer.getCardinality(), property);
                }


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(parent, cardinality);
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEquivalentClassesAxiom(childC, cardinality);
                    }
                }else{
                    axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(childC, cardinality);
                }
                break;
            }
        }
        System.out.println(axiom);
        RestrictionKeeper keeper = new RestrictionKeeper(UtilMethods.convertAxiom(axiom),axiom);

        boolean con= axiomHandler.addAxiom(axiom);

        if(con){
            if(cOrEq==0){
                classKeeper.get(transfer.getCurrentClass()).getSubClassRestrictions().add(keeper);
                getClassTree(true);
            }else{
                classKeeper.get(transfer.getCurrentClass()).getEqClassRestrictions().add(keeper);
            }
        }

        return con;
    }


    public OWLClass getParent(OWLClass clz) {
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

    public List<String> getRange(String p) throws OWLException, JsonProcessingException {
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

    public boolean addOrRemoveDisjointClass(String clz, String dClz, int addOrRemove) throws Exception {
        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(clz,EntityType.CLASS);
        OWLClass dis = (OWLClass) axiomHandler.getOWLEntity(dClz,EntityType.CLASS);
        OWLAxiom dja = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDisjointClassesAxiom(current,dis);
        if(addOrRemove==1){
            return axiomHandler.addAxiom(dja);
        }else{
            return axiomHandler.removeAxiom(dja);
        }

    }

//1=add
    public boolean addOrRemoveDomainOf(String clz, String prop, int addOrRemove) throws Exception {

        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(clz,EntityType.CLASS);

        OWLAxiom axiom;
        if(ontology.containsObjectPropertyInSignature(axiomHandler.getIRI(prop))){
            OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
            axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property,current);
        }else{
            OWLDataProperty property = (OWLDataProperty) axiomHandler.getOWLEntity(prop,EntityType.DATA_PROPERTY);
            axiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyDomainAxiom(property,current);
        }
        if(addOrRemove==1){
            return axiomHandler.addAxiom(axiom);
        }else{
            return axiomHandler.removeAxiom(axiom);
        }


    }

    public boolean addOrRemoveRangeOf(String clz, String prop, int addOrRemove) throws Exception {

        OWLClass current = (OWLClass) axiomHandler.getOWLEntity(clz,EntityType.CLASS);
        OWLObjectProperty property = (OWLObjectProperty) axiomHandler.getOWLEntity(prop,EntityType.OBJECT_PROPERTY);
        OWLAxiom axiom= ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property,current);

        if(addOrRemove==0){
            return axiomHandler.removeAxiom(axiom);
        }else{
           return axiomHandler.addAxiom(axiom);
        }
    }

    public boolean renameIRI(String entity, String newName) throws OWLException {

        IRI entityToRename = axiomHandler.getIRI(entity);
        IRI newNameIRI= axiomHandler.getIRI(newName);
        if (!ontology.getOWLOntologyManager().contains(newNameIRI)) {
            OWLEntityRenamer owlEntityRenamer = new OWLEntityRenamer(ontology.getOWLOntologyManager(), ontology.getOWLOntologyManager().getOntologies());

            final List<OWLOntologyChange> changes = owlEntityRenamer.changeIRI(entityToRename, newNameIRI);
            for (OWLOntologyChange ch : changes) {
                System.out.println(ch.getChangeRecord());
            }
            ontology.getOWLOntologyManager().applyChanges(changes);
            for (OWLOntology on : ontology.getOWLOntologyManager().getOntologies()) {
                ontology.getOWLOntologyManager().saveOntology(on);
            }
            ClassKeeper k = classKeeper.get(entity);
            classKeeper.remove(entity);
            k.setClassName(newName);
            classKeeper.put(newName,k);
            return true;
        }
        return false;
    }

    public boolean removeSubClassOfAxiom(String className, int id) throws Exception {
        axiomHandler.removeAxiom(classKeeper.get(className).getSubClassRestrictions().get(id).getAxiomObj());
        classKeeper.get(className).getSubClassRestrictions().remove(id);
        return axiomHandler.checkConsistency();
    }

    public boolean removeEqClassOfAxiom(String className, int id) throws Exception {
        axiomHandler.removeAxiom(classKeeper.get(className).getEqClassRestrictions().get(id).getAxiomObj());
        classKeeper.get(className).getEqClassRestrictions().remove(id);
        return axiomHandler.checkConsistency();
    }
}
