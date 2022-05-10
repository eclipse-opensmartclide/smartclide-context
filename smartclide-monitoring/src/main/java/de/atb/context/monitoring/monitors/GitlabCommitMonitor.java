package de.atb.context.monitoring.monitors;

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

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.monitors.webservice.WebServiceMonitor;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

public class GitlabCommitMonitor extends WebServiceMonitor {
    public GitlabCommitMonitor(final DataSource dataSource,
                               final Interpreter interpreter,
                               final Monitor monitor,
                               final Indexer indexer,
                               final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (!(dataSource instanceof GitlabDataSource)) {
            throw new IllegalArgumentException("Given dataSource must be of type GitlabDataSource!");
        }
        this.logger.info("Initialized GitlabCommitMonitor for uri: " + dataSource.getUri());
    }
}
