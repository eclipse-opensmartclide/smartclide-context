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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.services.AmIMonitoringService;
import de.atb.context.services.IAmIMonitoringDataRepositoryService;
import de.atb.context.services.IAmIMonitoringService;
import de.atb.context.services.SWServiceContainer;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceMain {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    private static IAmIMonitoringService service;
    // TODO addDataModel as parameter as soon as this is defined for SmartCLIDE
    private static AmIMonitoringDataRepositoryServiceWrapper monitoringDataRepository;
    private static IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> reposService;

    private static void initialize() {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        org.apache.log4j.BasicConfigurator.configure();

        Path smartclideConfigPath = Path.of("resources");

        // Environment Variable
        String smartclideHome = System.getenv("SMARTCLIDE_HOME");
        if (smartclideHome != null && Files.isDirectory(Paths.get(smartclideHome))) {
            smartclideConfigPath = Path.of(smartclideHome, "config");
            // Linux config directory /var/lib/smartclide
        } else if (Files.isDirectory(Paths.get("/var/lib/smartclide"))) {
            smartclideConfigPath = Path.of("/var/lib/smartclide", "config");
            // Linux config directory /opt/smartclide/config
        } else if (Files.isDirectory(Paths.get("/opt/smartclide"))) {
            smartclideConfigPath = Path.of("/opt/smartclide", "config");
            // Windows Config Directories
        } else if (Files.isDirectory(Paths.get("C:\\ProgramData\\smartclide"))) {
            smartclideConfigPath = Path.of("C:\\ProgramData\\smartclide", "config");
        }

        AmIMonitoringConfiguration amiConfig = new AmIMonitoringConfiguration();
        amiConfig.setId("SMARTCLIDE");
        amiConfig.setServiceConfiguration(readFile(smartclideConfigPath.resolve("monitoring-config.xml").toString()));

        SWServiceContainer serviceContainer =
                new SWServiceContainer("AmI-repository", smartclideConfigPath.resolve("services-config.xml").toString());
        ServiceManager.getLSWServiceContainer().add(serviceContainer);
        ServiceManager.registerWebservice(serviceContainer);

        for (SWServiceContainer container : ServiceManager.getLSWServiceContainer()) {
            if (Objects.requireNonNull(container.getServerClass())
                    .toString()
                    .contains("AmIMonitoringDataRepository")) {
                reposService = ServiceManager.getWebservice(container);
            }
        }

        ServiceManager.registerWebservice(AmIMonitoringService.class);
        service = ServiceManager.getWebservice(IAmIMonitoringService.class);
        service.configureService(amiConfig);
    }

    private static String readFile(String filename) {
        try {
            return Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
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
