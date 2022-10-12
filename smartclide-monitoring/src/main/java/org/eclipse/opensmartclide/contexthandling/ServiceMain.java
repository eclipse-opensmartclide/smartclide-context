package org.eclipse.opensmartclide.contexthandling;

/*
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.services.AmIMonitoringService;
import org.eclipse.opensmartclide.context.services.IAmIMonitoringDataRepositoryService;
import org.eclipse.opensmartclide.context.services.IAmIMonitoringService;
import org.eclipse.opensmartclide.context.services.SWServiceContainer;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMain.class);
    private static final String SMARTCLIDE = "SMARTCLIDE";
    private static final String MONITORING_CONFIG_FILE_NAME = "monitoring-config.xml";
    private static final String SERVICES_CONFIG_FILE_NAME = "services-config.xml";
    private static final String AMI_REPOSITORY_ID = "AmI-repository";

    public static void startService() {
        // start monitoring service (the repository is implicitly started from within the monitoring service)
        try {
            final Properties props = System.getProperties();
            props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

            final Path configDirPath = ContextPathUtils.getConfigDirPath();

            final Path serviceConfigFilePath = configDirPath.resolve(SERVICES_CONFIG_FILE_NAME);
            final SWServiceContainer serviceContainer =
                    new SWServiceContainer(AMI_REPOSITORY_ID, serviceConfigFilePath.toString());
            ServiceManager.getLSWServiceContainer().add(serviceContainer);
            ServiceManager.registerWebservice(serviceContainer);

            final SWServiceContainer repositoryServiceContainer = ServiceManager.getLSWServiceContainer().stream()
                    .filter(container -> container.getId().equals(AMI_REPOSITORY_ID))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Service could not be started, repository service is null"));

            final IAmIMonitoringDataRepositoryService<IMonitoringDataModel<?, ?>> repositoryService =
                    ServiceManager.getWebservice(repositoryServiceContainer);
            final AmIMonitoringDataRepositoryServiceWrapper<IMonitoringDataModel<?, ?>> monitoringDataRepository =
                    new AmIMonitoringDataRepositoryServiceWrapper<>(repositoryService);
            LOGGER.debug(monitoringDataRepository.ping());

            final Path monitoringConfigFilePath = configDirPath.resolve(MONITORING_CONFIG_FILE_NAME);
            final String monitoringConfig = Files.readString(monitoringConfigFilePath, StandardCharsets.UTF_8);
            final AmIMonitoringConfiguration amiConfig = new AmIMonitoringConfiguration();
            amiConfig.setId(SMARTCLIDE);
            amiConfig.setServiceConfiguration(monitoringConfig);

            ServiceManager.registerWebservice(AmIMonitoringService.class);
            final IAmIMonitoringService service = ServiceManager.getWebservice(IAmIMonitoringService.class);
            service.configureService(amiConfig);

            // TODO: add DleGitMonitorProgressListener as progress listener in GitMonitor
            service.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        startService();
    }
}
