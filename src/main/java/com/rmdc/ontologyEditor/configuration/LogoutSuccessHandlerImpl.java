package com.rmdc.ontologyEditor.configuration;

import com.rmdc.ontologyEditor.service.DBService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final DBService dbService;

    public LogoutSuccessHandlerImpl(DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        User author = (User) authentication.getPrincipal();
        dbService.updateDatabase(author.getUsername());
        response.sendRedirect("/login");
    }
}
