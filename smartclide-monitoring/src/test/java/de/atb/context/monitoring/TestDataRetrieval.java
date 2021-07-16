package de.atb.context.monitoring;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.atb.context.monitoring.config.models.Config;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.services.AmIMonitoringService;
import de.atb.context.services.IAmIMonitoringDataRepositoryService;
import de.atb.context.services.IAmIMonitoringService;
import de.atb.context.services.SWServiceContainer;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.cxf.endpoint.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

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
    private static final String ROUTING_KEY = "git.commits.*";
    private static final String DATASOURCE_GIT = "datasource-git";

    private static Server server;
    private static IAmIMonitoringService service;
    private static AmIMonitoringDataRepositoryServiceWrapper monitoringDataRepository;
    private static IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> reposService;

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
        monitoringDataRepository = new AmIMonitoringDataRepositoryServiceWrapper(reposService);

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
        final String message = new Gson().toJson(gitMessage);
        logger.info("Publishing message: {}", message);
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes(StandardCharsets.UTF_8));

        Thread.sleep(10000);

        // get the monitored data from the repository (latest registry)
        //List<ProntoDataModel> data = monitoringDataRepository.getMonitoringData(ApplicationScenario.DIVERSITY_1, ProntoDataModel.class, 1);

        //Assert.assertTrue(data != null);
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

}
