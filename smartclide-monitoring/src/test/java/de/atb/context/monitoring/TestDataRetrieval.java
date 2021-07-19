package de.atb.context.monitoring;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.Config;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.services.AmIMonitoringService;
import de.atb.context.services.IAmIMonitoringDataRepositoryService;
import de.atb.context.services.IAmIMonitoringService;
import de.atb.context.services.SWServiceContainer;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import eu.smartclide.contexthandling.dle.model.CommitMessage;
import eu.smartclide.contexthandling.dle.model.DleMessage;
import org.apache.cxf.endpoint.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * TestMonitoringService
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 */


public class TestDataRetrieval {

    private static final Logger logger = LoggerFactory.getLogger(TestDataRetrieval.class);

    private static final String RABBITMQ_3_ALPINE = "rabbitmq:3-alpine";
    private static final String EXCHANGE_NAME = "smartclide-monitoring";
    private static final String ROUTING_KEY_MONITORING = "monitoring.git.commits.*";
    private static final String ROUTING_KEY_DLE = "dle.commits.*";
    private static final String DATASOURCE_GIT = "datasource-git";
    private static final Gson GSON = new Gson();

    private static Server server;
    private static IAmIMonitoringService service;
    private static AmIMonitoringDataRepositoryServiceWrapper<GitDataModel> monitoringDataRepository;
    private static IAmIMonitoringDataRepositoryService<GitDataModel> reposService;

    private Channel channel;

    // starts a new rabbitmq message broker in a docker container.
    // @Rule must be final.
    @Rule
    public final RabbitMQContainer container = new RabbitMQContainer(RABBITMQ_3_ALPINE);

    @Before
    public void setup() throws Exception {
        final String rabbitMQContainerHost = container.getHost();
        final Integer rabbitMQContainerAmqpPort = container.getAmqpPort();
        setupBroker(rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        createFakeDleListener(rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        final String monitoringConfig = Path.of(getClass().getResource("/monitoring-config.xml").toURI()).toString();
        final String serviceConfig = Path.of(getClass().getResource("/services-config.xml").toURI()).toString();

        updateMessageBrokerDataSource(monitoringConfig, rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        AmIMonitoringConfiguration amiConfig = new AmIMonitoringConfiguration();
        amiConfig.setId("TEST_GITMESSAGE");
        amiConfig.setServiceConfiguration(readFile(monitoringConfig));

        SWServiceContainer serviceContainer = new SWServiceContainer("AmI-repository", serviceConfig);
        ServiceManager.registerWebservice(serviceContainer);
        ServiceManager.getLSWServiceContainer().add(serviceContainer);

        for (SWServiceContainer container : ServiceManager.getLSWServiceContainer()) {
            if (container.getServerClass().toString().contains("AmIMonitoringDataRepository")) {
                reposService = ServiceManager.getWebservice(container);
            }
        }
        monitoringDataRepository = new AmIMonitoringDataRepositoryServiceWrapper<>(reposService);

        server = ServiceManager.registerWebservice(AmIMonitoringService.class);
        service = ServiceManager.getWebservice(IAmIMonitoringService.class);
        service.configureService(amiConfig);
    }

    @After
    public void tearDown() throws IOException, TimeoutException {
        ServiceManager.shutdownServiceAndEngine(server);
        monitoringDataRepository.shutdown();

        if (channel != null) {
            channel.close();
        }
    }

    @Test
    public void testDataRetrieval() throws IOException, InterruptedException {
        // start monitoring service (the repository is implicitly started from within the monitoring service)
        service.start();

        Thread.sleep(10000);

        final GitMessage gitMessage = GitMessage.builder()
                .timestamp(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .user("user@smartclide.eu")
                .repository("git@github.com:eclipse-researchlabs/smartclide-context.git")
                .branch("branch")
                .noOfCommitsInBranch(42)
                .noOfModifiedFiles(3)
                .noOfPushesInBranch(17)
                .build();
        final String message = GSON.toJson(gitMessage);
        logger.info("Publishing message: {}", message);
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY_MONITORING, null, message.getBytes(StandardCharsets.UTF_8));

        Thread.sleep(50000);

        // get the monitored data from the repository (latest registry)
        final ApplicationScenario applicationScenario = ApplicationScenario.getInstance();
        logger.debug("application scenario: {}", applicationScenario);
        List<GitDataModel> data = monitoringDataRepository.getMonitoringData(applicationScenario, GitDataModel.class, 1);

        assertNotNull(data);
        assertFalse(data.isEmpty());

        final List<GitMessage> gitMessages = data.get(data.size() - 1).getGitMessages();
        for (GitMessage gm : gitMessages) {
            convertAndSendToDle(gm);
        }
    }

    private String readFile(String filename) {
        File f = new File(filename);
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    private void setupBroker(final String host, final Integer port) throws IOException, TimeoutException {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        final Connection connection = factory.newConnection();
        channel = connection.createChannel();
    }

    private void updateMessageBrokerDataSource(final String monitoringConfig, final String host, final Integer port)
            throws Exception {
        final Persister persister = new Persister();
        final Config config = persister.read(Config.class, new File(monitoringConfig));
        final Map<String, String> optionsMap = config.getDataSource(DATASOURCE_GIT).getOptionsMap();
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerServer.getKeyName(), host);
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerPort.getKeyName(), port.toString());
        config.getDataSource(DATASOURCE_GIT).setOptions(optionsMap);
        persister.write(config, new File(monitoringConfig));
    }

    private void createFakeDleListener(final String host, final Integer port) throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
        final String queue = channel.queueDeclare("", true, false, false, null).getQueue();
        channel.queueBind(queue, EXCHANGE_NAME, ROUTING_KEY_DLE);
        channel.basicConsume(
                queue,
                (t, m) -> logger.debug("DLE received message: {}", new String(m.getBody(), StandardCharsets.UTF_8)),
                (t) -> logger.info("cancelled!")
        );
    }

    private void convertAndSendToDle(final GitMessage gm) throws IOException {
        logger.debug("{}", gm);
        final DleMessage dleMessage = DleMessage.builder()
                .monitor(
                        CommitMessage.builder()
                                .user(gm.getUser())
                                .branch(gm.getBranch())
                                .files(gm.getNoOfModifiedFiles())
                                .build()
                )
                .build();
        final String dleMessageJson = GSON.toJson(dleMessage);
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY_DLE, null, dleMessageJson.getBytes(StandardCharsets.UTF_8));
    }

}
