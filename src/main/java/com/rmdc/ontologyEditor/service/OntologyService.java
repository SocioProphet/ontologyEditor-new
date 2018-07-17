package com.rmdc.ontologyEditor.service;

import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lotus on 10/6/2017.
 */
@Service
public class OntologyService {

    private final OWLOntology ontology;
    private final AxiomHandler axiomHandler;

    public OntologyService(OWLOntology ontology, AxiomHandler axiomHandler) {
        this.ontology = ontology;
        this.axiomHandler = axiomHandler;
    }

//    public List<String> processAxioms(OWLOntology ontology){
//        List<String> strings = new ArrayList<>();
//        OWLObjectVisitor objectVisitor = new OWLObjectVisitor();
//        if(ontology!=null)
//        {
//            Set<OWLAxiom> axiomSet = ontology.getAxioms();
//            HashMap<String,Integer> axiomsMap = new HashMap<>();
//            if(axiomSet!=null && axiomSet.size()>0)
//            {
//                Iterator<OWLAxiom> axiomIterator = axiomSet.iterator();
//                OWLAxiom axiom;
//                while(axiomIterator.hasNext()){
//                    axiom = axiomIterator.next();
//                    if(axiomsMap.containsKey(axiom.getAxiomType().getName())){
//                        axiomsMap.put(axiom.getAxiomType().getName(),axiomsMap.get(axiom.getAxiomType().getName())+1);
//                    }
//                    else{
//                        axiomsMap.put(axiom.getAxiomType().getName(),1);
//                    }
//                    axiom.accept(objectVisitor);
//                }
//                strings.add("No.of Axiom Types =["+axiomsMap.size()+"]");
//                Iterator<String> stringIterator = axiomsMap.keySet().iterator();
//                String axiomType;
//                while(stringIterator.hasNext()){
//                    axiomType = stringIterator.next();
//                    strings.add(axiomType +"\t\t\t"+"["+axiomsMap.get(axiomType)+"]");
//                }
//            }
//        }
//
//        return strings;
//
//    }

    public String getOntologyName(OWLOntology owlOntology){
        String ontoName = null;
        Set<OWLAnnotation> annotations = owlOntology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().getIRI().getShortForm().equals("label")||a.getProperty().getIRI().getShortForm().equals("title")){
                ontoName=a.getValue().toString();
                break;
            }
        }
        if(ontoName.isEmpty()){
            ontoName = owlOntology.getOntologyID().getOntologyIRI().asSet().iterator().next().getShortForm();
        }
        return ontoName;
    }

    public String getOntologyVersion(OWLOntology owlOntology){
        return owlOntology.getOntologyID().getVersionIRI().asSet().iterator().next().getShortForm();
    }



    public List<String> getDescription(OWLOntology owlOntology){
        List<String> des = new ArrayList<>();
        Set<OWLAnnotation> annotations = owlOntology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().getIRI().getShortForm().equals("comment")||a.getProperty().getIRI().getShortForm().equals("description")){
                des.add(a.getValue().toString());
            }
        }
        return  des;
    }

    public List<String> getContributors(OWLOntology owlOntology){
        List<String> con = new ArrayList<>();
        Set<OWLAnnotation> annotations = owlOntology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().getIRI().getShortForm().equals("contributor")||a.getProperty().getIRI().getShortForm().equals("author")){
                con.add(a.getValue().toString());
            }
        }
        return  con;
    }

    public void addContribute(String name) throws OWLOntologyStorageException {
        OWLAnnotationProperty property = (OWLAnnotationProperty) axiomHandler.getOWLEntity("contributor",EntityType.ANNOTATION_PROPERTY);
        OWLAnnotation annotation = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation( property, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(name));
        OWLOntologyChange owlOntologyChange =new AddOntologyAnnotation(ontology,annotation);
        ontology.getOWLOntologyManager().applyChange(owlOntologyChange);
        ontology.getOWLOntologyManager().saveOntology(ontology);
    }

//    public void addVersionInfo(OntoVersion version) throws OWLOntologyStorageException {
//        OWLAnnotationProperty property = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLVersionInfo();
//
//        Set<OWLAnnotation> annotations = ontology.getAnnotations();
//        for(OWLAnnotation a:annotations){
//            if(a.getProperty().equals(property)){
//                OWLOntologyChange owlOntologyChange =new RemoveOntologyAnnotation(ontology,a);
//                ontology.getOWLOntologyManager().applyChange(owlOntologyChange);
//                break;
//            }
//        }
//        OWLAnnotation annotation = ontology.getOWLOntologyManager().getOWLDataFactory()
//                .getOWLAnnotation( property, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral("v"+version.getMainVersion()+"."+version.getSubVersion()+"."+version.getChangeVersion()));
//
//        OWLOntologyChange owlOntologyChange =new AddOntologyAnnotation(Init.getOntology(),annotation);
//        ontology.getOWLOntologyManager().applyChange(owlOntologyChange);
//        IRI versionIRI=IRI.create(Variables.dIRI+version.getMainVersion()+"."+version.getSubVersion()+"."+version.getChangeVersion());
//        SetOntologyID change=new SetOntologyID(o,
//                new OWLOntologyID(o.getOntologyID().getOntologyIRI(), Optional.of(versionIRI)));
//        o.getOWLOntologyManager().applyChange(change);
//
//        Init.getManager().saveOntology(Init.getOntology());
//    }

//    public void addPriorVersion(OntoVersion pVer) throws OWLOntologyStorageException {
//        OWLAnnotationProperty property = Init.getFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/2002/07/owl#priorVersion"));
//        OWLAnnotation annotation = Init.getFactory().getOWLAnnotation( property, Init.getFactory().getOWLLiteral(Variables.dIRI+pVer.getMainVersion()+"."+pVer.getSubVersion()+"."+pVer.getChangeVersion()));
//
//        for(OWLAnnotation a: Init.getOntology().getAnnotations()){
//            if(annotation.getAnnotationPropertiesInSignature().contains(property)){
//                OWLOntologyChange owlOntologyChange =new RemoveOntologyAnnotation(Init.getOntology(),a);
//                Init.getManager().applyChange(owlOntologyChange);
//                break;
//            }
//        }
//        OWLOntologyChange owlOntologyChange =new AddOntologyAnnotation(Init.getOntology(),annotation);
//        Init.getManager().applyChange(owlOntologyChange);
//        Init.getManager().saveOntology(Init.getOntology());
//    }

//    public void addBackwardCompatibleWith(OWLOntology ontology) throws OWLOntologyStorageException {
//        OWLAnnotationProperty property = Init.getFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/2002/07/owl#backwardCompatibleWith"));
//        OWLAnnotation annotation = Init.getFactory().getOWLAnnotation( property, ontology.getOntologyID().getVersionIRI().get());
//        OWLOntologyChange owlOntologyChange =new AddOntologyAnnotation(Init.getOntology(),annotation);
//        Init.getManager().applyChange(owlOntologyChange);
//        Init.getManager().saveOntology(Init.getOntology());
//    }
//
//    public void addincompatibleWith(OWLOntology ontology) throws OWLOntologyStorageException {
//        OWLAnnotationProperty property = Init.getFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/2002/07/owl#incompatibleWith"));
//        OWLAnnotation annotation = Init.getFactory().getOWLAnnotation( property,ontology.getOntologyID().getVersionIRI().get());
//        OWLOntologyChange owlOntologyChange =new AddOntologyAnnotation(Init.getOntology(),annotation);
//        Init.getManager().applyChange(owlOntologyChange);
//        Init.getManager().saveOntology(Init.getOntology());
//    }

    public void addAxiomAnnotation(OWLOntology ontology, OWLAxiom axiom, String annotationValue, String annotationProperty){
        // 1. Get the Ontology Manager
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        List<OWLOntologyChange> changes = new ArrayList<>();

        // 1'. Get the existing annotations
        Set<OWLAnnotation> existingAnnotations = ontology.getAnnotations();

        // 2. Create annotation
        OWLAnnotation annotation = factory.getOWLAnnotation(

                factory.getOWLAnnotationProperty(IRI.create(annotationProperty)),
                factory.getOWLLiteral(annotationValue));


        Set<OWLAnnotation> newAnnotations = new HashSet<OWLAnnotation>();
        for (OWLAnnotation anno : existingAnnotations)
            newAnnotations.add(anno);

        newAnnotations.add(annotation);

        // 3. Bind annotation to axiom
        //OWLAxiom annotatedAxiom =
        axiom.getAnnotatedAxiom(newAnnotations);
        OWLAxiom annotatedAxiom;
        if (axiom.getAxiomType() == AxiomType.DECLARATION)
            annotatedAxiom =
                    factory.getOWLAnnotationAssertionAxiom(((OWLDeclarationAxiom)axiom).getEntity().getIRI(),
                            annotation, existingAnnotations);
        else
            annotatedAxiom = axiom.getAnnotatedAxiom(newAnnotations);

        changes.add(new RemoveAxiom(ontology, axiom));
        changes.add(new AddAxiom(ontology, annotatedAxiom));
        manager.applyChanges(changes);
    }

    public void addBackwardInCompatibleWith(OWLOntology preOnto) throws OWLOntologyStorageException {
        OWLAnnotationProperty property = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/2002/07/owl#incompatibleWith"));

        OWLAnnotationProperty cProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/2002/07/owl#backwardCompatibleWith"));

        Set<OWLAnnotation> annotations = ontology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().equals(cProperty)){
                OWLOntologyChange owlOntologyChange =new RemoveOntologyAnnotation(ontology,a);
                ontology.getOWLOntologyManager().applyChange(owlOntologyChange);
                break;
            }
        }
        OWLAnnotation annotation = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation( property, preOnto.getOntologyID().getVersionIRI().get());
        OWLOntologyChange owlOntologyChange =new AddOntologyAnnotation(ontology,annotation);
        ontology.getOWLOntologyManager().applyChange(owlOntologyChange);
        ontology.getOWLOntologyManager().saveOntology(ontology);
    }
}
