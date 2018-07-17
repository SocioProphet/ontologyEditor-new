package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.ChangeType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeTypeRepository extends CrudRepository<ChangeType, Long> {
    ChangeType findChangeTypeById(Integer id);
}