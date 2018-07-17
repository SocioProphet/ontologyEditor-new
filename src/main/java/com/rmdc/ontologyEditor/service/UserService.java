package com.rmdc.ontologyEditor.service;

import com.rmdc.ontologyEditor.model.User;

public interface UserService {

    public User findUserByName(String name);
    public void saveUser(User user);

}
