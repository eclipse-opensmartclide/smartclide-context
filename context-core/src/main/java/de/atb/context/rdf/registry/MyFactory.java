package de.atb.context.rdf.registry;

/*
 * #%L
 * ATB Context Extraction Core Lib
 * %%
 * Copyright (C) 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.util.Collection;

/**
 * A class that serves as the entry point to the generated code providing access
 * to existing individuals in the ontology and the ability to create new individuals in the ontology.<p>
 *
 * Source Class: MyFactory<br>
 * @version generated on Mon Oct 05 21:07:01 BST 2015 by Giovanni
 */
public class MyFactory {
    private OWLOntology ontology;

    public MyFactory(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public void saveOwlOntology() throws OWLOntologyStorageException {
        ontology.getOWLOntologyManager().saveOntology(ontology);
    }

    /**
     * Creates an instance of type Deployer_OntologyWrapper.  Modifies the underlying ontology.
     * @param name the name of the OWL named individual to be retrieved.
     * @return Deployer_OntologyWrapper
     */
    public Deployer_OntologyWrapper createDeployerOntologyWrapper(String name) {
		return null/*delegate.createWrappedIndividual(name, Vocabulary.CLASS_DEPLOYER_ONTOLOGYWRAPPER, DefaultDeployer_OntologyWrapper.class)*/;
    }

    /**
     * Gets an instance of type Deployer_OntologyWrapper with the given name.  Does not modify the underlying ontology.
     * @param name the name of the OWL named individual to be retrieved.
     * @return Deployer_OntologyWrapper
     */
    public Deployer_OntologyWrapper getDeployerOntologyWrapper(String name) {
		return null/*delegate.getWrappedIndividual(name, Vocabulary.CLASS_DEPLOYER_ONTOLOGYWRAPPER, DefaultDeployer_OntologyWrapper.class)*/;
    }

    /**
     * Gets all instances of Deployer_OntologyWrapper from the ontology.
     * @return Deployer_OntologyWrapper
     */
    public Collection<? extends Deployer_OntologyWrapper> getAllDeployerOntologyWrapperInstances() {
		return null/*delegate.getWrappedIndividuals(Vocabulary.CLASS_DEPLOYER_ONTOLOGYWRAPPER, DefaultDeployer_OntologyWrapper.class)*/;
    }

    /**
     * Gets an instance of type SW_Service_OntologyWrapper with the given name.  Does not modify the underlying ontology.
     * @param name the name of the OWL named individual to be retrieved.
     * @return SW_Service_Configuration_OntologyWrapper
     */
    public SW_Service_OntologyWrapper getSWServiceOntologyWrapper(String name) {
		return null/*delegate.getWrappedIndividual(name, Vocabulary.CLASS_SW_SERVICE_ONTOLOGYWRAPPER, DefaultSW_Service_OntologyWrapper.class)*/;
    }

    /**
     * Creates an instance of type SW_Service_Configuration_OntologyWrapper.  Modifies the underlying ontology.
     * @param name the name of the OWL named individual to be retrieved.
     * @return SW_Service_Configuration_OntologyWrapper
     */
    public SW_Service_Configuration_OntologyWrapper createSWServiceConfigurationOntologyWrapper(String name) {
		return null/*delegate.createWrappedIndividual(name, Vocabulary.CLASS_SW_SERVICE_CONFIGURATION_ONTOLOGYWRAPPER, DefaultSW_Service_Configuration_OntologyWrapper.class)*/;
    }

    /**
     * Gets an instance of type SW_Service_Configuration_OntologyWrapper with the given name.  Does not modify the underlying ontology.
     * @param name the name of the OWL named individual to be retrieved.
     * @return SW_Service_Configuration_OntologyWrapper
     */
    public SW_Service_Configuration_OntologyWrapper getSWServiceConfigurationOntologyWrapper(String name) {
		return null/*delegate.getWrappedIndividual(name, Vocabulary.CLASS_SW_SERVICE_CONFIGURATION_ONTOLOGYWRAPPER, DefaultSW_Service_Configuration_OntologyWrapper.class)*/;
    }

    /**
     * Gets all instances of SW_Service_Configuration_OntologyWrapper from the ontology.
     * @return SW_Service_Configuration_OntologyWrapper
     */
    public Collection<? extends SW_Service_Configuration_OntologyWrapper> getAllSWServiceConfigurationOntologyWrapperInstances() {
		return null/*delegate.getWrappedIndividuals(Vocabulary.CLASS_SW_SERVICE_CONFIGURATION_ONTOLOGYWRAPPER, DefaultSW_Service_Configuration_OntologyWrapper.class)*/;
    }
}
