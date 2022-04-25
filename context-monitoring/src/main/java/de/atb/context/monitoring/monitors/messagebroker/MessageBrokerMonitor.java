package de.atb.context.monitoring.monitors.messagebroker;

/*
 * #%L
 * ATB Context Monitoring Core Services
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

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import de.atb.context.monitoring.analyser.messagebroker.MessageBrokerAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.monitors.ScheduledExecutorThreadedMonitor;
import de.atb.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import de.atb.context.monitoring.parser.messagebroker.MessageBrokerParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

import java.nio.charset.StandardCharsets;

/**
 * WebServiceMonitor
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class MessageBrokerMonitor extends ScheduledExecutorThreadedMonitor<String, IMonitoringDataModel<?, ?>> {

    private final MessageBrokerDataSource dataSource;

    private MessageBrokerParser parser;

    private final DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        handleMessage(delivery.getEnvelope(), message);
    };

    private final CancelCallback cancelCallback = consumerTag -> logger.info("{} cancelled!", consumerTag);

    public MessageBrokerMonitor(final DataSource dataSource,
                                final Interpreter interpreter,
                                final Monitor monitor,
                                final Indexer indexer,
                                final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (dataSource.getType().equals(DataSourceType.MessageBroker) && (dataSource instanceof MessageBrokerDataSource)) {
            this.dataSource = (MessageBrokerDataSource) dataSource;
        } else {
            throw new IllegalArgumentException("Given dataSource must be of type MessageBrokerDataSource!");
        }
        this.logger.info("Initializing MessageBrokerMonitor for uri: " + dataSource.getUri());
    }

    @Override
    protected MessageBrokerParser getParser(final InterpreterConfiguration setting) {
        return setting.createParser(this.dataSource, this.indexer, this.amiConfiguration);
    }

    @Override
    protected long getScheduleInitialDelay() {
        return 500;
    }

    @Override
    protected InterpreterConfiguration getInterpreterConfiguration() {
        return this.interpreter.getConfigurations().isEmpty() ? null : this.interpreter.getConfigurations().get(0);
    }

    @Override
    protected void doMonitor(final InterpreterConfiguration setting) throws Exception {
        if (setting != null) {
            if ((this.dataSource.getUri() != null)) {
                this.parser = getParser(setting);
            }

            final Channel channel = MessageBrokerUtil.connectToTopicExchange(
                dataSource.getMessageBrokerServer(),
                dataSource.getMessageBrokerPort(),
                dataSource.getUserName(),
                dataSource.getPassword(),
                dataSource.getIncomingExchange(),
                dataSource.isIncomingDurable()
            );
            MessageBrokerUtil.registerListenerOnTopic(
                channel,
                dataSource.getIncomingExchange(),
                dataSource.getIncomingTopic(),
                dataSource.getId(),
                deliverCallback,
                cancelCallback
            );
        }
    }

    // FIXME: envelope is never used
    protected final void handleMessage(Envelope envelope, String message) {
        this.logger.info(
            "Handling message from exchange \"{}\" with routing key \"{}\" ...",
            envelope.getExchange(),
            envelope.getRoutingKey()
        );
        try {
            if ((this.dataSource.getUri() != null)) {
                MessageBrokerAnalyser analyser = (MessageBrokerAnalyser) parser.getAnalyser();

                parseAndAnalyse(message, parser, analyser);
            }
            // clean up indexed document and remove all stuff already used
            parser.getDocument().removeFields("content");
            parser.getDocument().removeFields("monitoredAt");
        } catch (Exception e) {
            logger.error("Unknown error in MessageBrokerMonitor.handleMessageBroker()", e);
        }
    }
}
