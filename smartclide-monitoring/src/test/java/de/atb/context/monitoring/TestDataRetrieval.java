package de.atb.context.monitoring;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import de.atb.context.common.ContextPathUtils;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.monitoring.config.models.Config;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import de.atb.context.monitoring.models.GitDataModel;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import de.atb.context.services.IAmIMonitoringDataRepositoryService;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import eu.smartclide.contexthandling.ServiceMain;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import static org.junit.Assert.assertEquals;

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
    private static final String ROUTING_KEY_MONITORING = "monitoring.git.commits";
    private static final String ROUTING_KEY_DLE = "dle.git.commits";
    private static final String QUEUE_PREFIX_DLE = "Fake-DLE";
    private static final String DATASOURCE_GIT = "datasource-git";
    private static final String MONITORING_CONFIG_FILE_NAME = "monitoring-config.xml";
    private static final String AMI_REPOSITORY_ID = "AmI-repository";

    private AmIMonitoringDataRepositoryServiceWrapper<GitDataModel> monitoringDataRepository;

    private Channel channel;

    // starts a new rabbitmq message broker in a docker container.
    // @Rule must be final.
    @Rule
    public final RabbitMQContainer container = new RabbitMQContainer(RABBITMQ_3_ALPINE).withAdminPassword(null);

    @Before
    public void setup() throws Exception {
        // setup message broker
        final String rabbitMQContainerHost = container.getHost();
        final Integer rabbitMQContainerAmqpPort = container.getAmqpPort();
        channel = MessageBrokerUtil.connectToTopicExchange(rabbitMQContainerHost, rabbitMQContainerAmqpPort, EXCHANGE_NAME);

        // setup fake DLE
        createFakeDleListener();

        // write dynamically allocated message broker host and port to monitoring config file
        final Path monitoringConfigFilePath = ContextPathUtils.getConfigDirPath().resolve(MONITORING_CONFIG_FILE_NAME);
        updateMessageBrokerDataSource(monitoringConfigFilePath, rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        // start service
        ServiceMain.startService();

        // get repository service
        monitoringDataRepository = ServiceManager.getLSWServiceContainer().stream()
                .filter(container -> container.getId().equals(AMI_REPOSITORY_ID))
                .findFirst()
                .map(container -> {
                    final IAmIMonitoringDataRepositoryService<GitDataModel> repositoryService =
                            ServiceManager.getWebservice(container);
                    return new AmIMonitoringDataRepositoryServiceWrapper<>(repositoryService);
                })
                .orElseThrow(() -> new RuntimeException("Could not setup AmI-repository!"));
    }

    @After
    public void tearDown() throws IOException, TimeoutException {
        monitoringDataRepository.shutdown();

        if (channel != null) {
            channel.close();
        }
    }

    @Test
    public void testDataRetrieval() throws IOException, InterruptedException {
        // wait for services to start
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
        MessageBrokerUtil.convertAndSendToTopic(channel, EXCHANGE_NAME, ROUTING_KEY_MONITORING, gitMessage);

        Thread.sleep(10000);

        // get the monitored data from the repository (the latest registry)
        final List<GitDataModel> data =
                monitoringDataRepository.getMonitoringData(ApplicationScenario.getInstance(), GitDataModel.class, 1);

        assertEquals(1, data.size());
        final List<GitMessage> gitMessages = data.get(0).getGitMessages();
        assertEquals(1, gitMessages.size());
        final GitMessage fromRepo = gitMessages.get(0);
        assertEquals(gitMessage.getTimestamp(), fromRepo.getTimestamp());
        assertEquals(gitMessage.getUser(), fromRepo.getUser());
        assertEquals(gitMessage.getRepository(), fromRepo.getRepository());
        assertEquals(gitMessage.getBranch(), fromRepo.getBranch());
        assertEquals(gitMessage.getNoOfCommitsInBranch(), fromRepo.getNoOfCommitsInBranch());
        assertEquals(gitMessage.getNoOfModifiedFiles(), fromRepo.getNoOfModifiedFiles());
        assertEquals(gitMessage.getNoOfPushesInBranch(), fromRepo.getNoOfPushesInBranch());
    }

    private void updateMessageBrokerDataSource(final Path monitoringConfig, final String host, final Integer port)
            throws Exception {
        final Persister persister = new Persister();
        final Config config = persister.read(Config.class, new File(monitoringConfig.toString()));
        final Map<String, String> optionsMap = config.getDataSource(DATASOURCE_GIT).getOptionsMap();
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerServer.getKeyName(), host);
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerPort.getKeyName(), port.toString());
        config.getDataSource(DATASOURCE_GIT).setOptions(optionsMap);
        persister.write(config, new File(monitoringConfig.toString()));
    }

    private void createFakeDleListener() throws IOException {
        MessageBrokerUtil.registerListenerOnTopic(
                channel,
                EXCHANGE_NAME,
                ROUTING_KEY_DLE,
                QUEUE_PREFIX_DLE,
                (t, m) -> logger.info("DLE received message: {}", new String(m.getBody(), StandardCharsets.UTF_8)),
                (t) -> logger.info("cancelled!")
        );
    }

}
