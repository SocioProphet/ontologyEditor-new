package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findRoleById(int id);
}