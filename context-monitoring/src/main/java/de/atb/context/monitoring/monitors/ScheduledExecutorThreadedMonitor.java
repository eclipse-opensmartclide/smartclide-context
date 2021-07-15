package de.atb.context.monitoring.monitors;

/*-
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.events.MonitoringProgressListener;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class ScheduledExecutorThreadedMonitor<P, A extends IMonitoringDataModel<?, ?>> extends ThreadedMonitor<P, A> {

    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    protected ScheduledExecutorThreadedMonitor(final DataSource dataSource,
                                               final Interpreter interpreter,
                                               final Monitor monitor,
                                               final Indexer indexer,
                                               final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
    }

    /*
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
        this.executor.shutdown();
    }

    @Override
    public final void restart() {
        shutdown();
        run();
    }

    @Override
    public final void shutdown() {
        shutdown(2, TimeUnit.SECONDS);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ThreadedMonitor#shutdown(long)
     */
    @Override
    protected final void shutdown(final long timeOut, final TimeUnit unit) {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(timeOut, unit)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } finally {
            this.running = false;
        }
    }

    @Override
    public final void run() {
        try {
            Thread.currentThread().setName(this.getClass().getSimpleName() + " (" + this.dataSource.getId() + ")");
            addProgressListener((MonitoringProgressListener) this);
            this.running = true;
            scheduleExecution();
        } catch (Exception e) {
            this.logger.error("Error starting DatabaseMonitor! ", e);
        }
    }

    public void monitor() throws Exception {
        this.logger.info("Starting monitoring for {} at URI: {}", this.getClass().getSimpleName(), this.dataSource.getUri());
        this.running = true;
        final InterpreterConfiguration setting = getInterpreterConfiguration();
        doMonitor(setting);
    }

    protected abstract long getScheduleInitialDelay();

    protected abstract InterpreterConfiguration getInterpreterConfiguration();

    protected abstract void doMonitor(final InterpreterConfiguration setting) throws Exception;

    protected void scheduleExecution() {
        long initialDelay = getScheduleInitialDelay();
        this.executor.schedule(new MonitoringRunner(this), initialDelay, TimeUnit.MILLISECONDS);
    }

    protected static final class MonitoringRunner implements Runnable {

        private static final Logger logger = LoggerFactory.getLogger(MonitoringRunner.class);

        private final ScheduledExecutorThreadedMonitor<?, ?> parent;

        public MonitoringRunner(final ScheduledExecutorThreadedMonitor<?, ?> parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            try {
                this.parent.monitor();
            } catch (Exception e) {
                logger.error("Error during monitoring of {}! ", parent.getClass().getSimpleName(), e);
            }
        }

    }
}
