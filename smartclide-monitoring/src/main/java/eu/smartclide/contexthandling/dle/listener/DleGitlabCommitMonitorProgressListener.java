package eu.smartclide.contexthandling.dle.listener;

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
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import de.atb.context.monitoring.events.MonitoringProgressListener;
import de.atb.context.monitoring.models.GitlabCommitDataModel;
import de.atb.context.monitoring.models.GitlabCommitMessage;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.models.IWebService;
import de.atb.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import eu.smartclide.contexthandling.dle.model.CommitMessage;
import eu.smartclide.contexthandling.dle.model.DleMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
                .map(this::convertToDleMessage)
                .forEach(this::send);
    }

    private DleMessage convertToDleMessage(final GitlabCommitMessage gitlabCommitMessage) {
        return DleMessage.builder()
                .monitor(CommitMessage.builder()
                        .repoId(gitlabCommitMessage.getRepository())
                        .user(gitlabCommitMessage.getUser())
                        .branch(gitlabCommitMessage.getBranch())
                        .timeSinceLastCommit(gitlabCommitMessage.getTimeSinceLastCommit())
                        .numberOfFilesModified(gitlabCommitMessage.getNoOfModifiedFiles())
                        .build())
                .build();
    }

    private void send(final DleMessage dleMessage) {
        if (useTopic) {
            MessageBrokerUtil.convertAndSendToTopic(channel, exchange, topic, dleMessage);
        } else {
            MessageBrokerUtil.convertAndSendToQueue(channel, queue, dleMessage);
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
