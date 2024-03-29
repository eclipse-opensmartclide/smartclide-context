package org.eclipse.opensmartclide.context.services;

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

import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.monitoring.MetaMonitor;
import org.eclipse.opensmartclide.context.monitoring.config.MonitoringConfiguration;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.Index;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.monitors.ThreadedMonitor;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.eclipse.opensmartclide.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.eclipse.opensmartclide.context.common.exceptions.ConfigurationException;
import org.eclipse.opensmartclide.context.infrastructure.ServiceInfo;
import org.eclipse.opensmartclide.context.services.interfaces.DataOutput;
import org.eclipse.opensmartclide.context.services.interfaces.Output;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.tools.ontology.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 144 $
 */
public final class AmIMonitoringService extends DeployableService implements IAmIMonitoringService {

    private static final Logger logger = LoggerFactory
        .getLogger(AmIMonitoringService.class);
    protected IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> service;

    @Override
    public boolean setNotifierClient(String host, int port, String className) throws ContextFault {
        return false;
    }

    protected AmIMonitoringDataRepositoryServiceWrapper repos;
    //protected ServiceInfo repositoryConfigData = new ServiceInfo();
    protected MonitoringConfiguration config;
    protected AmIMonitoringConfiguration amiConfiguration;
    protected List<ThreadedMonitor<?, ?>> monitors;
    private boolean isRunning;

    public AmIMonitoringService() {
        // this.config = MonitoringConfiguration.getInstance();
        initializeServices();
    }

    @Override
    public void start() {
        logger.info(String.format("Starting %s ...", this.getClass()
            .getSimpleName()));
        try {
            if (this.isRunning) {
                return;
            }

            logger.info("Starting Monitors...");
            this.monitors = new ArrayList<>(this.config
                .getMonitors().size());
            for (Monitor monitor : this.config.getMonitors()) {
                DataSource ds = this.config.getDataSource(monitor
                    .getDataSourceId());
                Interpreter interpreter = this.config.getInterpreter(monitor
                    .getInterpreterId());
                Index index = this.config.getIndex(monitor.getIndexId());
                ThreadedMonitor<?, ?> tmonitor;
                try {
                    tmonitor = MetaMonitor.createThreadedMonitor(monitor, ds,
                        interpreter, new Indexer(index),
                        this.amiConfiguration, repos);
                    this.monitors.add(tmonitor);
                } catch (ConfigurationException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            for (ThreadedMonitor<?, ?> monitor : this.monitors) {
                logger.debug(String.format("Starting monitor %s ...",
                    monitor.getClass()));
                monitor.start();
            }
            this.isRunning = true;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            //throw new ContextFault(t.getMessage(), t);
            //CHANGE FOR SVN
        }
    }

    @Override
    public void stop() {
        logger.info(String.format("Stopping %s ...", this.getClass()
            .getSimpleName()));
        if (!this.isRunning) {
            return;
        }
        for (ThreadedMonitor<?, ?> monitor : this.monitors) {
            monitor.stop();
        }
    }

    @Override
    public void restart() {
        logger.info(String.format("Restarting %s ...", this.getClass()
            .getSimpleName()));
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String ping() {
        logger.info(String.format("%s was pinged", this.getClass()
            .getSimpleName()));
        return ServiceManager.PING_RESPONSE;
    }

    @Override
    public boolean runtimeInvoke(String s) throws ContextFault {
        // TODO what to do with "Input"
        Output output = new Output();
        DataOutput dataOutput = new DataOutput();
        dataOutput.setReposData(repositoryConfigData);
        dataOutput
            .setResultId("use serviceInfo to access the results from AmIMonitoringRepository");
        output.setDataOutput(dataOutput);
        return true;
    }

    protected synchronized void initializeServices() {
        // initialize application scenarios
        ApplicationScenario.getInstance();
        try {
            SWServiceContainer reposContainer = null;
            if ((service == null) || !ServiceManager.isPingable(service)) {
                for (SWServiceContainer container : ServiceManager.getLSWServiceContainer()) {
                    if (container.getServerClass().toString()
                        .contains("AmIMonitoringDataRepository")) {
                        service = ServiceManager.getWebservice(container);
                        reposContainer = container;
                    }
                }
            }
            if ((service != null) && ServiceManager.isPingable(service)
                && reposContainer != null) {
                repos = new AmIMonitoringDataRepositoryServiceWrapper(service);
                this.repositoryConfigData.setName(reposContainer.getName());
                this.repositoryConfigData.setHost(reposContainer.getHost());
                this.repositoryConfigData.setLocation(reposContainer
                    .getLocation().toString());
                this.repositoryConfigData.setProxy(reposContainer
                    .getProxyClass().getName());
                this.repositoryConfigData.setServer(reposContainer
                    .getServerClass().getName());
                this.repositoryConfigData.setId(reposContainer.getId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * if the configuration shall be read from an xml file the param has to be
     * null
     */
    @Override
    public boolean configureService(final Configuration configuration) {
        if (configuration != null) {
            this.amiConfiguration = (AmIMonitoringConfiguration) configuration;
            this.config = MonitoringConfiguration
                .getInstance(this.amiConfiguration);
        } else {
            // if no Configuration is given try to read from file
            this.config = MonitoringConfiguration.getInstance();
        }
        return this.config != null;
        //start();
    }

    @Override
    public ServiceInfo getReposData() {
        return this.repositoryConfigData;
    }
}
