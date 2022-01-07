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


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Vocabulary class to provide access to the Manchester OWL API representatives for
 * various entities in the ontology used to generate this code.
 *
 * Source Class: ${javaClass}
 *
 * @version generated on Mon Oct 05 21:07:01 BST 2015 by Giovanni
 */

public class Vocabulary {

	private static final OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

	private Vocabulary() {}

    /* ***************************************************
     * Class http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#DeployerComponent
     */

    /**
     * A constant to give access to the Manchester OWL api representation of the class DEPLOYER_ONTOLOGYWRAPPER.
     *
     */
    public static final OWLClass CLASS_DEPLOYER_ONTOLOGYWRAPPER = factory.getOWLClass(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#DeployerComponent"));

    /* ***************************************************
     * Class http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#ProSEcoSWService
     */

    /**
     * A constant to give access to the Manchester OWL api representation of the class SW_SERVICE_ONTOLOGYWRAPPER.
     *
     */
    public static final OWLClass CLASS_SW_SERVICE_ONTOLOGYWRAPPER = factory.getOWLClass(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#ProSEcoSWService"));

    /* ***************************************************
     * Class http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#SWService_Configuration
     */

    /**
     * A constant to give access to the Manchester OWL api representation of the class SW_SERVICE_CONFIGURATION_ONTOLOGYWRAPPER.
     *
     */
    public static final OWLClass CLASS_SW_SERVICE_CONFIGURATION_ONTOLOGYWRAPPER = factory.getOWLClass(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#SWService_Configuration"));

    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasConfiguration_SWService
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the object property HASCONFIGURATION_SWSERVICE.
     *
     */
    public static final OWLObjectProperty OBJECT_PROPERTY_HASCONFIGURATION_SWSERVICE = factory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasConfiguration_SWService"));

    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasConfigurations
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the object property HASCONFIGURATIONS.
     *
     */
    public static final OWLObjectProperty OBJECT_PROPERTY_HASCONFIGURATIONS = factory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasConfigurations"));

    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasDeployer_SWServices
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the object property HASDEPLOYER_SWSERVICES.
     *
     */
    public static final OWLObjectProperty OBJECT_PROPERTY_HASDEPLOYER_SWSERVICES = factory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasDeployer_SWServices"));

    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasSWService_Configuration
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the object property HASSWSERVICE_CONFIGURATION.
     *
     */
    public static final OWLObjectProperty OBJECT_PROPERTY_HASSWSERVICE_CONFIGURATION = factory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasSWService_Configuration"));

    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasSWService_Deployer
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the object property HASSWSERVICE_DEPLOYER.
     *
     */
    public static final OWLObjectProperty OBJECT_PROPERTY_HASSWSERVICE_DEPLOYER = factory.getOWLObjectProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasSWService_Deployer"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#IdName
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property IDNAME.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_IDNAME = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#IdName"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#configParameters
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property CONFIGPARAMETERS.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_CONFIGPARAMETERS = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#configParameters"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#host
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property HOST.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_HOST = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#host"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#location
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property LOCATION.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_LOCATION = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#location"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#name
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property NAME.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_NAME = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#name"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#proxy
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property PROXY.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_PROXY = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#proxy"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#server
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property SERVER.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_SERVER = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#server"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#status
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property STATUS.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_STATUS = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#status"));

    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#type
     */

    /**
     * A constant to give access to the Manchester OWL API representation of the data property TYPE.
     *
     */
    public static final OWLDataProperty DATA_PROPERTY_TYPE = factory.getOWLDataProperty(IRI.create("http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#type"));


}
