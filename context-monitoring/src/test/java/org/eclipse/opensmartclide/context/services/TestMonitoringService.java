package org.eclipse.opensmartclide.context.services;

import org.apache.cxf.endpoint.Server;
import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * TestMonitoringService
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 */
public class TestMonitoringService {
    private static final Logger logger = LoggerFactory.getLogger(TestMonitoringService.class);
    private static Server server;
    private static IAmIMonitoringService service;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        final Path configDirPath = ContextPathUtils.getConfigDirPath();
        final String monitoringConfig = configDirPath.resolve("monitoring-config.xml").toString();

        AmIMonitoringConfiguration amiConfig = new AmIMonitoringConfiguration();
        amiConfig.setId("TEST_PES");
        amiConfig.setServiceConfiguration(readFile(monitoringConfig));

        server = ServiceManager.registerWebservice(AmIMonitoringService.class);
        service = ServiceManager.getWebservice(IAmIMonitoringService.class);
        service.configureService(amiConfig);
    }

    @AfterClass
    public static void afterClass() {
        ServiceManager.shutdownServiceAndEngine(server);
    }

    @Test
    public void shouldCheckRegisteredMonitoringServer() {
        assertNotNull("Server could not be registered!", server);
    }

    @Test
    public void shouldCheckRegisteredServiceInterface() {
        assertNotNull("IMonitoringService is null!", service);
    }

    @Test
    public void shouldStartService() {
        assertTrue(ServiceManager.isPingable(service));
        try {
            service.start();
        } catch (ContextFault e) {
            logger.error(e.getMessage());
            fail(e.getMessage());
        }
    }

    @Test
    public void shouldStopService() {
        try {
            service.stop();
        } catch (ContextFault e) {
            logger.error(e.getMessage(), e);
            fail("Service could not be stopped properly.");
        }
    }

    @Test
    public void shouldStopServer() {
        server.stop();
    }

    private static String readFile(String filename) throws IOException {
        File f = new File(filename);
        byte[] bytes = Files.readAllBytes(f.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
