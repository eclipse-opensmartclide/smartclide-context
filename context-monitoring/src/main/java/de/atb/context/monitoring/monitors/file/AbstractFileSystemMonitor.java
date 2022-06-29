package de.atb.context.monitoring.monitors.file;

/*-
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.monitors.ThreadedMonitor;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

public abstract class AbstractFileSystemMonitor<P> extends ThreadedMonitor<P, IMonitoringDataModel<?, ?>> {
    protected static final Long EVENT_FIRING_OFFSET = 100L;
    protected File pathToMonitor;
    protected Thread watchDaemon;
    protected Map<String, Long> filesToDates = new HashMap<>();
    protected final Logger logger = LoggerFactory
        .getLogger(FilePairSystemMonitor.class);

    public AbstractFileSystemMonitor(DataSource dataSource, Interpreter interpreter, Monitor monitor, Indexer indexer, AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);

        this.logger.info("Initializing " + this.getClass().getSimpleName()
            + " for uri: " + dataSource.getUri());

        this.pathToMonitor = new File(this.dataSource.getUri())
            .getAbsoluteFile();
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
            Thread.currentThread().setName(
                this.getClass().getSimpleName() + " ("
                    + this.dataSource.getId() + ")");
            monitor();
            startWatcher();
        } catch (Exception e) {
            this.logger.error("Error starting FileSystemMonitor! ", e);
        }
    }

    @Override
    protected boolean isIndexEnabled() {
        return true;
    }

    @Override
    public final void monitor() throws Exception {
        this.logger.info("Starting monitoring for path " + this.pathToMonitor);
        this.filesToDates.clear();
        if (this.pathToMonitor.isDirectory()) {
            this.running = true;
        }
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
        final File finalPath = this.pathToMonitor;
        Thread t = new Thread(() -> iterateFiles(finalPath), this.getClass().getSimpleName() + " Iterator Thread");
        t.setDaemon(true);
        t.start();
    }

    protected final void startWatcher() {
        prepareWatcher();
        final File pathToMonitor = this.pathToMonitor;
        this.watchDaemon = new Thread(new Runnable() {
            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void run() {
                try {
                    WatchService watchService;
                        watchService = FileSystems.getDefault().newWatchService();

                    Path watchedPath = Paths.get(pathToMonitor.getAbsolutePath());
                    this.logger.debug("Started monitoring '" + watchedPath + "'");
                    WatchKey signalledKey;

                    if (watchService != null) {
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
                                    .info("Watch service was interrupted, closing...");
                                this.logger.debug(ix.getMessage(), ix);
                                watchService.close();
                                Thread.currentThread().interrupt();
                                break;
                            } catch (ClosedWatchServiceException cwse) {
                                this.logger
                                    .info("Watch service closed, terminating...");
                                this.logger.debug(cwse.getMessage(), cwse);
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    this.logger.error(e.getMessage(), e);
                    shutdown();
                }
            }
        }, "File watch service Thread");
        this.watchDaemon.start();
    }

    protected final void handleWatchEvents(final List<WatchEvent<?>> events,
                                           final Long time) {
        String watchedPath = String.valueOf(Paths.get(this.pathToMonitor
            .getAbsolutePath()));
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
                fileDeleted(file, time);
            } else {
                this.logger.debug("Event " + e.kind() + " will be ignored");
            }
        }
    }

    protected abstract void iterateFiles(final File directory);

    protected abstract void handleFile(final String fileName, final Long time,
                                    final InterpreterConfiguration setting);

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

    protected final void fileDeleted(final String file, final Long time) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(file
                + " deleted at "
                + this.getDefaultDateFormat().format(
                new Date(time)));
        }
        this.filesToDates.remove(file);
    }

    protected final void fileModified(final String file, final Long time,
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
                this.watchDaemon.join(unit.toMillis(timeOut));
                stopWatcher();
            } catch (InterruptedException e) {
                this.logger.warn(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
    protected final DateFormat getDefaultDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
}
