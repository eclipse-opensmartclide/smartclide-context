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

import java.util.concurrent.TimeUnit;

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

public abstract class PeriodicScheduledExecutorThreadedMonitor<P, A extends IMonitoringDataModel<?, ?>>
    extends ScheduledExecutorThreadedMonitor<P, A> {

    protected PeriodicScheduledExecutorThreadedMonitor(final DataSource dataSource,
                                                       final Interpreter interpreter,
                                                       final Monitor monitor,
                                                       final Indexer indexer,
                                                       final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
    }

    protected abstract long getSchedulePeriod();

    @Override
    protected void scheduleExecution() {
        long period = getSchedulePeriod();
        long initialDelay = getScheduleInitialDelay();
        this.executor.scheduleAtFixedRate(new MonitoringRunner(this), initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
