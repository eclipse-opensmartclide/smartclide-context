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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.events.MonitoringProgressListener;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import eu.smartclide.contexthandling.dle.model.CommitMessage;
import eu.smartclide.contexthandling.dle.model.DleMessage;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DleGitMonitorProgressListener implements MonitoringProgressListener<String, IMonitoringDataModel<?, ?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DleGitMonitorProgressListener.class);

    private final String topic;
    private final String exchange;
    private final Channel channel;

    public DleGitMonitorProgressListener(final MessageBrokerDataSource messageBrokerDataSource)
            throws IOException, TimeoutException {
        exchange = messageBrokerDataSource.getExchange();
        topic = messageBrokerDataSource.getDleTopic();
        channel = MessageBrokerUtil.connectToTopicExchange(messageBrokerDataSource);
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

    private void send(final DleMessage dleMessage) {
        try {
            // simulate that actual context-extraction will take some time
            Thread.sleep(1000);
            MessageBrokerUtil.convertAndSendToTopic(channel, exchange, topic, dleMessage);
        } catch (Exception e) {
            LOGGER.error("Failed to send {} to {}/{}", dleMessage, exchange, topic, e);
        }
    }
}
