package com.rmdc.ontologyEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OntologyEditorApplication {
	//private static final Logger logger = LoggerFactory.getLogger(OntologyEditorApplication.class);
	public static void main(String[] args) {

		SpringApplication.run(OntologyEditorApplication.class, args);
		//logger.debug("--Ontology Editor Started--");
	}
}
