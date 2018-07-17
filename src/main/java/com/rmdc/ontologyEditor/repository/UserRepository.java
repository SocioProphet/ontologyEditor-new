package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findUserById(Integer id);
    User findUserByName(String name);
}
