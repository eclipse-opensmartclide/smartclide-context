package de.atb.context.monitoring.monitors.messagebroker;

/*-
 * #%L
 * ATB Context Monitoring Core Services
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

import com.rabbitmq.client.*;
import de.atb.context.monitoring.analyser.messagebroker.MessageBrokerAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.events.MonitoringProgressListener;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.monitors.ThreadedMonitor;
import de.atb.context.monitoring.parser.messagebroker.MessageBrokerParser;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebServiceMonitor
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class MessageBrokerMonitor extends ThreadedMonitor<String, IMonitoringDataModel<?, ?>> {
    private MessageBrokerParser parser;
    protected MessageBrokerDataSource dataSource;
    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    protected String id;
    protected boolean isAnalysisRunning;

    public MessageBrokerMonitor(final DataSource dataSource, final Interpreter interpreter, final Monitor monitor, final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreter, monitor, indexer, amiConfiguration);
        if (dataSource.getType().equals(DataSourceType.MessageBroker) && (dataSource instanceof MessageBrokerDataSource)) {
            this.dataSource = (MessageBrokerDataSource) dataSource;
        } else {
            throw new IllegalArgumentException("Given dataSource must be of type MessageBrokerDataSource!");
        }
        this.logger.info("Initializing MessageBrokerMonitor for uri: " + dataSource.getUri());

        this.running = false;
        this.isAnalysisRunning = false;

        this.interpreterConfiguration = this.interpreter.getConfigurations().get(0); // FIXME exception if interpreter is null
    }

    @Override
    public final boolean isRunning() {
        return this.running;
    }

    @Override
    public final void pause() {
        this.running = false;
        this.executor.shutdown();
    }

    @Override
    public final void restart() {
        shutdown();
        run();
    }

    @Override
    public final void shutdown() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(2, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } finally {
            this.running = false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ThreadedMonitor#shutdown(long)
     */
    @Override
    protected final void shutdown(final long timeOut, final TimeUnit unit) {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(timeOut, unit)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } finally {
            this.running = false;
        }
    }

    @Override
    public final void run() {
        try {
            Thread.currentThread().setName(this.getClass().getSimpleName() + " (" + this.dataSource.getId() + ")");
            addProgressListener(MessageBrokerMonitor.this);
            this.running = true;

            if ((this.dataSource.getUri() != null)) {
                parser = interpreterConfiguration.createParser(
                        this.dataSource, this.indexer, this.amiConfiguration);
            }

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(dataSource.getMessageBrokerServer());
            factory.setUsername(dataSource.getUserName());
            factory.setPassword(dataSource.getPassword());

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(dataSource.getTopic(), false, false, false, null);

            DeliverCallback deliverCallback = new DeliverCallback() {
                public void handle(String s, Delivery delivery) throws IOException {
                    String envelope = delivery.getEnvelope().toString();
                    String message = new String(delivery.getBody(), "UTF-8");

                    handleMessageBroker(envelope, message);
                }
            };

            channel.basicConsume(dataSource.getTopic(), true, deliverCallback, new CancelCallback() {
                public void handle(String consumerTag) throws IOException {
                }
            });
        } catch (Exception e) {
            this.logger.error("Error starting MessageBrokerMonitor! ", e);
        }
    }

    public void monitor() {
        this.logger.info("Starting monitoring for MessageBroker at URI: " + this.dataSource.getMessageBrokerServer());
    }

    protected final void handleMessageBroker(String envelope, String message) {
        this.logger.debug("Handling URI " + this.dataSource.getUri() + "...");
        this.isAnalysisRunning = true;
        try {
            if ((this.dataSource.getUri() != null)) {
                MessageBrokerAnalyser analyser = (MessageBrokerAnalyser) parser.getAnalyser();

                if (parser.parse(message)) {
                    this.indexer.addDocumentToIndex(parser.getDocument());
                    this.raiseParsedEvent(message, parser.getDocument());
                    List<IMonitoringDataModel<?, ?>> analysedMessages = analyser.analyse(message);
                    this.raiseAnalysedEvent(analysedMessages, message, analyser.getDocument());
                }
            }
            // clean up indexed document and remove all stuff already used
            parser.getDocument().removeFields("content");
            parser.getDocument().removeFields("monitoredAt");
        } catch (Exception e) {
            logger.error("Unknown error ind MessageBrokerMonitor.handleMessageBroker()", e);
        }
        this.isAnalysisRunning = false;
    }

    /**
     * Every X seconds this class calls the monitor method to consume all received topics from kafka and
     * send them to the MessageBrokerParser and KafkaAnalyser
     */
    protected static final class MessageBrokerMonitoringRunner implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(MessageBrokerMonitoringRunner.class);
        private final MessageBrokerMonitor parent;

        public MessageBrokerMonitoringRunner(final MessageBrokerMonitor parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            try {
                this.parent.monitor();
            } catch (Exception e) {
                logger.error("Error during monitoring of MessageBroker! ", e);
            }
        }

    }
}
