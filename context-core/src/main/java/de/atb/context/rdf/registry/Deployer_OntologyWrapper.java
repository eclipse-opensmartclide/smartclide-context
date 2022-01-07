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


import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.Collection;

/**
 * @version generated on Mon Oct 05 21:07:01 BST 2015 by Giovanni
 */

public interface Deployer_OntologyWrapper extends SW_Service_OntologyWrapper {

    /**
     * Gets all property values for the hasDeployer_SWServices property.<p>
     *
     * @return a collection of values for the hasDeployer_SWServices property.
     */
    Collection<? extends SW_Service_OntologyWrapper> getHasDeployerSWServices();

    /**
     * Gets all property values for the hasSWService_Configuration property.<p>
     *
     * @return a collection of values for the hasSWService_Configuration property.
     */
    Collection<? extends SW_Service_Configuration_OntologyWrapper> getHasSWServiceConfiguration();

    /**
     * Adds a hasSWService_Configuration property value.<p>
     *
     * @param newHasSWServiceConfiguration the hasSWService_Configuration property value to be added
     */
    void addHasSWServiceConfiguration(SW_Service_Configuration_OntologyWrapper newHasSWServiceConfiguration);

    /**
     * Gets all property values for the hasSWService_Deployer property.<p>
     *
     * @return a collection of values for the hasSWService_Deployer property.
     */
    Collection<? extends Deployer_OntologyWrapper> getHasSWServiceDeployer();

    /**
     * Gets all property values for the IdName property.<p>
     *
     * @return a collection of values for the IdName property.
     */
    Collection<? extends String> getIdName();

    /**
     * Gets all property values for the status property.<p>
     *
     * @return a collection of values for the status property.
     */
    Collection<? extends String> getStatus();

    /**
     * Adds a status property value.<p>
     *
     * @param newStatus the status property value to be added
     */
    void addStatus(String newStatus);

    /**
     * Removes a status property value.<p>
     *
     * @param oldStatus the status property value to be removed.
     */
    void removeStatus(String oldStatus);

    /* ***************************************************
     * Common interfaces
     */

    OWLNamedIndividual getOwlIndividual();

    void delete();

}
