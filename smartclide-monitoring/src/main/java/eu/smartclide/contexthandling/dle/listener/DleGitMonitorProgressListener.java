package eu.smartclide.contexthandling.dle.listener;

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

import com.rabbitmq.client.Channel;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import de.atb.context.monitoring.events.MonitoringProgressListener;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import eu.smartclide.contexthandling.dle.model.CommitMessage;
import eu.smartclide.contexthandling.dle.model.DleMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class DleGitMonitorProgressListener implements MonitoringProgressListener<String, IMonitoringDataModel<?, ?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DleGitMonitorProgressListener.class);

    private final Channel channel;
    private final String exchange;
    private final String topic;
    private final String queue;

    private boolean useTopic = false;

    public DleGitMonitorProgressListener(final MessageBrokerDataSource messageBrokerDataSource)
            throws IOException, TimeoutException {
        exchange = messageBrokerDataSource.getOutgoingExchange();
        topic = messageBrokerDataSource.getOutgoingTopic();
        queue = messageBrokerDataSource.getOutgoingQueue();

        channel = connectToMessageBroker(
                messageBrokerDataSource.getMessageBrokerServer(),
                messageBrokerDataSource.getMessageBrokerPort(),
                messageBrokerDataSource.getUserName(),
                messageBrokerDataSource.getPassword(),
                messageBrokerDataSource.isOutgoingDurable()
        );
    }

    public DleGitMonitorProgressListener(final GitlabDataSource gitlabDataSource) throws IOException, TimeoutException {
        exchange = gitlabDataSource.getOutgoingExchange();
        topic = gitlabDataSource.getOutgoingTopic();
        queue = gitlabDataSource.getOutgoingQueue();

        channel = connectToMessageBroker(
                gitlabDataSource.getMessageBrokerServer(),
                gitlabDataSource.getMessageBrokerPort(),
                gitlabDataSource.getUserName(),
                gitlabDataSource.getPassword(),
                gitlabDataSource.isOutgoingDurable()
        );
    }

    @Override
    public void documentIndexed(final String indexId, final Document document) {
        // noop
    }

    @Override
    public void documentParsed(final String parsed, final Document document) {
        // noop
    }

    @Override
    public void documentAnalysed(final List<IMonitoringDataModel<?, ?>> analysed,
                                 final String parsed,
                                 final Document document) {
        analysed.stream()
                .filter(iMonitoringDataModel -> iMonitoringDataModel instanceof GitDataModel)
                .map(iMonitoringDataModel -> (GitDataModel) iMonitoringDataModel)
                .flatMap(gitDataModel -> gitDataModel.getGitMessages().stream())
                .map(this::convertToDleMessage)
                .forEach(this::send);
    }

    private DleMessage convertToDleMessage(final GitMessage gitMessage) {
        return DleMessage.builder()
                .monitor(CommitMessage.builder()
                        .user(gitMessage.getUser())
                        .branch(gitMessage.getBranch())
                        .files(gitMessage.getNoOfModifiedFiles())
                        .build())
                .build();
    }

    private Channel connectToMessageBroker(final String host,
                                           final int port,
                                           final String userName,
                                           final String password,
                                           final boolean durable) throws IOException, TimeoutException {
        if (StringUtils.isBlank(topic) && StringUtils.isBlank(queue)) {
            throw new IllegalArgumentException(String.format(
                    "Must specify either %s or %s!",
                    MessageBrokerDataSourceOptions.OutgoingTopic.getKeyName(),
                    MessageBrokerDataSourceOptions.OutgoingTopic.getKeyName()
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
            useTopic = true;
            return MessageBrokerUtil.connectToTopicExchange(host, port, userName, password, exchange, durable);
        } else {
            useTopic = false;
            return MessageBrokerUtil.connectToQueue(host, port, userName, password, queue, durable);
        }
    }

    private void send(final DleMessage dleMessage) {
        if (useTopic) {
            MessageBrokerUtil.convertAndSendToTopic(channel, exchange, topic, dleMessage);
        } else {
            MessageBrokerUtil.convertAndSendToQueue(channel, queue, dleMessage);
        }
    }
}
