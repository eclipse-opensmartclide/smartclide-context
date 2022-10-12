package org.eclipse.opensmartclide.context.monitoring.parser;

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

import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.GitlabDataSource;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IWebService;
import org.eclipse.opensmartclide.context.monitoring.parser.webservice.WebServiceParser;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;

public class GitlabCommitParser extends WebServiceParser {
    public GitlabCommitParser(final DataSource dataSource,
                              final InterpreterConfiguration interpreterConfiguration,
                              final Indexer indexer,
                              final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
        if (!(dataSource instanceof GitlabDataSource)) {
            throw new IllegalArgumentException("Given dataSource must be of type GitlabDataSource!");
        }
    }

    @Override
    protected boolean parseObject(IWebService service, Document document) {
        return true;
    }
}
