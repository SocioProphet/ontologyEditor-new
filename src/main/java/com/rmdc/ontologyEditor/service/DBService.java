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

    public static List<TempChangesKeeper> getChanges() {
        return DBService.changes;
    }

    public static void setChanges(List<TempChangesKeeper> changes) {
        DBService.changes = changes;
    }

    public void updateDatabase(String author){
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

            for(OWLAxiom axiom:change.getAxioms()){
                ChangeDes changeDes = new ChangeDes();
                changeDes.setOntoChange(ontoChange);
                changeDes.setDescription(axiom.toString());
                changeDes.setObject(UtilMethods.toByts(axiom));
                changeDesRepository.save(changeDes);
            }
            for(OWLNamedIndividual i: change.getIndividuals()){
                ChangeInstances instances= new ChangeInstances();
                instances.setOntoChange(ontoChange);
                instances.setObject(UtilMethods.toByts(i));
                instances.setDescription(i.getIRI().getShortForm());
                changeInstancesRepository.save(instances);
            }

            for (OWLAnnotation annotation : change.getAnnotations()) {
                ChangeAnnotation changeAnnotation = new ChangeAnnotation();
                changeAnnotation.setOntoChange(ontoChange);
                changeAnnotation.setObject(UtilMethods.toByts(annotation));
                changeAnnotation.setAnnKey(annotation.getProperty().toString());
                changeAnnotation.setAnnValue(annotation.getValue().asLiteral().asSet().iterator().next().getLiteral());
                changeAnnotationRepository.save(changeAnnotation);
            }
        }
    }
}
