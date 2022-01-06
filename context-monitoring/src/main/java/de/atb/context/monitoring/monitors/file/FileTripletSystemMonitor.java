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
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import de.atb.context.monitoring.monitors.ThreadedMonitor;
import de.atb.context.monitoring.parser.IndexingParser;
import de.atb.context.monitoring.parser.file.FileTripletParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileTripletSystemMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class FileTripletSystemMonitor extends
    ThreadedMonitor<Triplet<File, File, File>, IMonitoringDataModel<?, ?>>
    implements Runnable {

    protected File pathToMonitor;
    protected Thread watchDaemon;

    protected Map<String, Long> filesToDates = new HashMap<>();
    protected Triplet<File, File, File> filePair;

    private static final Long EVENT_FIRING_OFFSET = 100L;

    private final Logger logger = LoggerFactory
        .getLogger(FileTripletSystemMonitor.class);

    public FileTripletSystemMonitor(final DataSource dataSource,
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

        this.logger.info("Initializing " + this.getClass().getSimpleName()
                         + " for uri: " + dataSource.getUri());
        this.pathToMonitor = new File(this.dataSource.getUri());
    }

    /**
     * (non-Javadoc)
     *
     * @see ThreadedMonitor#isRunning()
     */
    @Override
    public final boolean isRunning() {
        return this.running;
    }

    @Override
    public final void pause() {
        this.running = false;
        stopWatcher();
    }

    @Override
    public final void restart() {
        this.running = true;
        stopWatcher();
        this.filesToDates.clear();
        startWatcher();
    }

    @Override
    public final void run() {
        try {
            this.running = true;
            Thread.currentThread().setName(
                this.getClass().getSimpleName() + " ("
                + this.dataSource.getId() + ")");
            this.logger.info("Really starting "
                             + this.getClass().getSimpleName() + " ");
            monitor();
            startWatcher();
        } catch (Exception e) {
            this.logger.error("Error starting FileSystemMonitor! ", e);
        }
    }

    @Override
    public final void monitor() throws Exception {
        this.logger.info("Starting monitoring for path " + this.pathToMonitor);
        this.filesToDates.clear();
    }

    protected final void stopWatcher() {
        if (this.watchDaemon != null) {
            this.watchDaemon.interrupt();
            try {
                this.watchDaemon.join(2000);
            } catch (InterruptedException e) {
                this.logger.warn(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    protected final void prepareWatcher() {
        iterateFiles(this.pathToMonitor);
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

    protected final void startWatcher() {
        prepareWatcher();
        final File pathToMonitor = this.pathToMonitor;
        this.watchDaemon = new Thread(new Runnable() {
            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void run() {
                WatchService watchService = null;
                try {
                    watchService = FileSystems.getDefault().newWatchService();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Path watchedPath = Paths.get(pathToMonitor.getAbsolutePath());
                this.logger.debug("Started monitoring '" + watchedPath + "'");
                WatchKey signalledKey;
                try {
                    watchedPath.register(watchService,
                                         StandardWatchEventKinds.ENTRY_CREATE,
                                         StandardWatchEventKinds.ENTRY_MODIFY,
                                         StandardWatchEventKinds.ENTRY_DELETE,
                                         StandardWatchEventKinds.OVERFLOW);
                    while (true) {
                        try {
                            signalledKey = watchService.take();
                            Long time = System.currentTimeMillis();
                            handleWatchEvents(signalledKey.pollEvents(), time);
                            signalledKey.reset();
                        } catch (InterruptedException ix) {
                            this.logger
                                .warn("Watch service was interrupted, closing...");
                            // this.logger.debug(ix.getMessage(), ix);
                            watchService.close();
                            Thread.currentThread().interrupt();
                            break;
                        } catch (ClosedWatchServiceException cwse) {
                            this.logger
                                .warn("Watch service closed, terminating...");
                            this.logger.info(cwse.getMessage(), cwse);
                            break;
                        }
                    }
                } catch (IOException e) {
                    this.logger.error(e.getMessage(), e);
                }
            }
        }, "File watch service Thread");
        this.watchDaemon.start();
    }

    protected final void handleWatchEvents(final List<WatchEvent<?>> events,
                                           final Long time) {
        String watchedPath = String.valueOf(Paths.get(this.pathToMonitor
                                                          .getAbsolutePath()));
        String from = null;
        for (WatchEvent<?> e : events) {
            Path context = (Path) e.context();
            String file = watchedPath + java.io.File.separator
                          + context.toString();
            InterpreterConfiguration setting = this.interpreter
                .getConfiguration(file);
            if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                fileCreated(file, time, setting);
            } else if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                fileModified(file, time, setting);
            } else if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                fileDeleted(file, time, setting);
            } else {
                this.logger.debug("Event " + e.kind() + " will be ignored");
            }
        }
    }

    protected final void fileExisting(final String file, final Long time,
                                      final InterpreterConfiguration setting) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(file
                              + " already existed at "
                              + this.getDefaultDateFormat().format(
                new Date(time)));
        }
        if (setting != null) {
            this.filesToDates.put(file, time);
        }
        handleFile(file, time, setting);
    }

    protected final void fileCreated(final String file, final Long time,
                                     final InterpreterConfiguration setting) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(file
                              + " created at "
                              + this.getDefaultDateFormat().format(
                new Date(time)));
        }
        if (setting != null) {
            this.filesToDates.put(file, time);
        }
        handleFile(file, time, setting);
    }

    protected final void fileDeleted(final String file, final Long time,
                                     final InterpreterConfiguration setting) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(file
                              + " deleted at "
                              + this.getDefaultDateFormat().format(
                new Date(time)));
        }
        this.filesToDates.remove(file);
    }

    protected final boolean fileModified(final String file, final Long time,
                                         final InterpreterConfiguration setting) {
        boolean modified = false;
        Long oldTime = this.filesToDates.get(file);
        if (oldTime == null || oldTime + EVENT_FIRING_OFFSET < time) {
            modified = this.filesToDates.put(file, time) != null;
        }

        if (this.logger.isTraceEnabled() && modified) {
            this.logger.trace(file
                              + " modified at "
                              + this.getDefaultDateFormat().format(
                new Date(time)));
        }
        if (modified) {
            handleFile(file, time, setting);
        }
        return modified;
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
                IndexingParser<Triplet<File, File, File>> parser = getParser(setting);
                IndexingAnalyser<IMonitoringDataModel<?, ?>, Triplet<File, File, File>> analyser = (IndexingAnalyser<IMonitoringDataModel<?, ?>, Triplet<File, File, File>>) parser
                    .getAnalyser();

                parseAndAnalyse(this.filePair, parser, analyser);

                this.filePair = Triplet.with(null, null, null);
            }
        } else {
            this.logger.debug("File " + fileName + " will be ignored!");
        }
    }

    @Override
    protected IndexingParser<Triplet<File, File, File>> getParser(final InterpreterConfiguration setting) {
        return setting.createParser(this.dataSource, this.indexer, this.amiConfiguration);
    }

    protected final Triplet<File, File, File> updateFilePair(final File file) {
        if (this.filePair == null) {
            this.filePair = Triplet.with(null, null, null);
        }
        FileTripletSystemDataSource dataSource = this.dataSource
            .convertTo(FileTripletSystemDataSource.class);
        if ((this.filePair.getValue0() == null)
            && dataSource.getFirstExtension().equals(
            FileUtils.getExtension(file))) {
            this.filePair.setAt0(file);
        }
        if ((this.filePair.getValue1() == null)
            && dataSource.getSecondExtension().equals(
            FileUtils.getExtension(file))) {
            this.filePair.setAt1(file);
        }
        if ((this.filePair.getValue2() == null)
            && dataSource.getThirdExtension().equals(
            FileUtils.getExtension(file))) {
            this.filePair.setAt2(file);
        }
        return this.filePair;
    }

    protected final DateFormat getDefaultDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * (non-Javadoc)
     *
     * @see ThreadedMonitor#shutdown()
     */
    @Override
    public final void shutdown() {
        this.running = false;
        stopWatcher();
        this.filesToDates.clear();
    }

    /**
     * (non-Javadoc)
     *
     * @see ThreadedMonitor#shutdown(long, TimeUnit)
     */
    @Override
    protected final void shutdown(final long timeOut, final TimeUnit unit) {
        if (this.isRunning() && (this.watchDaemon != null)) {
            try {
                stopWatcher();
                this.watchDaemon.join(unit.toMillis(timeOut));
            } catch (InterruptedException e) {
                this.logger.warn(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
