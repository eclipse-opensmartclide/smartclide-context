package de.atb.context.monitoring.parser;

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

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IWebService;
import de.atb.context.monitoring.parser.webservice.WebServiceParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
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
