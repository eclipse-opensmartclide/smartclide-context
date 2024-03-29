package org.eclipse.opensmartclide.context.common.configuration;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.exceptions.NotImplementedException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.opensmartclide.pm2.common.application.SysCaller;
import org.eclipse.opensmartclide.pm2.common.application.SysCallerImpl;

/**
 * ApplicationScenarioConfiguration
 *
 * @author scholze
 * @version $LastChangedRevision: 683 $
 */
public abstract class ApplicationScenarioConfiguration<B extends IConfigurationBean>
        implements IConfigurationContainer<B> {

    public static final String CONFIGURATION_FILE = " configuration file: ";
    private final Logger logger = LoggerFactory
            .getLogger(ApplicationScenarioConfiguration.class);

    private final SysCaller sysCaller = new SysCallerImpl(false);

    protected ApplicationScenario scenario;
    protected B configurationB;

    protected Class<? extends B> configurationClass;
    protected String configurationCompleteFilePath;
    protected String configurationFileName;

    @SuppressWarnings("unchecked")
    public ApplicationScenarioConfiguration(final ApplicationScenario scenario,
                                            final String className) throws ClassNotFoundException {
        this(scenario, (Class<? extends B>) Class.forName(className));
    }

    public ApplicationScenarioConfiguration(final ApplicationScenario scenario,
                                            final Class<? extends B> clazz) {
        if (scenario == null) {
            throw new NullPointerException("Scenario may not be null!");
        }
        this.scenario = scenario;
        this.configurationClass = clazz;
        this.configurationFileName = scenario.toString() + ".xml";
        if (clazz != null) {
            try {
                configurationB = clazz.newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("Configuration bean cannot be instanciated, because bean class is null!");
        }
    }

    public final ApplicationScenario getApplicationScenario() {
        return scenario;
    }

    public final String serialize() {
        final Serializer serializer = new Persister();
        final StringWriter writer = new StringWriter();
        try {
            serializer.write(configurationB, writer);
            return writer.toString();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T extends ApplicationScenarioConfiguration<?>> T deserialize(
            final String serializedConfiguration, final Class<T> clazz) {
        final Serializer serializer = new Persister();
        final StringReader reader = new StringReader(serializedConfiguration);
        try {
            return serializer.read(clazz, reader);
        } catch (final Exception e) {
            LoggerFactory.getLogger(ApplicationScenarioConfiguration.class)
                    .error(e.getMessage(), e);
        }
        return null;
    }

    public final void load() {
        load(null);
    }

    public final void load(final String path) {
        logger.info("Loading {}.xml from path '{}'", scenario.toString(), path);

        String drmHandle = sysCaller.openDRMobject("appscenario-config.xml", configurationCompleteFilePath, "read");
        if (drmHandle != null) {
            byte[] readConfig = sysCaller.getDRMobject("appscenario-config.xml", configurationCompleteFilePath);
            if (readConfig != null) {
                try (
                    InputStream is = new ByteArrayInputStream(readConfig);
                    ) {
                    Serializer serializer = new Persister();
                    this.configurationB = serializer.read(
                            this.configurationClass, is);
                    is.close();
                    logger.info("{} loaded from '{}'", scenario.toString(), path);
                } catch (final FileNotFoundException fnfe) {
                    logger.error("Could not open the " + scenario.toString()
                                    + CONFIGURATION_FILE + this.configurationFileName,
                            fnfe);
                } catch (final Exception e) {
                    logger.error("Could not deserialize the " + scenario.toString()
                            + CONFIGURATION_FILE + this.configurationFileName, e);
                }
            }
            sysCaller.closeDRMobject(drmHandle);
        }
        configurationCompleteFilePath = path;
    }

    public final void save() {
        save(null);
    }

    public final void save(final String path) {
        logger.info("Saving configuration...");
        final Serializer serializer = new Persister();

        ByteArrayOutputStream source = new ByteArrayOutputStream();
        try {
            serializer.write(getConfig(), source);

            String drmHandle = sysCaller.openDRMobject("appscen-config.xml", configurationCompleteFilePath, "write");
            sysCaller.writeDRMobject(drmHandle, source.toByteArray());
            sysCaller.closeDRMobject(drmHandle);

            logger.info("{} configuration saved to '{}'", scenario.toString(), path);
        } catch (final Exception e) {
            logger.error("Could not save the " + scenario.toString()
                    + CONFIGURATION_FILE + this.configurationFileName, e);
        }
    }

    public final void delete() {
        throw new NotImplementedException("deleting of config files is not supported!");
    }

    @Override
    public final void reset() {
        this.configurationB.reset();
    }

    public final String getConfigurationFileName() {
        return this.configurationFileName;
    }

    protected final B getConfig() {
        return this.configurationB;
    }

}
