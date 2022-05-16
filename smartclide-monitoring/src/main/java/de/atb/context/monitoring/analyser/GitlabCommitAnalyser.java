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

import de.atb.context.monitoring.analyser.webservice.WebServiceAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.GitlabCommitDataModel;
import de.atb.context.monitoring.models.GitlabCommitMessage;
import de.atb.context.monitoring.models.IWebService;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import eu.smartclide.contexthandling.services.GitlabApiClient;
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
                dataSource.getUri(), ((GitlabDataSource) dataSource).getInterval()
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
