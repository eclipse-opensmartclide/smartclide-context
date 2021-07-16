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

import de.atb.context.monitoring.analyser.messagebroker.MessageBrokerAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GitAnalyser extends MessageBrokerAnalyser {
    private final Logger logger = LoggerFactory.getLogger(MessageBrokerAnalyser.class);

    public GitAnalyser(DataSource dataSource, InterpreterConfiguration interpreterConfiguration, Indexer indexer, Document document, AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
    }

    @Override
    public List<GitDataModel> analyseObject(String service) {
        List<GitDataModel> models = new ArrayList<>();
        JSONParser parser = new JSONParser();
        GitDataModel model = new GitDataModel();
        GitMessage gitMessage = null;

        List<IndexableField> recMixerMessage = new ArrayList(Arrays.asList(this.document.getFields("content")));

        for (IndexableField msg : recMixerMessage) {
            if (msg != null) {
                gitMessage = new GitMessage();
                addMixerInformation(gitMessage, msg);
            }
        }

        models.add(model);
        model.addGitMessage(gitMessage);
        model.setMonitoredAt(new Date());

        return models;
    }

    private void addMixerInformation(GitMessage gitMessage, IndexableField message) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(message.stringValue());
            gitMessage.setUser((String) jsonObject.get("user"));
            gitMessage.setRepository((String) jsonObject.get("repository"));
            gitMessage.setBranch((String) jsonObject.get("branch"));
            gitMessage.setNoOfCommitsInBranch((Integer) jsonObject.get("no_of_commits_in_branch"));
            gitMessage.setNoOfPushesInBranch((Integer) jsonObject.get("no_of_pushes_in_branch"));
            gitMessage.setNoOfModifiedFiles((Integer) jsonObject.get("no_of_modified_files"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
