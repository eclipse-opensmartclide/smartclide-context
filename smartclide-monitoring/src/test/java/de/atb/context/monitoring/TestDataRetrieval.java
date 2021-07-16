package de.atb.context.monitoring;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.services.AmIMonitoringService;
import de.atb.context.services.IAmIMonitoringDataRepositoryService;
import de.atb.context.services.IAmIMonitoringService;
import de.atb.context.services.SWServiceContainer;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.cxf.endpoint.Server;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * TestMonitoringService
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 */


public class TestDataRetrieval {

	private static final Logger logger = LoggerFactory
			.getLogger(TestDataRetrieval.class);

	private static final String RABBITMQ_3_ALPINE = "rabbitmq:3-alpine";
	private static final String EXCHANGE_NAME = "monitoring";
	private static final String ROUTING_KEY = "commits";
	private Channel channel;

	private static Server server;
	private static IAmIMonitoringService service;
	private static AmIMonitoringDataRepositoryServiceWrapper monitoringDataRepository;
	private static IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> reposService;

	// starts a new rabbitmq message broker in a docker container.
	// @Rule must be final.
	@Rule
	public final RabbitMQContainer container = new RabbitMQContainer(RABBITMQ_3_ALPINE);

	@Before
	public void setup() throws IOException, TimeoutException {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        final Path configDir = Path.of("src", "test", "resources").toAbsolutePath();
        final String monitoringConfig = configDir.resolve("monitoring-config.xml").toString();
        final String serviceConfig = configDir.resolve("services-config.xml").toString();

		AmIMonitoringConfiguration amionfig = new AmIMonitoringConfiguration();
		amionfig.setId("TEST_GITMESSAGE");
		amionfig.setServiceConfiguration(readFile(monitoringConfig));

		SWServiceContainer serviceContainer = new SWServiceContainer(
				"AmI-repository", serviceConfig);
		ServiceManager.registerWebservice(serviceContainer);
		ServiceManager.getLSWServiceContainer().add(serviceContainer);

		for (SWServiceContainer container : ServiceManager.getLSWServiceContainer()) {
			if (container.getServerClass().toString()
					.contains("AmIMonitoringDataRepository")) {
				reposService = ServiceManager.getWebservice(container);
			}
		}
		monitoringDataRepository = new AmIMonitoringDataRepositoryServiceWrapper(reposService);

		server = ServiceManager.registerWebservice(AmIMonitoringService.class);
		service = ServiceManager.getWebservice(IAmIMonitoringService.class);
		service.configureService(amionfig);

		setupBroker();

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

	@Test
	public void testDataretrieval() throws IOException, InterruptedException {
		// start monitoring service (the repository is implicitly started from within the monitoring service)
		try {
			service.start();
		} catch (ContextFault e) {
			logger.error(e.getMessage(), e);
		}

		// wait a while (50s)
		try {
			Thread.sleep(50000L);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}

		for (int i = 0; i < 5; i++) {
			channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, ("foo" + i).getBytes(StandardCharsets.UTF_8));
			Thread.sleep(1000);
		}

		// get the monitored data from the repository (latest registry)
		//List<ProntoDataModel> data = monitoringDataRepository.getMonitoringData(ApplicationScenario.DIVERSITY_1, ProntoDataModel.class, 1);

		//Assert.assertTrue(data != null);
	}

	public void setupBroker() throws IOException, TimeoutException {
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
	}

	@After
	public void tearDown() throws IOException, TimeoutException {
		ServiceManager.shutdownServiceAndEngine(server);
		monitoringDataRepository.shutdown();

		if (channel != null) {
			channel.close();
		}
	}

}
