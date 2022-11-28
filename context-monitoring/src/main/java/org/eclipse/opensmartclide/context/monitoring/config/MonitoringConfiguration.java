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
import org.eclipse.opensmartclide.context.common.exceptions.ConfigurationException;
import org.eclipse.opensmartclide.context.monitoring.config.models.*;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        final URI uri;
        try {
            uri = Objects.requireNonNull(MonitoringConfiguration.class.getResource("/")).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final String DEFAULT_FILE_PATH = Path.of(uri).toAbsolutePath().toString();
        if (SETTINGS.get(DEFAULT_FILE_NAME) == null) {
                SETTINGS.put(DEFAULT_FILE_NAME, new MonitoringConfiguration(DEFAULT_FILE_NAME, DEFAULT_FILE_PATH));
        }
        return SETTINGS.get(DEFAULT_FILE_NAME);
    }

    public static MonitoringConfiguration getInstance(final AmIMonitoringConfiguration config) {
        if (SETTINGS.get(config) == null) {
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
        InputStream is = null;
        try {
            final String drmHandle = sysCaller.openDRMobject(configurationFileName, configurationLookupPath, "read");
            logger.debug("drm handle value: {}", drmHandle);
            if (drmHandle != null) {
                final byte[] readConfig = sysCaller.getDRMobject(configurationFileName, configurationLookupPath);
                if (readConfig != null) {
                    is = new ByteArrayInputStream(readConfig);
                    this.configurationBean = new Persister().read(this.configurationClass, is);
                    is.close();
                    logger.info("{} loaded!", configurationFileName);
                }
                sysCaller.closeDRMobject(drmHandle);
            } else {
                throw new NullPointerException("Read config file fails due to null DRM object.");
            }
        } catch (final Exception e) {
            logger.error("Could not serialize the {} file {}", configurationName, configurationFileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
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
