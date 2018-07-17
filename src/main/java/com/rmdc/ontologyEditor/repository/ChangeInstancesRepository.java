package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.ChangeInstances;
import com.rmdc.ontologyEditor.model.OntoChange;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ChangeInstancesRepository extends CrudRepository<ChangeInstances, Long> {
    ChangeInstances findChangeInstancesById(Integer id);
    Set<ChangeInstances> findChangeInstancesByOntoChange(OntoChange ontoChange);
    void removeByOntoChange(OntoChange ontoChange);
}
