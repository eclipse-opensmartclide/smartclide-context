package de.atb.context.monitoring.analyser;

/*-
 * #%L
 * SmartCLIDE Monitoring
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

import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import de.atb.context.monitoring.analyser.messagebroker.MessageBrokerAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitAnalyser extends MessageBrokerAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(GitAnalyser.class);

    private static final Gson GSON = new Gson();

    public GitAnalyser(final DataSource dataSource,
                       final InterpreterConfiguration interpreterConfiguration,
                       final Indexer indexer,
                       final Document document,
                       final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
    }

    @Override
    public List<GitDataModel> analyseObject(final String input) {
        try {
            final GitMessage gitMessage = GSON.fromJson(input, GitMessage.class);
            final GitDataModel model = new GitDataModel();
            model.addGitMessage(gitMessage);
            model.setMonitoredAt(new Date());
            return List.of(model);
        } catch (Exception e) {
            logger.error("Error analysing input: {}", input);
            return List.of();
        }
    }
}
