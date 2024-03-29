package org.eclipse.opensmartclide.context.monitoring;

import com.rabbitmq.client.Channel;
import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.monitoring.analyser.FakeGitlabCommitAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.Config;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitMessage;
import org.eclipse.opensmartclide.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import org.eclipse.opensmartclide.context.services.IAmIMonitoringDataRepositoryService;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.eclipse.opensmartclide.contexthandling.ServiceMain;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * TestMonitoringService
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 */
public class GitlabCommitMonitorTest {

    private static final Logger logger = LoggerFactory.getLogger(GitlabCommitMonitorTest.class);

    private static final String RABBITMQ_3_ALPINE = "rabbitmq:3-alpine";
    private static final String QUEUE_NAME_DLE = "code_repo_recommendation_queue";
    private static final String DATASOURCE_GITLAB = "datasource-gitlab";
    private static final String MONITORING_CONFIG_FILE_NAME = "monitoring-config.xml";
    private static final String AMI_REPOSITORY_ID = "AmI-repository";

    private AmIMonitoringDataRepositoryServiceWrapper<GitlabCommitDataModel> monitoringDataRepository;
    private Channel fakeDleChannel;

    private int fakeDleNumberOfReceivedMessages;

    // starts a new rabbitmq message broker in a docker container.
    // @Rule must be final.
    @Rule
    public final RabbitMQContainer container = new RabbitMQContainer(RABBITMQ_3_ALPINE).withAdminPassword(null);

    @Before
    public void setup() throws Exception {
        // setup message broker
        final String rabbitMQContainerHost = container.getHost();
        final Integer rabbitMQContainerAmqpPort = container.getAmqpPort();

        // setup fake DLE
        fakeDleNumberOfReceivedMessages = 0;
        fakeDleChannel = createFakeDleListener(rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        // write dynamically allocated message broker host and port to monitoring config file
        updateDataSource(rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        // start service
        ServiceMain.startService();

        // get repository service
        monitoringDataRepository = ServiceManager.getLSWServiceContainer().stream()
                .filter(container -> container.getId().equals(AMI_REPOSITORY_ID))
                .findFirst()
                .map(container -> {
                    final IAmIMonitoringDataRepositoryService<GitlabCommitDataModel> repositoryService =
                            ServiceManager.getWebservice(container);
                    return new AmIMonitoringDataRepositoryServiceWrapper<>(repositoryService);
                })
                .orElseThrow(() -> new RuntimeException("Could not setup AmI-repository!"));
    }

    @After
    public void tearDown() throws IOException, TimeoutException {
        if (monitoringDataRepository != null) {
            monitoringDataRepository.shutdown();
        }

        if (fakeDleChannel != null) {
            fakeDleChannel.close();
        }
        fakeDleNumberOfReceivedMessages = 0;
    }

    @Test
    public void testDoMonitor() throws InterruptedException {
        // give services some time to start up and run monitor once
        Thread.sleep(15000);

        // get the latest entry of monitored data from the repository
        List<GitlabCommitDataModel> data = monitoringDataRepository.getMonitoringData(
                ApplicationScenario.getInstance(),
                GitlabCommitDataModel.class,
                1
        );
        assertEquals(1, data.size());

        final List<GitlabCommitMessage> gitlabCommitMessages = data.get(0).getGitlabCommitMessages();
        assertArrayEquals(
                FakeGitlabCommitAnalyser.FAKE_GITLAB_COMMIT_MESSAGES.toArray(),
                gitlabCommitMessages.toArray()
        );

        // assert that all GitLabCommitMessages have been received by fake DLE
        assertEquals(gitlabCommitMessages.size(), fakeDleNumberOfReceivedMessages);
    }

    private void updateDataSource(final String messageBrokerHost,
                                  final Integer messageBrokerPort) throws Exception {
        final Path testResourcesPath = Path.of(Objects.requireNonNull(this.getClass().getResource("/")).toURI());
        final Path originalConfigFilePath = testResourcesPath.resolve(MONITORING_CONFIG_FILE_NAME);
        final Path testConfigFilePath = testResourcesPath.resolve("config").resolve(MONITORING_CONFIG_FILE_NAME);
        Files.copy(originalConfigFilePath, testConfigFilePath, StandardCopyOption.REPLACE_EXISTING);
        final Path monitoringConfigPath = ContextPathUtils.getConfigDirPath().resolve(MONITORING_CONFIG_FILE_NAME);
        final Persister persister = new Persister();
        final Config config = persister.read(Config.class, new File(monitoringConfigPath.toString()));
        final Map<String, String> optionsMap = config.getDataSource(DATASOURCE_GITLAB).getOptionsMap();
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerServer.getKeyName(), messageBrokerHost);
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerPort.getKeyName(), messageBrokerPort.toString());
        config.getDataSource(DATASOURCE_GITLAB).setOptions(optionsMap);
        persister.write(config, new File(monitoringConfigPath.toString()));
    }

    private Channel createFakeDleListener(final String rabbitMQContainerHost, final Integer rabbitMQContainerAmqpPort)
            throws IOException, TimeoutException {
        final Channel channel = MessageBrokerUtil.connectToQueue(
                rabbitMQContainerHost,
                rabbitMQContainerAmqpPort,
                null,
                null,
                QUEUE_NAME_DLE,
                false
        );
        channel.basicConsume(
                QUEUE_NAME_DLE,
                true,
                (t, m) -> {
                    logger.info("DLE received message: {}", new String(m.getBody(), StandardCharsets.UTF_8));
                    fakeDleNumberOfReceivedMessages++;
                },
                (t) -> logger.info("cancelled!")
        );
        return channel;
    }
}
