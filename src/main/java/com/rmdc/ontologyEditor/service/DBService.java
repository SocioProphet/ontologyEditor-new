package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.UtilMethods;
import com.rmdc.ontologyEditor.model.*;
import com.rmdc.ontologyEditor.repository.*;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DBService {
    private static List<TempChangesKeeper> changes;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OntoChangeRepository ontoChangeRepository;
    private final OntoVersionRepository ontoVersionRepository;
    private final ChangeTypeRepository changeTypeRepository;
    private final ChangeOnRepository changeOnRepository;
    private final ChangeInstancesRepository changeInstancesRepository;
    private final ChangeDesRepository changeDesRepository;
    private final ChangeAnnotationRepository changeAnnotationRepository;

    @Autowired
    public DBService(UserRepository userRepository,
                     RoleRepository roleRepository,
                     OntoChangeRepository ontoChangeRepository,
                     OntoVersionRepository ontoVersionRepository,
                     ChangeTypeRepository changeTypeRepository,
                     ChangeOnRepository changeOnRepository,
                     ChangeInstancesRepository changeInstancesRepository,
                     ChangeDesRepository changeDesRepository,
                     ChangeAnnotationRepository changeAnnotationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.ontoChangeRepository = ontoChangeRepository;
        this.ontoVersionRepository = ontoVersionRepository;
        this.changeTypeRepository = changeTypeRepository;
        this.changeOnRepository = changeOnRepository;
        this.changeInstancesRepository = changeInstancesRepository;
        this.changeDesRepository = changeDesRepository;
        this.changeAnnotationRepository = changeAnnotationRepository;
    }

    public static void updateDBQueue( List<OWLAxiom> axioms, List<OWLNamedIndividual> individuals,
                               List<OWLAnnotation> annotations,
                               EChangeType eChangeType, String conceptName,
                               EConceptType eConceptType, String description){

        if(changes==null){
            changes = new ArrayList<>();
        }

        TempChangesKeeper changesKeeper = new TempChangesKeeper(
                axioms,individuals, annotations, new Date(),
                eChangeType, conceptName, eConceptType, description );
        changes.add(changesKeeper);
    }

    public void updateDatabase(String author){
        if(changes==null) return;
        User u = userRepository.findUserByName(author);
        for (TempChangesKeeper change:changes) {
            ChangeType type = changeTypeRepository.findChangeTypeById(change.geteChangeType().getChangeType());
            ChangeOn changeOn = changeOnRepository.findChangeOnById(change.geteConceptType().getConceptType());
            OntoVersion version = ontoVersionRepository.findOntoVersionById(2);//TODO:Versioning

            OntoChange ontoChange = new OntoChange();
            ontoChange.setUser(u);
            ontoChange.setChangeType(type);
            ontoChange.setChangeOn(changeOn);
            ontoChange.setOntoVersion(version);
            ontoChange.setDescription(change.getDescription());
            ontoChange.setConcept(change.getConceptName());
           // ontoChange.setChangeAxiom(UtilMethods.convertAxiom(axiom)); //no need
            ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));

            ontoChangeRepository.save(ontoChange);

            if(change.getAxioms()!=null)
            for(OWLAxiom axiom:change.getAxioms()){
                ChangeDes changeDes = new ChangeDes();
                changeDes.setOntoChange(ontoChange);
                changeDes.setDescription(axiom.toString());
                changeDes.setObject(UtilMethods.toBits(axiom));
                changeDesRepository.save(changeDes);
            }

            if(change.getIndividuals()!=null)
            for(OWLNamedIndividual i: change.getIndividuals()){
                ChangeInstances instances= new ChangeInstances();
                instances.setOntoChange(ontoChange);
                instances.setObject(UtilMethods.toBits(i));
                instances.setDescription(i.getIRI().getShortForm());
                changeInstancesRepository.save(instances);
            }
            if(change.getAnnotations()!=null)
            for (OWLAnnotation annotation : change.getAnnotations()) {
                ChangeAnnotation changeAnnotation = new ChangeAnnotation();
                changeAnnotation.setOntoChange(ontoChange);
                changeAnnotation.setObject(UtilMethods.toBits(annotation));
                changeAnnotation.setAnnKey(annotation.getProperty().toString());
                changeAnnotation.setAnnValue(annotation.getValue().asLiteral().asSet().iterator().next().getLiteral());
                changeAnnotationRepository.save(changeAnnotation);
            }
        }
    }

    public Iterable<OntoVersion> getVersions(){
        return ontoVersionRepository.findAll();
    }

    public Iterable<OntoChange> getChanges(){
        return ontoChangeRepository.findAll();
    }

    public OntoChange getOntoChange(int id){
        return ontoChangeRepository.findOntoChangeById(id);
    }

    public Iterable<ChangeDes> getDesForChange(OntoChange change){
        return changeDesRepository.findChangeDesByOntoChange(change);
    }

    public Iterable<ChangeInstances> getInstancesForChange(OntoChange change){
        return changeInstancesRepository.findChangeInstancesByOntoChange(change);
    }

    public Iterable<ChangeAnnotation> changeAnnotationRepository(OntoChange change){
        return changeAnnotationRepository.findChangeAnnotationByOntoChange(change);
    }
}
