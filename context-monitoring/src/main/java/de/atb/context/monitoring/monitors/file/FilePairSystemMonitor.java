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
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;

import de.atb.context.common.io.FileUtils;
import de.atb.context.monitoring.analyser.IndexingAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.FilePairSystemDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.parser.IndexingParser;
import de.atb.context.monitoring.parser.file.FilePairParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.javatuples.Pair;

/**
 * FilePairSystemMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class FilePairSystemMonitor extends AbstractFileSystemMonitor<Pair<File, File>> {

    protected Pair<File, File> filePair;

    public FilePairSystemMonitor(final DataSource dataSource,
                                 final Interpreter interpreter,
                                 final Monitor monitor,
                                 final Indexer indexer,
                                 final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (dataSource.getType().equals(DataSourceType.FilePairSystem)) {
            this.dataSource = dataSource;
        } else {
            throw new IllegalArgumentException(
                "Given dataSource must be of type FilePairSystemDataSource!");
        }
    }

    protected final void iterateFiles(final File directory) {
        this.logger.debug("Iterating " + directory);
        if ((directory == null) || (directory.listFiles() == null)) {
            return;
        }
        FilePairSystemDataSource dataSource = this.dataSource
            .convertTo(FilePairSystemDataSource.class);
        FilenameFilter filter = dataSource.getFilenameFilter();
        File[] files = new File(dataSource.getUri()).getAbsoluteFile()
            .listFiles(filter);
        List<Pair<File, File>> filePairs = FilePairParser.getPairsFromFiles(
            files, dataSource.getFirstExtension(),
            dataSource.getSecondExtension());

        for (Pair<File, File> f : filePairs) {
            if (!this.running) {
                return;
            }
            if ((f == null) || (f.getValue0() == null)
                || (f.getValue1() == null)) {
                continue;
            }
            try {
                if (f.getValue0().isFile() && f.getValue1().isFile()
                    && this.interpreter.accepts(f)) {
                    Long time = new Date().getTime();
                    fileExisting(f.getValue0().getAbsolutePath(), time,
                                 this.interpreter.getConfiguration(f.getValue0()));
                    fileExisting(f.getValue1().getAbsolutePath(), time,
                                 this.interpreter.getConfiguration(f.getValue1()));
                } else if (f.getValue0().isFile() && f.getValue1().isFile()
                           && !this.interpreter.accepts(f)) {
                    this.logger.trace("Skipping files "
                                      + f.getValue0().getAbsolutePath() + " and "
                                      + f.getValue1().getAbsolutePath()
                                      + ", interpreter does not accept it");
                }
            } catch (Throwable t) {
                this.logger.error("Error parsing file "
                                  + f.getValue0().getAbsolutePath() + " and "
                                  + f.getValue1().getAbsolutePath(), t);
            }
        }

    }

    @SuppressWarnings("unchecked")
    protected final void handleFile(final String fileName, final Long time,
                                    final InterpreterConfiguration setting) {
        if (setting != null) {
            File file = new File(fileName);
            this.logger.debug("Handling file " + fileName + "...");
            updateFilePair(file);
            if ((this.filePair != null) && (this.filePair.getValue0() != null)
                && (this.filePair.getValue1() != null)) {
                IndexingParser<Pair<File, File>> parser = getParser(setting);
                IndexingAnalyser<IMonitoringDataModel<?, ?>, Pair<File, File>> analyser = (IndexingAnalyser<IMonitoringDataModel<?, ?>, Pair<File, File>>) parser
                    .getAnalyser();

                parseAndAnalyse(this.filePair, parser, analyser);

                this.filePair = Pair.with(null, null);
            }
        } else {
            this.logger.debug("File " + fileName + " will be ignored!");
        }
    }

    protected final void updateFilePair(final File file) {
        if (this.filePair == null) {
            this.filePair = Pair.with(null, null);
        }
        FilePairSystemDataSource dataSource = this.dataSource
            .convertTo(FilePairSystemDataSource.class);
        if ((this.filePair.getValue0() == null)
            && dataSource.getFirstExtension().equals(
            FileUtils.getExtension(file))) {
            this.filePair = this.filePair.setAt0(file);
        }
        if ((this.filePair.getValue1() == null)
            && dataSource.getSecondExtension().equals(
            FileUtils.getExtension(file))) {
            this.filePair = this.filePair.setAt1(file);
        }
    }
}
