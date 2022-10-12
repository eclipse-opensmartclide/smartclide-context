package org.eclipse.opensmartclide.context.persistence.monitoring;

/*
 * #%L
 * ATB Context Monitoring Core Services
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


import java.util.List;

import org.apache.jena.query.ResultSet;

import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.persistence.common.IPersistenceUnit;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;

/**
 * IMonitoringDataRepository
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public interface IMonitoringDataRepository<Type extends IMonitoringDataModel<?, ?>> extends IPersistenceUnit<Type> {

    List<Type> getMonitoringData(BusinessCase businessCase, Class<Type> clazz, int count) throws ContextFault;

    List<Type> getMonitoringData(BusinessCase businessCase, Class<Type> clazz, TimeFrame timeFrame) throws ContextFault;

    Type getMonitoringData(BusinessCase businessCase, Class<Type> clazz, String identifier) throws ContextFault;

    List<Type> getMonitoringData(ApplicationScenario applicationScenario, Class<Type> clazz, int count) throws ContextFault;

    List<Type> getMonitoringData(ApplicationScenario applicationScenario, Class<Type> clazz, TimeFrame timeFrame)
        throws ContextFault;

    Type getMonitoringData(ApplicationScenario applicationScenario, Class<Type> clazz, String identifier) throws ContextFault;

    List<String> getLastIds(BusinessCase businessCase, Class<Type> clazz, int count) throws ContextFault;

    List<String> getLastIds(BusinessCase businessCase, Class<Type> clazz, TimeFrame timeFrame) throws ContextFault;

    List<String> getLastIds(ApplicationScenario appScenario, Class<Type> clazz, int count) throws ContextFault;

    List<String> getLastIds(ApplicationScenario appScenario, Class<Type> clazz, TimeFrame timeFrame) throws ContextFault;

    ResultSet executeSparqlSelectQuery(BusinessCase businessCase, String query) throws ContextFault;

    void executeSparqlUpdateQuery(BusinessCase businessCase, String query) throws ContextFault;

    void executeSparqlUpdateQueries(BusinessCase businessCase, String... queries) throws ContextFault;

    boolean reset(BusinessCase bc) throws ContextFault;

    void shutdown();
}
