package org.eclipse.opensmartclide.context.monitoring.config.models;

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
import java.lang.reflect.Modifier;

import org.eclipse.opensmartclide.context.monitoring.analyser.database.DatabaseAnalyser;
import org.eclipse.opensmartclide.context.monitoring.analyser.file.FileAnalyser;
import org.eclipse.opensmartclide.context.monitoring.analyser.webservice.WebServiceAnalyser;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexingParser;
import org.apache.lucene.document.Document;
import org.eclipse.opensmartclide.context.monitoring.analyser.IndexingAnalyser;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.simpleframework.xml.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileSystem
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class InterpreterConfiguration {

    private final Logger logger = LoggerFactory.getLogger(InterpreterConfiguration.class);

    /**
     * Represents the extension of the FileSystem.
     */
    @Attribute(name = "type")
    protected String extension;

    /**
     * Represents the Class name of the Parser to be used for parsing the
     * FileSystem.
     */
    @Attribute
    protected String parser;

    /**
     * Represents the Class name of the Analyser to be used for parsing the
     * FileSystem.
     */
    @Attribute
    protected String analyser;

    /**
     * Default Constructor. Creates a InterpreterConfiguration that is not bound
     * to any extension, analyser and parser.
     */
    public InterpreterConfiguration() {
    }

    /**
     * Creates a InterpreterConfiguration that is bound to the given extension.
     *
     * @param extension the extension of the file.
     */
    public InterpreterConfiguration(final String extension) {
        this.extension = extension;
    }

    /**
     * Gets the extension of configurations this InterpreterConfiguration
     * applies to.
     *
     * @return the extension of configurations this InterpreterConfiguration
     * applies to.
     */
    public final String getExtension() {
        return this.extension;
    }

    /**
     * Sets the extension of configurations this InterpreterConfiguration
     * applies to.
     *
     * @param extension the extension of configurations this InterpreterConfiguration
     *                  applies to.
     */
    public final void setExtension(final String extension) {
        this.extension = extension;
    }

    /**
     * Gets the Class name of the Parser to be used for parsing configurations
     * that match this InterpreterConfiguration.
     *
     * @return the Class name of the Parser to be used for parsing
     * configurations that match this InterpreterConfiguration.
     */
    public final String getParser() {
        return this.parser;
    }

    /**
     * Sets the Class name of the Parser to be used for parsing configurations
     * that match this InterpreterConfiguration.
     *
     * @param parser the Class name of the Parser to be used for parsing
     *               configurations that match this InterpreterConfiguration.
     */
    public final void setParser(final String parser) {
        this.parser = parser;
    }

    /**
     * Gets the Class name of the Analyser to be used for analysing
     * configurations that match this InterpreterConfiguration.
     *
     * @return the Class name of the Analyser to be used for analysing
     * configurations that match this InterpreterConfiguration.
     */
    public final String getAnalyser() {
        return this.analyser;
    }

    /**
     * Sets the Class name of the Analyser to be used for analysing
     * configurations that match this InterpreterConfiguration.
     *
     * @param analyser the Class name of the Analyser to be used for analysing
     *                 configurations that match this InterpreterConfiguration.
     */
    public final void setAnalyser(final String analyser) {
        this.analyser = analyser;
    }

    public final FileAnalyser<IMonitoringDataModel<?, ?>> createFileAnalyser(
        final DataSource ds, final Indexer indexer,
        final Document document, final AmIMonitoringConfiguration amiConfiguration) {
        return this.createAnalyser(ds, indexer, document, amiConfiguration);
    }

    public final WebServiceAnalyser<IMonitoringDataModel<?, ?>> createWebServiceAnalyser(
        final DataSource ds, final Indexer indexer,
        final Document document, final AmIMonitoringConfiguration amiConfiguration) {
        return this.createAnalyser(ds, indexer, document, amiConfiguration);
    }

    public final DatabaseAnalyser<IMonitoringDataModel<?, ?>> createDatabaseAnalyser(
        final DataSource ds, final Indexer indexer,
        final Document document, final AmIMonitoringConfiguration amiConfiguration) {
        return this.createAnalyser(ds, indexer, document, amiConfiguration);
    }

    @SuppressWarnings("unchecked")
    public final <T extends IndexingAnalyser<IMonitoringDataModel<?, ?>, ?>> T createAnalyser(
        final DataSource ds, final Indexer indexer,
        final Document document, final AmIMonitoringConfiguration amiConfiguration) {
        if ((this.analyser == null) || (this.analyser.trim().length() == 0)) {
            return null;
        }
        try {
            Class<?> factory = Class.forName(this.analyser);
            int modifier = factory.getModifiers();

            if (!Modifier.isAbstract(modifier)
                && !Modifier.isInterface(modifier)
                && !Modifier.isStatic(modifier)) {
                Constructor<?> constructor = factory
                    .getConstructor(DataSource.class,
                        InterpreterConfiguration.class, Indexer.class,
                        Document.class, AmIMonitoringConfiguration.class);
                return (T) constructor.newInstance(new Object[]{ds, this,
                    indexer, document, amiConfiguration});
            }
        } catch (Throwable e) {
            logger.error("Could not create an instance for " + this.analyser, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public final <T extends IndexingParser<?>> T createParser(
        final DataSource ds, final Indexer indexer,
        final AmIMonitoringConfiguration amiCOnfiguration) {
        if ((this.parser == null) || (this.parser.trim().length() == 0)) {
            return null;
        }
        try {
            Class<?> factory = Class.forName(this.parser);
            int modifier = factory.getModifiers();

            if (!Modifier.isAbstract(modifier)
                && !Modifier.isInterface(modifier)
                && !Modifier.isStatic(modifier)) {
                Constructor<?> constructor = factory
                    .getConstructor(DataSource.class,
                        InterpreterConfiguration.class, Indexer.class,
                        AmIMonitoringConfiguration.class);
                return (T) constructor.newInstance(new Object[]{ds, this,
                    indexer, amiCOnfiguration});
            }
        } catch (Throwable e) {
            logger.error("Could not create an instance for " + this.parser, e);
        }
        return null;
    }

    @Override
    public final String toString() {
        return String.format("%s = %s & %s", this.extension, this.parser,
            this.analyser);
    }
}
