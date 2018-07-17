package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.ChangeAnnotation;
import com.rmdc.ontologyEditor.model.OntoChange;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ChangeAnnotationRepository extends CrudRepository<ChangeAnnotation, Long> {
    ChangeAnnotation findChangeAnnotationById(Integer id);
    Set<ChangeAnnotation> findChangeAnnotationByOntoChange(OntoChange ontoChange);

    void removeByOntoChange(OntoChange ontoChange);
}
