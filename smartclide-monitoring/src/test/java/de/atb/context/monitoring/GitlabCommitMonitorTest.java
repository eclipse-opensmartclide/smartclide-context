package de.atb.context.monitoring;

import com.rabbitmq.client.Channel;
import de.atb.context.common.ContextPathUtils;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.monitoring.config.models.Config;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSource;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSourceOptions;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.GitlabCommitDataModel;
import de.atb.context.monitoring.monitors.messagebroker.util.MessageBrokerUtil;
import de.atb.context.services.IAmIMonitoringDataRepositoryService;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import eu.smartclide.contexthandling.ServiceMain;
import org.apache.commons.lang3.StringUtils;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    // starts a new rabbitmq message broker in a docker container.
    // @Rule must be final.
    @Rule
    public final RabbitMQContainer container = new RabbitMQContainer(RABBITMQ_3_ALPINE).withAdminPassword(null);

    @Before
    public void setup() throws Exception {
        final String gitlabApiToken = System.getenv("SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN");
        if (StringUtils.isBlank(gitlabApiToken)) {
            throw new IllegalStateException("Did not find valid GitLab API token in \"SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN\" environment variable!");
        }

        // setup message broker
        final String rabbitMQContainerHost = container.getHost();
        final Integer rabbitMQContainerAmqpPort = container.getAmqpPort();

        // setup fake DLE
        fakeDleChannel = createFakeDleListener(rabbitMQContainerHost, rabbitMQContainerAmqpPort);

        // write dynamically allocated message broker host and port to monitoring config file
        final Path monitoringConfigFilePath = ContextPathUtils.getConfigDirPath().resolve(MONITORING_CONFIG_FILE_NAME);
        updateDataSource(monitoringConfigFilePath, rabbitMQContainerHost, rabbitMQContainerAmqpPort, gitlabApiToken);

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
    }

    @Test
    public void testDoMonitor() throws InterruptedException {
        // get the latest entry of monitored data from the repository
        List<GitlabCommitDataModel> data = List.of();
        int counter = 0;
        while (counter <= 5 && data.isEmpty()) {
            counter++;
            //noinspection BusyWait
            Thread.sleep(2000);
            data = monitoringDataRepository.getMonitoringData(ApplicationScenario.getInstance(), GitlabCommitDataModel.class, 1);
        }

        assertEquals(1, data.size());
        final List<GitMessage> gitMessages = data.get(0).getGitMessages();
        gitMessages.forEach(gitMessage -> {
            assertEquals("new commit", gitMessage.getHeader());
            assertEquals("info", gitMessage.getState());
            assertTrue(StringUtils.isNotBlank(gitMessage.getUser()));
            assertTrue(StringUtils.isNotBlank(gitMessage.getRepository()));
            assertTrue(StringUtils.isNotBlank(gitMessage.getBranch()));
            assertTrue(gitMessage.getTimeSinceLastCommit() > 0);
            assertTrue(gitMessage.getNoOfModifiedFiles() > 0);
        });
    }

    private void updateDataSource(final Path monitoringConfig,
                                  final String messageBrokerHost,
                                  final Integer messageBrokerPort,
                                  final String gitlabApiToken) throws Exception {
        final Persister persister = new Persister();
        final Config config = persister.read(Config.class, new File(monitoringConfig.toString()));
        final Map<String, String> optionsMap = config.getDataSource(DATASOURCE_GITLAB).getOptionsMap();
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerServer.getKeyName(), messageBrokerHost);
        optionsMap.put(MessageBrokerDataSourceOptions.MessageBrokerPort.getKeyName(), messageBrokerPort.toString());
        optionsMap.put(GitlabDataSource.ACCESS_TOKEN_OPTION.getKeyName(), gitlabApiToken);
        config.getDataSource(DATASOURCE_GITLAB).setOptions(optionsMap);
        persister.write(config, new File(monitoringConfig.toString()));
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
                (t, m) -> logger.info("DLE received message: {}", new String(m.getBody(), StandardCharsets.UTF_8)),
                (t) -> logger.info("cancelled!")
        );
        return channel;
    }
}
