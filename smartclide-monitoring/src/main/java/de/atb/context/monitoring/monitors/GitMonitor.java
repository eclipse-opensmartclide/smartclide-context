package de.atb.context.monitoring.monitors;

/*
 * #%L
 * SmartCLIDE Monitoring
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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.monitors.messagebroker.MessageBrokerMonitor;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import eu.smartclide.contexthandling.dle.listener.DleGitMonitorProgressListener;

public class GitMonitor extends MessageBrokerMonitor {
    public GitMonitor(final DataSource dataSource,
                      final Interpreter interpreter,
                      final Monitor monitor,
                      final Indexer indexer,
                      final AmIMonitoringConfiguration configuration) throws IOException, TimeoutException {
        super(dataSource, interpreter, monitor, indexer, configuration);

        // FIXME: this is a temporary workaround and should be removed!
        addProgressListener(new DleGitMonitorProgressListener((GitlabDataSource) dataSource));
    }
}
