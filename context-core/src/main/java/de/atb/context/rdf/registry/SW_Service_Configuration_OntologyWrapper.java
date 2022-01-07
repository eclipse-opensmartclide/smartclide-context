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
 *
 * Source Class: SW_Service_Configuration_OntologyWrapper <br>
 * @version generated on Mon Oct 05 21:07:01 BST 2015 by Giovanni
 */
public interface SW_Service_Configuration_OntologyWrapper {

    /**
     * Gets all property values for the hasConfiguration_SWService property.<p>
     *
     * @return a collection of values for the hasConfiguration_SWService property.
     */
    Collection<? extends SW_Service_OntologyWrapper> getHasConfigurationSWService();

    /**
     * Adds a hasConfiguration_SWService property value.<p>
     *
     * @param newHasConfigurationSWService the hasConfiguration_SWService property value to be added
     */
    void addHasConfigurationSWService(SW_Service_OntologyWrapper newHasConfigurationSWService);

    /**
     * Gets all property values for the IdName property.<p>
     *
     * @return a collection of values for the IdName property.
     */
    Collection<? extends String> getIdName();

    /**
     * Adds a IdName property value.<p>
     *
     * @param newIdName the IdName property value to be added
     */
    void addIdName(String newIdName);

    /**
     * Gets all property values for the host property.<p>
     *
     * @return a collection of values for the host property.
     */
    Collection<? extends String> getHost();

    /**
     * Adds a host property value.<p>
     *
     * @param newHost the host property value to be added
     */
    void addHost(String newHost);

    /**
     * Gets all property values for the location property.<p>
     *
     * @return a collection of values for the location property.
     */
    Collection<? extends String> getLocation();

    /**
     * Adds a location property value.<p>
     *
     * @param newLocation the location property value to be added
     */
    void addLocation(String newLocation);

    /**
     * Gets all property values for the name property.<p>
     *
     * @return a collection of values for the name property.
     */
    Collection<? extends String> getName();

    /**
     * Removes a name property value.<p>
     *
     * @param oldName the name property value to be removed.
     */
    void removeName(String oldName);

    /**
     * Gets all property values for the proxy property.<p>
     *
     * @return a collection of values for the proxy property.
     */
    Collection<? extends String> getProxy();

    /**
     * Adds a proxy property value.<p>
     *
     * @param newProxy the proxy property value to be added
     */
    void addProxy(String newProxy);

    /**
     * Gets all property values for the server property.<p>
     *
     * @return a collection of values for the server property.
     */
    Collection<? extends String> getServer();

    /**
     * Checks if the class has a server property value.<p>
     *
     * @return true if there is a server property value.
     */
    boolean hasServer();

    /**
     * Adds a server property value.<p>
     *
     * @param newServer the server property value to be added
     */
    void addServer(String newServer);

    /**
     * Gets all property values for the type property.<p>
     *
     * @return a collection of values for the type property.
     */
    Collection<? extends String> getType();

    /**
     * Adds a type property value.<p>
     *
     * @param newType the type property value to be added
     */
    void addType(String newType);

    OWLNamedIndividual getOwlIndividual();

    void delete();
}
