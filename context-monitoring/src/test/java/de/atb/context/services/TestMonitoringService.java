package de.atb.context.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.cxf.endpoint.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.atb.context.services.manager.ServiceManager;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import de.atb.context.services.faults.ContextFault;

/**
 * TestMonitoringService
 * 
 * @author scholze
 * @version $LastChangedRevision: 577 $
 * 
 */
public class TestMonitoringService {

	private static final Logger logger = LoggerFactory
			.getLogger(TestMonitoringService.class);

	private static Server server;
	private static IAmIMonitoringService service;

	@BeforeClass
	public static void beforeClass() {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        final Path configDir = Path.of("src", "test", "resources").toAbsolutePath();
        final String monitoringConfig = configDir.resolve("monitoring-config.xml").toString();
        final String serviceConfig = configDir.resolve("services-config.xml").toString();

        AmIMonitoringConfiguration amionfig = new AmIMonitoringConfiguration();
		amionfig.setId("TEST_PES");
		amionfig.setServiceConfiguration(readFile(monitoringConfig));

		server = ServiceManager.registerWebservice(AmIMonitoringService.class);
		service = ServiceManager.getWebservice(IAmIMonitoringService.class);
		service.configureService(amionfig);
	}

	@Test
	public void shouldCheckRegisteredMonitoringServer() {
		Assert.assertTrue("Server could not be registered!", server != null);
	}

	@Test
	public void shouldCheckRegisteredServiceInterface() {
		Assert.assertTrue("IMonitoringService is null!", service != null);
	}

	@Test
	public void shouldStartService() {
		Assert.assertTrue(ServiceManager.isPingable(service));
		try {
			service.start();
		} catch (ContextFault e) {
			logger.error(e.getMessage());
			Assert.fail(e.getMessage());
		}
/*		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}*/
	}

	@Test
	public void shouldStopService() {
		try {
			service.stop();
		} catch (ContextFault e) {
			logger.error(e.getMessage(), e);
			Assert.fail("Service could not be stopped properly.");
		}
	}

	@Test
	public void shouldStopServer() {
		server.stop();
	}

	private static String readFile(String filename) {
		File f = new File(filename);
		try {
			byte[] bytes = Files.readAllBytes(f.toPath());
			return new String(bytes, "UTF-8");
		} catch (FileNotFoundException e) {
			logger.debug(e.getMessage());
		} catch (IOException e) {
            logger.debug(e.getMessage());
		}
		return "";
	}

	@AfterClass
	public static void afterClass() {
		ServiceManager.shutdownServiceAndEngine(server);

	}

}
