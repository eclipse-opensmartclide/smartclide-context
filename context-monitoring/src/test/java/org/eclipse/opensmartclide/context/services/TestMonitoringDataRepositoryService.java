package org.eclipse.opensmartclide.context.services;

import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.monitoring.models.DummyMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.rdf.RdfHelper;
import org.eclipse.opensmartclide.context.persistence.TestMonitoringDataRepository;
import org.eclipse.opensmartclide.context.persistence.processors.DummyMonitoringDataPersistencePostProcessor;
import org.eclipse.opensmartclide.context.persistence.processors.DummyMonitoringDataPersistencePreProcessor;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.apache.cxf.endpoint.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

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
            absolutefilePath.concat(File.separator + "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "config" + File.separator + "services-config.xml"));
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
        ApplicationScenario appScenario = ApplicationScenario.getInstance();
        Assert.assertTrue(service.addPersistencePostProcessor(
            appScenario, postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName()));
        Assert.assertTrue(service.addPersistencePreProcessor(
            appScenario, preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName()));

        Assert.assertFalse(service.addPersistencePostProcessor(
            appScenario, postId,
            DummyMonitoringDataPersistencePostProcessor.class.getName()));
        Assert.assertFalse(service.addPersistencePreProcessor(
            appScenario, preId,
            DummyMonitoringDataPersistencePreProcessor.class.getName()));
    }

    @Test
    public void shouldNotAddProcessorIfInvalidClassName() throws Throwable {
        try {
            service.addPersistencePreProcessor(
                ApplicationScenario.getInstance(), "non-existent-id",
                Object.class.getName());
        } catch (ContextFault e) {
            logger.info("ContextFault as expected");
            Assert.assertTrue(true);
        }
    }

    @Test
    public void shouldNotAddProcessor() throws Throwable {
        try {
            service.addPersistencePreProcessor(
                ApplicationScenario.getInstance(), "", "");
        } catch (ContextFault e) {
            logger.info("ContextFault as expected");
            Assert.assertTrue(true);
        }
    }

    @Test
    @Ignore
    public void shouldPersistAndRetrieveMonitoringDataViaServiceById()
        throws ContextFault {
        Assert.assertTrue(ServiceManager.isPingable(service));
        service.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

        DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
        String id = dummy.getIdentifier();

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
        one.setMonitoredAt(getDateFromLastWeek());
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

        Date startDate = one.getMonitoredAt();
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
