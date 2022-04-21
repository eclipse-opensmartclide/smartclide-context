package de.atb.context.monitoring.monitors.messagebroker.util;

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

import com.google.gson.Gson;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Helper class wrapping methods for interacting with message broker.
 */
public class MessageBrokerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBrokerUtil.class);
    private static final Gson GSON = new Gson();

    /**
     * Connect to the message broker specified by {@code host} and {@code port}
     * with the credentials specified by {@code userName} and {@code password}.
     * Create the given {@code exchange} if it does not exist yet.
     *
     * @param host     the host where the message broker is running
     * @param port     the port where the message broker is listening
     * @param userName the username to use when connecting to message broker - optional
     * @param password the password to use when connecting to message broker - optional
     * @param exchange the topic exchange's name
     * @return a {@link Channel} object representing the established connection to the message broker
     * @throws IOException      in case of error
     * @throws TimeoutException in case of error
     */
    public static Channel connectToTopicExchange(final String host,
                                                 final int port,
                                                 final String userName,
                                                 final String password,
                                                 final String exchange) throws IOException, TimeoutException {
        final Connection connection = getConnection(host, port, userName, password);

        final Channel channel = connection.createChannel();

        LOGGER.info("Creating topic exchange {}", exchange);
        channel.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, true);

        return channel;
    }

    /**
     * Connect to message broker. Message broker details and credentials are specified in the given {@code dataSource}.
     * Create the given {@code exchange} if it does not exist yet.
     *
     * @param dataSource the {@link MessageBrokerDataSource} containing the message broker connection details
     * @return a {@link Channel} object representing the established connection to the message broker
     * @throws IOException      in case of error
     * @throws TimeoutException in case of error
     * @see MessageBrokerUtil#connectToTopicExchange(String, int, String, String, String)
     */
    public static Channel connectToTopicExchange(final MessageBrokerDataSource dataSource)
        throws IOException, TimeoutException {
        return connectToTopicExchange(
            dataSource.getMessageBrokerServer(),
            dataSource.getMessageBrokerPort(),
            dataSource.getUserName(),
            dataSource.getPassword(),
            dataSource.getExchange()
        );
    }

    /**
     * Connect to the message broker specified by {@code host} and {@code port}
     * with the credentials specified by {@code userName} and {@code password}.
     * Create the given {@code queue} if it does not exist yet.
     *
     * @param host     the host where the message broker is running
     * @param port     the port where the message broker is listening
     * @param userName the username to use when connecting to message broker - optional
     * @param password the password to use when connecting to message broker - optional
     * @param queue    the queue's name
     * @return a {@link Channel} object representing the established connection to the message broker
     * @throws IOException      in case of error
     * @throws TimeoutException in case of error
     */
    public static Channel connectToQueue(final String host,
                                         final int port,
                                         final String userName,
                                         final String password,
                                         final String queue) throws IOException, TimeoutException {
        final Connection connection = getConnection(host, port, userName, password);

        final Channel channel = connection.createChannel();

        LOGGER.info("Creating queue {}", queue);
        channel.queueDeclare(queue, true, false, false, null);

        return channel;
    }

    /**
     * Register the given callback functions to consume messages on the given {@code exchange} for the given {@code topic}.
     * <p>
     * Use {@link MessageBrokerUtil#connectToTopicExchange(String, int, String, String, String)} or one of its overloads
     * to create {@link Channel}.
     *
     * @param channel         the {@link Channel} object representing the established connection to the message broker
     * @param exchange        the topic exchange's name
     * @param topic           the topic's name
     * @param queuePrefix     the prefix to attach to the queue's name
     * @param deliverCallback callback function to handle received messages
     * @param cancelCallback  callback function to handle cancellation of the listener
     * @throws IOException in case of error
     */
    public static void registerListenerOnTopic(final Channel channel,
                                               final String exchange,
                                               final String topic,
                                               final String queuePrefix,
                                               final DeliverCallback deliverCallback,
                                               final CancelCallback cancelCallback) throws IOException {
        LOGGER.info("Registering listener on topic {}/{}", exchange, topic);
        final String queueName = String.format("%s-%s", queuePrefix, UUID.randomUUID());
        final String queue = channel.queueDeclare(queueName, true, true, true, null).getQueue();
        LOGGER.info("Created queue {}", queue);
        channel.queueBind(queue, exchange, topic);
        channel.basicConsume(queue, true, deliverCallback, cancelCallback);
    }

    /**
     * Register the given callback functions to consume messages.
     * The exchange and topic to register for are specified in the given {@code dataSource}.
     * <p>
     * Use {@link MessageBrokerUtil#connectToTopicExchange(String, int, String, String, String)} or one of its overloads
     * to create {@link Channel}.
     *
     * @param channel         the {@link Channel} object representing the established connection to the message broker
     * @param dataSource      the {@link MessageBrokerDataSource} containing the exchange and topic details
     * @param deliverCallback callback function to handle received messages
     * @param cancelCallback  callback function to handle cancellation of the listener
     * @throws IOException in case of error
     */
    public static void registerListenerOnTopic(final Channel channel,
                                               final MessageBrokerDataSource dataSource,
                                               final DeliverCallback deliverCallback,
                                               final CancelCallback cancelCallback) throws IOException {
        registerListenerOnTopic(
            channel,
            dataSource.getExchange(),
            dataSource.getTopic(),
            dataSource.getId(),
            deliverCallback,
            cancelCallback
        );
    }

    /**
     * Converts the given {@code payload} object to a JSON string
     * and sends it to the given {@code topic} on the given {@code exchange}.
     * <p>
     * Use {@link MessageBrokerUtil#connectToTopicExchange(String, int, String, String, String)} or one of its overloads
     * to create {@link Channel}.
     *
     * @param channel  the {@link Channel} object representing the established connection to the message broker
     * @param exchange the topic exchange's name
     * @param topic    the topic's name
     * @param payload  the object to send
     * @throws IOException in case of error
     */
    public static void convertAndSendToTopic(final Channel channel,
                                             final String exchange,
                                             final String topic,
                                             final Object payload) throws IOException {
        final String jsonMessage = GSON.toJson(payload);
        LOGGER.info("Publishing message to topic {}/{}: {}", exchange, topic, jsonMessage);
        channel.basicPublish(exchange, topic, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts the given {@code payload} object to a JSON string and sends it to the given {@code queue}.
     * <p>
     * Use {@link MessageBrokerUtil#connectToQueue(String, int, String, String, String)} to create {@link Channel}.
     *
     * @param channel the {@link Channel} object representing the established connection to the message broker
     * @param queue   the queue's name
     * @param payload the object to send
     * @throws IOException in case of error
     */
    public static void convertAndSendToQueue(final Channel channel, final String queue, final Object payload)
        throws IOException {
        final String jsonMessage = GSON.toJson(payload);
        LOGGER.info("Publishing message to queue {}: {}", queue, jsonMessage);
        channel.basicPublish("", queue, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
    }

    private static Connection getConnection(final String host,
                                            final int port,
                                            final String userName,
                                            final String password) throws IOException, TimeoutException {
        LOGGER.info("Connecting to messagebroker {}:{} with user {}", host, port, userName != null ? userName : "<null>");
        final ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(host);
        factory.setPort(port);

        if (StringUtils.isNotBlank(userName)) {
            factory.setUsername(userName);
        }
        if (StringUtils.isNotBlank(password)) {
            factory.setPassword(password);
        }

        factory.setAutomaticRecoveryEnabled(true);

        return factory.newConnection();
    }
}
