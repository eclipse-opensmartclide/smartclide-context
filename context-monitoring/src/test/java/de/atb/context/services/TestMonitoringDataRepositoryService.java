/*
 * @(#)TestMonitoringDataRepositoryService.java
 *
 * $Id: TestMonitoringDataRepositoryService.java 686 2016-12-02 15:53:40Z scholze $
 *
 * $Rev:: 577                  $ 	last change revision
 * $Date:: 2012-04-11 12:49:28#$	last change date
 * $Author:: scholze             $	last change author
 *
 * Copyright 2011-15 Sebastian Scholze (ATB). All rights reserved.
 *
 */
package de.atb.context.services;

/*
 * #%L
 * ProSEco AmI Monitoring Core Services
 * %%
 * Copyright (C) 2015 ATB
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.TimeFrame;
import de.atb.context.monitoring.models.DummyMonitoringDataModel;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.TestMonitoringDataRepository;
import de.atb.context.persistence.processors.DummyMonitoringDataPersistencePostProcessor;
import de.atb.context.persistence.processors.DummyMonitoringDataPersistencePreProcessor;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.manager.ServiceManager;
import org.apache.cxf.endpoint.Server;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.ZoneId;
import java.util.*;

/**
 * TestMonitoringDataRepositoryService
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 *
 */
public class TestMonitoringDataRepositoryService {

    private static Server server;
    private static IAmIMonitoringDataRepositoryService<DummyMonitoringDataModel> service;
    private static final String postId = "myPostProcessorId";
    private static final String preId = "myPreProcessorId";
    private static File configFile;

    private static final Logger logger = LoggerFactory
        .getLogger(TestMonitoringDataRepositoryService.class);

    @BeforeClass
    public static void beforeClass() {
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "true");

        String absolutefilePath = new File("").getAbsolutePath();
        configFile = new File(
            absolutefilePath.concat(File.separator + "resources"+ File.separator + "services-config.xml"));
        String filepath = configFile.getPath();
        SWServiceContainer serviceContainer = new SWServiceContainer(
            "AmI-repository", filepath);
        server = ServiceManager.registerWebservice(serviceContainer);
        service = ServiceManager.getWebservice(serviceContainer);
    }

    @Test
    @Ignore
    public void shouldAddPersistenceProcessorsAndIgnoreAddingWithSameIdsAgain()
        throws ContextFault {
        Assert.assertTrue(service.addPersistencePostProcessor(
            ApplicationScenario.getInstance(), postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName()));
        Assert.assertTrue(service.addPersistencePreProcessor(
            ApplicationScenario.getInstance(), preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName()));

        Assert.assertFalse(service.addPersistencePostProcessor(
            ApplicationScenario.getInstance(), postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName()));
        Assert.assertFalse(service.addPersistencePreProcessor(
            ApplicationScenario.getInstance(), preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName()));
    }

    @Test(expected = ClassCastException.class)
    public void shouldNotAddProcessorIfInvalidClassName() throws Throwable {
        try {
            Assert.assertFalse(service.addPersistencePreProcessor(
                ApplicationScenario.getInstance(), "non-existent-id",
                Object.class.getName()));
        } catch (ContextFault e) {
            throw e.getCause();
        }
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldNotAddProcessor() throws Throwable {
        try {
            Assert.assertFalse(service.addPersistencePreProcessor(
                ApplicationScenario.getInstance(), "", ""));
        } catch (ContextFault e) {
            throw e.getCause();
        }
    }

    @Test
    @Ignore
    public void shouldPersistAndRetrieveMonitoringDataViaServiceById()
        throws ContextFault {
        Assert.assertTrue(ServiceManager.isPingable(service));
        service.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

        DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
        UUID id = dummy.getIdentifier();

        service.persist(dummy.toRdfString(), dummy.getClass().getName(),
            dummy.getApplicationScenario());
        String rdfString = service.getMonitoringData(
            ApplicationScenario.getInstance(),
            DummyMonitoringDataModel.class.getName(), id.toString());

        Assert.assertTrue(rdfString != null);
        DummyMonitoringDataModel dummy2 = RdfHelper.createMonitoringData(
            rdfString, DummyMonitoringDataModel.class);
        Assert.assertTrue(TestMonitoringDataRepository.validateModel(dummy,
            dummy2));
    }

    @Test
    @Ignore
    public void shouldGetDummyModelFromRepositoryByTimeFrameAndValidate()
        throws ContextFault {
        Assert.assertTrue(service.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL)));

        DummyMonitoringDataModel one = new DummyMonitoringDataModel();
        one.setMonitoredAt(getDateFromLastWeek().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        DummyMonitoringDataModel two = new DummyMonitoringDataModel();

        service.persist(one.toRdfString(),
            DummyMonitoringDataModel.class.getName(),
            one.getApplicationScenario());
        service.persist(two.toRdfString(),
            DummyMonitoringDataModel.class.getName(),
            two.getApplicationScenario());

        List<String> modelStringList = null;
        List<DummyMonitoringDataModel> models = null;
        DummyMonitoringDataModel model = null;

        DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
        TimeFrame timeFrame = new TimeFrame(null, new Date());

        // get models older than today
        modelStringList = service.getMonitoringData(
            ApplicationScenario.getInstance(),
            DummyMonitoringDataModel.class.getName(), timeFrame);
        models = new ArrayList<DummyMonitoringDataModel>();
        for (String rdf : modelStringList) {
            logger.debug(rdf);
            models.add(RdfHelper.createMonitoringData(rdf,
                DummyMonitoringDataModel.class));
        }
        Assert.assertTrue("Models is null or empty!", (models != null)
            && (models.size() > 0));
        model = models.get(0);
        Assert.assertTrue(TestMonitoringDataRepository.validateModel(model,
            dummy));

        // get models newer than 1970-1-1
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1970, 0, 1, 0, 0);
        timeFrame = new TimeFrame(cal.getTime(), null);
        modelStringList = service.getMonitoringData(
            ApplicationScenario.getInstance(),
            DummyMonitoringDataModel.class.getName(), timeFrame);
        models = new ArrayList<DummyMonitoringDataModel>();
        for (String rdf : modelStringList) {
            models.add(RdfHelper.createMonitoringData(rdf,
                DummyMonitoringDataModel.class));
        }
        Assert.assertTrue("Models is null or empty!", (models != null)
            && (models.size() > 0));
        model = models.get(0);
        Assert.assertTrue(TestMonitoringDataRepository.validateModel(model,
            dummy));

        Date startDate = new Date((one.getMonitoredAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) - 10000L);
        Date endDate = new Date();

        timeFrame = new TimeFrame(startDate, endDate);
        modelStringList = service.getMonitoringData(
            ApplicationScenario.getInstance(),
            DummyMonitoringDataModel.class.getName(), timeFrame);
        models = new ArrayList<DummyMonitoringDataModel>();
        for (String rdf : modelStringList) {
            models.add(RdfHelper.createMonitoringData(rdf,
                DummyMonitoringDataModel.class));
        }
        Assert.assertTrue("Models is null!", models != null);
        Assert.assertTrue("#Models " + models.size() + " != 2!",
            models.size() == 2);
        model = models.get(0);
        Assert.assertTrue(TestMonitoringDataRepository.validateModel(model,
            dummy));
    }

    @Test
    @Ignore
    public void shouldRemovePersistenceProcessorsAndIgnoreRemovalOfNotExistentOnes()
        throws ContextFault {
        service.addPersistencePostProcessor(ApplicationScenario.getInstance(),
            postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName());
        service.addPersistencePreProcessor(ApplicationScenario.getInstance(),
            preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName());

        Assert.assertTrue(service.removePersistencePostProcessor(
            ApplicationScenario.getInstance(), postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName()));
        Assert.assertTrue(service.removePersistencePreProcessor(
            ApplicationScenario.getInstance(), preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName()));

        Assert.assertFalse(service.removePersistencePostProcessor(
            ApplicationScenario.getInstance(), postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName()));
        Assert.assertFalse(service.removePersistencePreProcessor(
            ApplicationScenario.getInstance(), preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName()));
    }

    protected Date getDateFromLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) - 1);
        return cal.getTime();
    }

    @AfterClass
    public static void afterClass() {
        ServiceManager.shutdownServiceAndEngine(server);
    }

}
