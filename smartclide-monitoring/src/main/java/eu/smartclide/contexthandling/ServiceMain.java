package eu.smartclide.contexthandling;
/*-
 * #%L
 * smartclide-monitoring
 * %%
 * Copyright (C) 2017 - 2019 ATB
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

@SpringBootApplication
public class ServiceMain {
    private static final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    private static Server server;
    private static IAmIMonitoringService service;
    private static AmIMonitoringDataRepositoryServiceWrapper monitoringDataRepository;
    private static IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> reposService;

    private static void initialize() {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        org.apache.log4j.BasicConfigurator.configure();

        String smartclideConfigPath = "resources";

        // Environment Variable
        String smartclideHome = System.getenv("SMARTCLIDE_HOME");
        if (smartclideHome != null && Files.exists(Paths.get(smartclideHome))) {
            smartclideConfigPath = smartclideHome + File.separator + "config";
        // Linux config directory /var/lib/smartclide
        } else if (Files.exists(Paths.get("/var/lib/smartclide"))) {
            smartclideHome = "/var/lib/smartclide";
            smartclideConfigPath = smartclideHome + File.separator + "config";
        // Linux config directory /opt/smartclide/config
        } else if (Files.exists(Paths.get("/opt/smartclide"))) {
            smartclideHome = "/opt/smartclide";
            smartclideConfigPath = smartclideHome + File.separator + "config";
        // Windows COnfigu Directories
        } else if (Files.exists(Paths.get("C:\\ProgramData\\smartclide"))) {
            smartclideHome = "C:\\ProgramData\\smartclide";
            smartclideConfigPath = smartclideHome + File.separator + "config";
        }

        AmIMonitoringConfiguration amionfig = new AmIMonitoringConfiguration();
        amionfig.setId("SMARTCLIDE");
        amionfig.setServiceConfiguration(readFile(smartclideConfigPath + File.separator + "monitoring-config.xml"));

        File configFile = new File(
                smartclideConfigPath + File.separator + "services-config.xml");
        String filepath = configFile.getPath();
        SWServiceContainer serviceContainer = new SWServiceContainer(
                "AmI-repository", filepath);
        ServiceManager.getLSWServiceContainer().add(serviceContainer);
        server = ServiceManager.registerWebservice(serviceContainer);


        for (SWServiceContainer container : ServiceManager.getLSWServiceContainer()) {
            if (Objects.requireNonNull(container.getServerClass()).toString()
                    .contains("AmIMonitoringDataRepository")) {
                reposService = ServiceManager.getWebservice(container);
            }
        }

        server = ServiceManager.registerWebservice(AmIMonitoringService.class);
        service = ServiceManager.getWebservice(IAmIMonitoringService.class);
        service.configureService(amionfig);
    }

    private static String readFile(String filename) {
        File f = new File(filename);
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void startService() {
        // start monitoring service (the repository is implicitly started from within the monitoring service)
        try {
            service.start();
        } catch (ContextFault e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        initialize();

        if (reposService != null) {
            monitoringDataRepository = new AmIMonitoringDataRepositoryServiceWrapper(reposService);
            logger.debug(monitoringDataRepository.ping());
            startService();
        } else {
            logger.error("Service could not be started, repository service is null");
        }
    }
}
