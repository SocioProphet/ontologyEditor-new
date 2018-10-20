package com.rmdc.ontologyEditor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ForTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder  = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("password"));


    }


}