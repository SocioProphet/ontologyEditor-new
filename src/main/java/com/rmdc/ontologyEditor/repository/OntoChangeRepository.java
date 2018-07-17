package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.OntoChange;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface OntoChangeRepository extends CrudRepository<OntoChange, Long> {
    OntoChange findOntoChangeById(Integer id);
    Set<OntoChange> findAll();
    OntoChange findTop1ByOrderByIdDesc();
    void removeById(Integer id);
}