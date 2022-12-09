package org.eclipse.opensmartclide.context.services;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.apache.cxf.endpoint.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * TestMonitoringService
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 */
public class TestDataRetrieval {

	private static final Logger logger = LoggerFactory.getLogger(TestDataRetrieval.class);

	private static Server server;
	private static IAmIMonitoringService service;
	private static AmIMonitoringDataRepositoryServiceWrapper monitoringDataRepository;
	private static IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> reposService;

	@BeforeClass
	public static void beforeClass() throws IOException {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        final Path configDirPath = ContextPathUtils.getConfigDirPath();
        final String monitoringConfig = configDirPath.resolve("monitoring-config.xml").toString();
        final String serviceConfig = configDirPath.resolve("services-config.xml").toString();

		AmIMonitoringConfiguration amionfig = new AmIMonitoringConfiguration();
		amionfig.setId("TEST_PES");
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
	}

	private static String readFile(String filename) throws IOException {
		File f = new File(filename);
        byte[] bytes = Files.readAllBytes(f.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
	}

	@Test
    @Ignore
	public void testDataretrieval() {
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

		// get the monitored data from the repository (latest registry)
		//List<ProntoDataModel> data = monitoringDataRepository.getMonitoringData(ApplicationScenario.DIVERSITY_1, ProntoDataModel.class, 1);

		//Assert.assertTrue(data != null);
	}

	@AfterClass
	public static void afterClass() {
		ServiceManager.shutdownServiceAndEngine(server);
		monitoringDataRepository.shutdown();
	}

}
