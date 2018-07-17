package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.ChangeDes;
import com.rmdc.ontologyEditor.model.OntoChange;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ChangeDesRepository extends CrudRepository<ChangeDes, Long> {
    ChangeDes findChangeDesById(Integer id);
    Set<ChangeDes> findChangeDesByOntoChange(OntoChange ontoChange);
    void removeByOntoChange(OntoChange ontoChange);
}
