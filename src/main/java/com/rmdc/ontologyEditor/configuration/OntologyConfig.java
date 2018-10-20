package com.rmdc.ontologyEditor.configuration;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.rmdc.ontologyEditor.model.DataTransfer;
import com.rmdc.ontologyEditor.model.OPKeeper;
import com.rmdc.ontologyEditor.model.TempChangesKeeper;
import com.rmdc.ontologyEditor.repository.OntoVersionRepository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.CachingBidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class OntologyConfig {

    private final OntoVersionRepository versionRepository;
    private final MessageSource messageSource;

    @Autowired
    public OntologyConfig(OntoVersionRepository versionRepository, MessageSource messageSource) {
        this.versionRepository = versionRepository;
        this.messageSource = messageSource;
    }

    @Bean
    public OWLOntology ontology(){
        try {
            return loadOntology(manager(),versionRepository.findOntoVersionByCurrentEquals(true).getLocation());
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    public OWLReasoner structuralReasoner(){
        return new StructuralReasonerFactory().createNonBufferingReasoner(ontology());
    }

    @Bean
    public OWLReasoner pelletReasoner(){
        return new PelletReasonerFactory().createNonBufferingReasoner(ontology());
    }


    OWLOntologyManager manager(){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        return manager;
    }

    private OWLOntology loadOntology(OWLOntologyManager manager, String fileName) throws OWLOntologyCreationException {
        return manager.loadOntologyFromOntologyDocument(new File(fileName));
    }

    @Bean
    public DataTransfer dataTransfer(){
        return new DataTransfer();
    }

    @Bean
    public OPKeeper opKeeper(){ return new OPKeeper(); }

    @Bean
    public ShortFormEntityChecker entityChecker(){
        Provider shortFormProvider = new Provider();
        ShortFormEntityChecker entityChecker = new ShortFormEntityChecker(shortFormProvider);
        ontology().getClassesInSignature().parallelStream().forEach(c->shortFormProvider.add(c));
        ontology().getObjectPropertiesInSignature().parallelStream().forEach(c->shortFormProvider.add(c));
        ontology().getDataPropertiesInSignature().parallelStream().forEach(c->shortFormProvider.add(c));

        return entityChecker;
    }


    private static class Provider extends CachingBidirectionalShortFormProvider {

        private SimpleShortFormProvider provider = new SimpleShortFormProvider();

        @Override
        protected String generateShortForm(OWLEntity entity) {
            return provider.getShortForm(entity);
        }
    }
}
