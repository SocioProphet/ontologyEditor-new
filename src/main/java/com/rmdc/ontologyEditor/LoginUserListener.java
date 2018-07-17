package com.rmdc.ontologyEditor;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class LoginUserListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final ObjectFactory<HttpSession> httpSessionFactory;

    @Autowired
    public LoginUserListener(ObjectFactory<HttpSession> httpSessionFactory) {
        this.httpSessionFactory = httpSessionFactory;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        HttpSession session = httpSessionFactory.getObject();
        session.setAttribute("currentDP","topDataProperty");
        session.setAttribute("currentOP","topObjectProperty");
        session.setAttribute("currentClass","Thing");

    }
}