package org.eclipse.opensmartclide.contexthandling.dle.listener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/*-
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2015 - 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import com.rabbitmq.client.Channel;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;

import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.GitlabDataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import org.eclipse.opensmartclide.context.monitoring.events.MonitoringProgressListener;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitMessage;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.IWebService;
import org.eclipse.opensmartclide.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import org.eclipse.opensmartclide.contexthandling.dle.model.CommitMessage;

public class DleGitlabCommitMonitorProgressListener
        implements MonitoringProgressListener<IWebService, IMonitoringDataModel<?, ?>> {

    private final Channel channel;
    private final String exchange;
    private final String topic;
    private final String queue;
    private final boolean useTopic;

    public DleGitlabCommitMonitorProgressListener(final GitlabDataSource dataSource)
            throws IOException, TimeoutException {
        exchange = dataSource.getOutgoingExchange();
        topic = dataSource.getOutgoingTopic();
        queue = dataSource.getOutgoingQueue();
        useTopic = isOutgoingTopic();
        channel = connectToMessageBroker(
                dataSource.getMessageBrokerServer(),
                dataSource.getMessageBrokerPort(),
                dataSource.getUserName(),
                dataSource.getPassword(),
                dataSource.isOutgoingDurable()
        );
    }

    @Override
    public void documentIndexed(final String indexId, final Document document) {
        // noop
    }

    @Override
    public void documentParsed(final IWebService parsed, final Document document) {
        // noop
    }

    @Override
    public void documentAnalysed(final List<IMonitoringDataModel<?, ?>> analysed,
                                 final IWebService parsed,
                                 final Document document) {
        analysed.stream()
                .filter(iMonitoringDataModel -> iMonitoringDataModel instanceof GitlabCommitDataModel)
                .map(iMonitoringDataModel -> (GitlabCommitDataModel) iMonitoringDataModel)
                .flatMap(gitlabCommitDataModel -> gitlabCommitDataModel.getGitlabCommitMessages().stream())
                .map(this::convertToCommitMessage)
                .forEach(this::send);
    }

    private CommitMessage convertToCommitMessage(final GitlabCommitMessage gitlabCommitMessage) {
        return CommitMessage.builder()
                        .repoId(gitlabCommitMessage.getRepository())
                        .user(gitlabCommitMessage.getUser())
                        .branch(gitlabCommitMessage.getBranch())
                        .timeSinceLastCommit(gitlabCommitMessage.getTimeSinceLastCommit())
                .numberOfFilesModified(gitlabCommitMessage.getNoOfModifiedFiles())
                .build();
    }

    private void send(final CommitMessage commitMessage) {
        if (useTopic) {
            MessageBrokerUtil.convertAndSendToTopic(channel, exchange, topic, commitMessage);
        } else {
            MessageBrokerUtil.convertAndSendToQueue(channel, queue, commitMessage);
        }
    }

    private Channel connectToMessageBroker(final String host,
                                           final Integer port,
                                           final String userName,
                                           final String password,
                                           final boolean durable) throws IOException, TimeoutException {
        return useTopic
                ? MessageBrokerUtil.connectToTopicExchange(host, port, userName, password, exchange, durable)
                : MessageBrokerUtil.connectToQueue(host, port, userName, password, queue, durable);
    }

    private boolean isOutgoingTopic() {
        if (StringUtils.isBlank(topic) && StringUtils.isBlank(queue)) {
            throw new IllegalArgumentException(String.format(
                    "Must specify either %s or %s!",
                    MessageBrokerDataSourceOptions.OutgoingTopic.getKeyName(),
                    MessageBrokerDataSourceOptions.OutgoingQueue.getKeyName()
            ));
        }
        if (StringUtils.isNotBlank(topic)) {
            if (StringUtils.isBlank(exchange)) {
                throw new IllegalArgumentException(String.format(
                        "Must specify %s when connecting to topic %s!",
                        MessageBrokerDataSourceOptions.OutgoingExchange.getKeyName(),
                        MessageBrokerDataSourceOptions.OutgoingTopic.getKeyName()
                ));
            }
            return true;
        } else {
            return false;
        }
    }
}
