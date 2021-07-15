package de.atb.context.monitoring.monitors.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

public class MessageBrokerMonitorIntegrationTests {

    private static final Logger logger = LoggerFactory.getLogger(MessageBrokerMonitorIntegrationTests.class);

    private static final String RABBITMQ_3_ALPINE = "rabbitmq:3-alpine";
    private static final String EXCHANGE_NAME = "monitoring";
    private static final String ROUTING_KEY = "commits";

    private Channel channel;

    // starts a new rabbitmq message broker in a docker container.
    // @Rule must be final.
    @Rule
    public final RabbitMQContainer container = new RabbitMQContainer(RABBITMQ_3_ALPINE);

    @Before
    public void setup() throws IOException, TimeoutException {
        final String rabbitMQContainerHost = container.getHost();
        final Integer rabbitMQContainerAmqpPort = container.getAmqpPort();

        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQContainerHost);
        factory.setPort(rabbitMQContainerAmqpPort);
        final Connection connection = factory.newConnection();
        channel = connection.createChannel();

        // example consumer - to be replaced
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        final String queue = channel.queueDeclare("", true, false, false, null).getQueue();
        channel.queueBind(queue, EXCHANGE_NAME, ROUTING_KEY);

        channel.basicConsume(
            queue,
            true,
            (tag, message) -> {
                final String body = new String(message.getBody(), StandardCharsets.UTF_8);
                System.out.println("received " + message.getEnvelope().getRoutingKey() + ": " + body);
            },
            tag -> {
            }
        );
    }

    @After
    public void tearDown() throws IOException, TimeoutException {
        if (channel != null) {
            channel.close();
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {
        logger.info("running test...");
        for (int i = 0; i < 5; i++) {
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, ("foo" + i).getBytes(StandardCharsets.UTF_8));
            Thread.sleep(1000);
        }
    }

}
