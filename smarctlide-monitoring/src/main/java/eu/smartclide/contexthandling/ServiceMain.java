package eu.smartclide.contexthandling;
/*-
 * #%L
 * smarctlide-monitoring
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
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.apache.cxf.endpoint.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import de.atb.context.services.SWServiceContainer;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

        AmIMonitoringConfiguration amionfig = new AmIMonitoringConfiguration();
        String absolutefilePath = new File("").getAbsolutePath();
        amionfig.setId("TEST");
        amionfig.setServiceConfiguration(readFile((absolutefilePath.concat(File.separator
                + "resources" + File.separator + "monitoring-config.xml"))));

        File configFile = new File(
                absolutefilePath.concat(File.separator + "resources" + File.separator + "services-config.xml"));
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

        // wait a while (10s)
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        initialize();

        monitoringDataRepository = new AmIMonitoringDataRepositoryServiceWrapper(reposService);
        System.out.println(monitoringDataRepository.ping());
        startService();

/*        new Thread(() -> {
            while (true)
                try {
                    List<DataModel> data = monitoringDataRepository.getMonitoringData(ApplicationScenario.getInstance(BusinessCase.getInstance("DUMMY_SCENARIO", BusinessCase.NS_BASE_URL + "bc-dummy/")), ProntoDataModel.class, 1);
                    if (!data.isEmpty()) {
                        List<MonitoredOrdersInformation> ordersData =
                                data.get(0).getProntoMachineList().get(0).getProntoOrdersList();

                        List<MonitoredMixerStatusInformation> mixerData =
                                data.get(0).getProntoMachineList().get(0).getProntoMixerStatusList();

                        Thread.sleep(10000L);
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
        }).start();
*/
    }
}
