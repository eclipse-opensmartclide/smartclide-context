package org.eclipse.opensmartclide.context.monitoring.monitors;

/*
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.GitlabDataSource;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.monitors.webservice.WebServiceMonitor;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.contexthandling.dle.listener.DleGitlabCommitMonitorProgressListener;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class GitlabCommitMonitor extends WebServiceMonitor {
    public GitlabCommitMonitor(final DataSource dataSource,
                               final Interpreter interpreter,
                               final Monitor monitor,
                               final Indexer indexer,
                               final AmIMonitoringConfiguration configuration) throws IOException, TimeoutException {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (!(dataSource instanceof GitlabDataSource)) {
            throw new IllegalArgumentException("Given dataSource must be of type GitlabDataSource!");
        }
        this.addProgressListener(new DleGitlabCommitMonitorProgressListener((GitlabDataSource) dataSource));
        this.logger.info("Initialized GitlabCommitMonitor for uri: " + dataSource.getUri());
    }
}
