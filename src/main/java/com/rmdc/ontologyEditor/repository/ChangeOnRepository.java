package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.ChangeOn;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeOnRepository extends CrudRepository<ChangeOn, Long> {
    ChangeOn findChangeOnById(Integer id);
}