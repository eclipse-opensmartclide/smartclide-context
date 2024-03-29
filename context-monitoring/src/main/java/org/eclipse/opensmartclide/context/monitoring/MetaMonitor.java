package org.eclipse.opensmartclide.context.monitoring;

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


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.eclipse.opensmartclide.context.common.exceptions.ConfigurationException;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.monitors.ThreadedMonitor;
import org.eclipse.opensmartclide.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.slf4j.LoggerFactory;

/**
 * MetaMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class MetaMonitor {

    @SuppressWarnings("unchecked")
    public static <P, A extends IMonitoringDataModel<?, ?>> ThreadedMonitor<P, A> createThreadedMonitor(
        final Monitor monitor,
        final DataSource dataSource,
        final Interpreter interpreter,
        final Indexer indexer,
        final AmIMonitoringConfiguration configuration,
        final AmIMonitoringDataRepositoryServiceWrapper amiRepository) throws ConfigurationException {
        Class<?> factory;
        try {
            factory = Class.forName(dataSource.getMonitor());
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(MetaMonitor.class).error(e.getMessage(), e);
            throw new ConfigurationException("DAO-class [%s] not found!", dataSource.getMonitor());
        }
        int modifier = factory.getModifiers();

        if (!Modifier.isAbstract(modifier) && !Modifier.isInterface(modifier) && !Modifier.isStatic(modifier)) {
            try {
                Constructor<?> constructor = factory.getConstructor(DataSource.class,
                                                                    Interpreter.class,
                                                                    Monitor.class,
                                                                    Indexer.class,
                                                                    AmIMonitoringConfiguration.class);
                ThreadedMonitor<P, A> instance = (ThreadedMonitor<P, A>) constructor.newInstance(new Object[]{
                    dataSource,
                    interpreter,
                    monitor,
                    indexer,
                    configuration
                });
                instance.setAmiRepository(amiRepository);
                return instance;
            } catch (InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
                throw new ConfigurationException("Error while instantiating class [%s].", e, dataSource.getMonitor());
            }
        } else {
            throw new ConfigurationException("Can't instantiate DAO-class [%s]!", dataSource.getMonitor());
        }
    }

}
