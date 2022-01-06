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
import de.atb.context.monitoring.config.models.datasources.FileTripletSystemDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.parser.IndexingParser;
import de.atb.context.monitoring.parser.file.FileTripletParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.javatuples.Triplet;

/**
 * FileTripletSystemMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class FileTripletSystemMonitor extends AbstractFileSystemMonitor<Triplet<File, File, File>> {

    protected Triplet<File, File, File> fileTriplet;

    public FileTripletSystemMonitor(final DataSource dataSource,
                                    final Interpreter interpreter,
                                    final Monitor monitor,
                                    final Indexer indexer,
                                    final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (dataSource.getType().equals(DataSourceType.FileTripletSystem)) {
            this.dataSource = dataSource;
        } else {
            throw new IllegalArgumentException(
                "Given dataSource must be of type FileTripletSystemDataSource!");
        }
    }

    protected final void iterateFiles(final File directory) {
        this.logger.debug("Iterating " + directory);
        if ((directory == null) || (directory.listFiles() == null)) {
            return;
        }
        FileTripletSystemDataSource dataSource = this.dataSource
            .convertTo(FileTripletSystemDataSource.class);
        FilenameFilter filter = dataSource.getFilenameFilter();
        File[] files = new File(dataSource.getUri()).getAbsoluteFile()
            .listFiles(filter);
        List<Triplet<File, File, File>> fileTriplets = FileTripletParser
            .getTripletsFromFiles(files, dataSource.getFirstExtension(),
                                  dataSource.getSecondExtension(),
                                  dataSource.getThirdExtension());

        for (Triplet<File, File, File> f : fileTriplets) {
            if (!this.running) {
                return;
            }
            if ((f == null)
                || ((f.getValue0() == null) && (f.getValue1() == null) && (f
                                                                               .getValue2() == null))) {
                continue;
            }
            try {
                if (this.interpreter.accepts(f)) {
                    Long time = new Date().getTime();
                    if (f.getValue0() != null) {
                        fileExisting(
                            f.getValue0().getAbsolutePath(),
                            time,
                            this.interpreter.getConfiguration(f.getValue0()));
                    }
                    if (f.getValue1() != null) {
                        fileExisting(
                            f.getValue1().getAbsolutePath(),
                            time,
                            this.interpreter.getConfiguration(f.getValue1()));
                    }
                    if (f.getValue2() != null) {
                        fileExisting(
                            f.getValue2().getAbsolutePath(),
                            time,
                            this.interpreter.getConfiguration(f.getValue2()));
                    }
                } else {
                    this.logger.trace("Skipping files " + f.getValue0()
                                      + " and " + f.getValue1() + " and " + f.getValue2()
                                      + ", interpreter does not accept it");
                }
            } catch (Throwable t) {
                this.logger.error("Error parsing file " + f.getValue0()
                                  + " and " + f.getValue1() + " and " + f.getValue2(), t);
            }
        }

    }

    protected final void handleFile(final String fileName, final Long time,
                                    final InterpreterConfiguration setting) {
        if (setting != null) {
            File file = new File(fileName);
            this.logger.debug("Handling file " + fileName + "...");
            updateFileTriplet(file);
            if ((this.fileTriplet != null) && (this.fileTriplet.getValue0() != null)
                && (this.fileTriplet.getValue1() != null)) {
                IndexingParser<Triplet<File, File, File>> parser = getParser(setting);
                IndexingAnalyser<IMonitoringDataModel<?, ?>, Triplet<File, File, File>> analyser = (IndexingAnalyser<IMonitoringDataModel<?, ?>, Triplet<File, File, File>>) parser
                    .getAnalyser();

                parseAndAnalyse(this.fileTriplet, parser, analyser);

                this.fileTriplet = Triplet.with(null, null, null);
            }
        } else {
            this.logger.debug("File " + fileName + " will be ignored!");
        }
    }

    protected final Triplet<File, File, File> updateFileTriplet(final File file) {
        if (this.fileTriplet == null) {
            this.fileTriplet = Triplet.with(null, null, null);
        }
        FileTripletSystemDataSource dataSource = this.dataSource
            .convertTo(FileTripletSystemDataSource.class);
        if ((this.fileTriplet.getValue0() == null)
            && dataSource.getFirstExtension().equals(
            FileUtils.getExtension(file))) {
            this.fileTriplet.setAt0(file);
        }
        if ((this.fileTriplet.getValue1() == null)
            && dataSource.getSecondExtension().equals(
            FileUtils.getExtension(file))) {
            this.fileTriplet.setAt1(file);
        }
        if ((this.fileTriplet.getValue2() == null)
            && dataSource.getThirdExtension().equals(
            FileUtils.getExtension(file))) {
            this.fileTriplet.setAt2(file);
        }
        return this.fileTriplet;
    }
}
