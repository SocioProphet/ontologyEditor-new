package com.rmdc.ontologyEditor.repository;

import com.rmdc.ontologyEditor.model.OntoVersion;
import com.rmdc.ontologyEditor.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface OntoVersionRepository extends CrudRepository<OntoVersion, Long> {
    OntoVersion findOntoVersionById(Integer id);
    OntoVersion findOntoVersionByCurrentEquals(boolean i);
    OntoVersion findOntoVersionByMainVersionAndSubVersionAndChangeVersion(int main, int sub, int change);
    Set<OntoVersion> findOntoVersionByUsers(Set<User> users);
    OntoVersion findOntoVersionByMainVersionAndSubVersionAndAndChangeVersion(int mainVersion,int subVersion, int changeVersion);
    Set<OntoVersion> findOntoVersionsByPrior(int id);
}
