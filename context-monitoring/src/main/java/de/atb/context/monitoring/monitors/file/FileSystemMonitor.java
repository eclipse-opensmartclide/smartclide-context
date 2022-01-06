package de.atb.context.monitoring.monitors.file;

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

import java.io.File;
import java.util.Date;

import de.atb.context.monitoring.analyser.IndexingAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.parser.IndexingParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

/**
 * FileSystemMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class FileSystemMonitor extends AbstractFileSystemMonitor<File> {

    public FileSystemMonitor(final DataSource dataSource,
                             final Interpreter interpreter,
                             final Monitor monitor,
                             final Indexer indexer,
                             final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (dataSource.getType().equals(DataSourceType.FileSystem)
            && (dataSource instanceof FileSystemDataSource)) {
            this.dataSource = dataSource;
        } else {
            throw new IllegalArgumentException(
                "Given dataSource must be of type FileSystemDataSource!");
        }

        this.logger.info("Initializing " + this.getClass().getSimpleName()
                         + " for uri: " + dataSource.getUri());
    }

    protected final void iterateFiles(final File directory) {
        logger.debug("Iterating files in directory '" + directory + "'");
        if ((directory == null) || (directory.listFiles() == null)) {
            return;
        }
        File[] files = directory
            .listFiles(this.interpreter.getFilenameFilter());
        for (File f : files) {
            if (!this.running) {
                return;
            }

            if (f == null) {
                continue;
            }
            try {
                if (f.isFile() && this.interpreter.accepts(f)) {
                    fileExisting(f.getAbsolutePath(),
                        new Date().getTime(),
                        this.interpreter.getConfiguration(f));
                } else if (f.isFile() && !this.interpreter.accepts(f)) {
                    this.logger.debug("Skipping file " + f.getAbsolutePath()
                        + ", interpreter does not accept it");
                } else if (f.isDirectory()) {
                    iterateFiles(f);
                }
            } catch (Throwable t) {
                this.logger.error("Error parsing file " + f.getAbsolutePath(),
                    t);
            }
        }
    }

    protected final void handleFile(final String fileName, final Long time,
                                    final InterpreterConfiguration setting) {
        if (setting != null) {
            File file = new File(fileName);
            this.logger.debug("Handling file " + fileName + "...");
            IndexingParser<File> parser = getParser(setting);
            IndexingAnalyser<IMonitoringDataModel<?, ?>, File> analyser = (IndexingAnalyser<IMonitoringDataModel<?, ?>, File>) parser.getAnalyser();

            parseAndAnalyse(file, parser, analyser);
        } else {
            this.logger.debug("File " + fileName + " will be ignored!");
        }
    }

    @Override
    protected IndexingParser<File> getParser(final InterpreterConfiguration setting) {
        return setting.createFileParser(this.dataSource, this.indexer, this.amiConfiguration);
    }
}
