package de.atb.context.monitoring.analyser;

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

import com.google.gson.Gson;
import de.atb.context.monitoring.analyser.webservice.WebServiceAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.IWebService;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.List;

public class GitlabCommitAnalyser extends WebServiceAnalyser<GitDataModel> {

    private static final Logger logger = LoggerFactory.getLogger(GitlabCommitAnalyser.class);

    private static final Gson GSON = new Gson();

    public GitlabCommitAnalyser(final DataSource dataSource,
                                final InterpreterConfiguration interpreterConfiguration,
                                final Indexer indexer,
                                final Document document,
                                final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
    }

    @Override
    public List<GitDataModel> analyseObject(IWebService service) {
        try {
            final GitDataModel model = new GitDataModel();
            URI gitlabEndpoint = service.getURI();

            // TODO "NEED TO BE IMPLEMENTED"
            // * get list of projects where CH user has access to
            // * iterate through projects and get last commits
            // * for each identified "new" commit add a GitNessage below
            model.addGitMessage(new GitMessage());

            model.setMonitoredAt(new Date());
            return List.of(model);
        } catch (Exception e) {
            logger.error("Error analysing service: {}", service);
            return List.of();
        }
    }
}
