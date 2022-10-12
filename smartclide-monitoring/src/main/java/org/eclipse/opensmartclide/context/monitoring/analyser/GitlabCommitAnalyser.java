package org.eclipse.opensmartclide.context.monitoring.analyser;

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

import org.eclipse.opensmartclide.context.monitoring.analyser.webservice.WebServiceAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.GitlabDataSource;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitMessage;
import org.eclipse.opensmartclide.context.monitoring.models.IWebService;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.contexthandling.services.GitlabApiClient;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class GitlabCommitAnalyser extends WebServiceAnalyser<GitlabCommitDataModel> {

    private static final Logger logger = LoggerFactory.getLogger(GitlabCommitAnalyser.class);

    private final GitlabApiClient gitlabApiClient;

    public GitlabCommitAnalyser(final DataSource dataSource,
                                final InterpreterConfiguration interpreterConfiguration,
                                final Indexer indexer,
                                final Document document,
                                final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
        if (!(dataSource instanceof GitlabDataSource)) {
            throw new IllegalArgumentException("Given dataSource must be of type GitlabDataSource!");
        }
        gitlabApiClient = new GitlabApiClient(
                ((GitlabDataSource) dataSource).getGitLabAccessToken(),
                dataSource.getUri()
        );
    }

    @Override
    public List<GitlabCommitDataModel> analyseObject(IWebService service) {
        try {
            final List<GitlabCommitMessage> gitlabCommitMessages = gitlabApiClient.getGitlabCommitMessages();

            if (gitlabCommitMessages.isEmpty()) {
                return List.of();
            }

            final GitlabCommitDataModel model = new GitlabCommitDataModel();
            model.setGitlabCommitMessages(gitlabCommitMessages);
            model.setMonitoredAt(new Date());
            logger.info("Analysed {} GitlabCommitMessages", gitlabCommitMessages.size());
            return List.of(model);
        } catch (Exception e) {
            logger.error("Error analysing service: {}", service);
            return List.of();
        }
    }
}
