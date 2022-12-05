package org.eclipse.opensmartclide.context.monitoring.config;

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


import org.eclipse.opensmartclide.context.common.Configuration;
import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.common.exceptions.ConfigurationException;
import org.eclipse.opensmartclide.context.monitoring.config.models.*;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Settings
 *
 * @author scholze
 * @version $LastChangedRevision: 250 $
 */
public final class MonitoringConfiguration extends Configuration<Config> implements IMonitoringConfiguration {

    private static final Map<String, MonitoringConfiguration> SETTINGS = new HashMap<>();
    private static final String DEFAULT_FILE_NAME = "monitoring-config.xml";

    public static MonitoringConfiguration getInstance() {
        final Path DEFAULT_FILE_PATH = ContextPathUtils.getConfigDirPath();
        if (SETTINGS.get(DEFAULT_FILE_NAME) == null) {
            SETTINGS.put(DEFAULT_FILE_NAME, new MonitoringConfiguration(DEFAULT_FILE_NAME, DEFAULT_FILE_PATH.toString()));
        }
        return SETTINGS.get(DEFAULT_FILE_NAME);
    }

    public static MonitoringConfiguration getInstance(final AmIMonitoringConfiguration config) {
        if (SETTINGS.get(config.getId()) == null) {
            SETTINGS.put(config.getId(), new MonitoringConfiguration(config));
        }
        return SETTINGS.get(config.getId());
    }

    public static MonitoringConfiguration getInstance(final String configFileName, final String configFilePath) {
        if (SETTINGS.get(configFileName) == null) {
            SETTINGS.put(configFileName, new MonitoringConfiguration(configFileName, configFilePath));
        }
        return SETTINGS.get(configFileName);
    }

    private MonitoringConfiguration(final String givenName, final String givenPath) {
        super(givenName, givenPath, Config.class, "Monitoring Configuration");
    }

    private MonitoringConfiguration(final AmIMonitoringConfiguration config) {
        super(config, Config.class, "Monitoring Configuration");
    }

    protected void readConfigurationFile() {
        final String drmHandle = sysCaller.openDRMobject(configurationFileName, configurationLookupPath, "read");
        if (drmHandle == null) {
            logger.error("config file with name: {} at given location: {} does not exist", configurationFileName, configurationLookupPath);
            throw new RuntimeException("Read config file fails due to null DRM object.");
        }
        final byte[] readConfig = sysCaller.getDRMobject(configurationFileName, configurationLookupPath);
        if (readConfig == null) {
            logger.error("Reading config file with name: {} at given location: {} fails", configurationFileName, configurationLookupPath);
            throw new RuntimeException("Read config file fails due to null readConfig object.");
        }
        try (InputStream is = new ByteArrayInputStream(readConfig)) {
            this.configurationBean = new Persister().read(this.configurationClass, is);
            logger.info("{} loaded!", configurationFileName);
        } catch (Exception e) {
            throw new RuntimeException("Runtime exception while reading byte data from input stream", e);
        }
        sysCaller.closeDRMobject(drmHandle);
    }

    @Override
    public List<Index> getIndexes() {
        return this.configurationBean.getIndexes();
    }

    @Override
    public Index getIndex(final String id) {
        return this.configurationBean.getIndex(id);
    }

    @Override
    public List<Monitor> getMonitors() {
        return this.configurationBean.getMonitors();
    }

    @Override
    public Monitor getMonitor(final String id) {
        return this.configurationBean.getMonitor(id);
    }

    @Override
    public List<DataSource> getDataSources() {
        return this.configurationBean.getDataSources();
    }

    @Override
    public DataSource getDataSource(final String id) {
        return this.configurationBean.getDataSource(id);
    }

    @Override
    public List<Interpreter> getInterpreters() {
        return this.configurationBean.getInterpreters();
    }

    @Override
    public Interpreter getInterpreter(final String id) {
        return this.configurationBean.getInterpreter(id);
    }

    @Override
    public void checkConsistency() throws ConfigurationException {
        for (Monitor monitor : getMonitors()) {
            if (monitor.getDataSourceId() == null) {
                throw new ConfigurationException("DataSource for Monitor '%s' is null", monitor.getId());
            }
            if (monitor.getInterpreterId() == null) {
                throw new ConfigurationException("Interpreter for Monitor '%s' is null", monitor.getId());
            }
            if (monitor.getIndexId() == null) {
                throw new ConfigurationException("Index for Monitor '%s' is null", monitor.getId());
            }

            if (getDataSource(monitor.getDataSourceId()) == null) {
                throw new ConfigurationException(
                    "DataSource '%s' for Monitor '%s' is not configured",
                    monitor.getDataSourceId(),
                    monitor.getId()
                );
            }
            if (getInterpreter(monitor.getInterpreterId()) == null) {
                throw new ConfigurationException(
                    "Interpreter '%s' for Monitor '%s' is not configured",
                    monitor.getInterpreterId(),
                    monitor.getId()
                );
            }
            if (getIndex(monitor.getIndexId()) == null) {
                throw new ConfigurationException(
                    "Index '%s' for Monitor '%s' is not configured",
                    monitor.getIndexId(),
                    monitor.getId()
                );
            }
        }
    }

}
