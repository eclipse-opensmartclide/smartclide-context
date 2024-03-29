package org.eclipse.opensmartclide.context.persistence;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.FileSystemDataSource;
import org.eclipse.opensmartclide.context.monitoring.models.DummyMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.persistence.monitoring.MonitoringDataRepository;
import org.eclipse.opensmartclide.context.persistence.processors.DummyMonitoringDataPersistencePostProcessor;
import org.eclipse.opensmartclide.context.persistence.processors.DummyMonitoringDataPersistencePreProcessor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * TestMonitoringDataRepository
 * 
 * @author scholze
 * @version $LastChangedRevision: 688 $
 * 
 */
public class TestMonitoringDataRepository {

	private static MonitoringDataRepository<DummyMonitoringDataModel> monitoringRepos;
	private static DummyMonitoringDataPersistencePostProcessor post;
	private static DummyMonitoringDataPersistencePreProcessor pre;

	private static final String EMPTY_MONITORINGDATA_MODEL_PATH = "models/empty_monitoringdata_model.rdf";
	private static final String DUMMY_MONITORINGDATA_MODEL_PATH = "models/dummy_monitoringdata_model.rdf";

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() {
		monitoringRepos = (MonitoringDataRepository<DummyMonitoringDataModel>) MonitoringDataRepository
				.getInstance();
		post = new DummyMonitoringDataPersistencePostProcessor();
		pre = new DummyMonitoringDataPersistencePreProcessor();
	}

	@Test(expected = NullPointerException.class)
	public final void shouldThrowNullPointerExceptionWhenBusinessCaseIsNull() {
		monitoringRepos.getMonitoringData((BusinessCase) null,
				DummyMonitoringDataModel.class, 15);
	}

	@Test(expected = NullPointerException.class)
	public final void shouldThrowNullPointerExceptionWhenClazzIsNull() {
		monitoringRepos.getMonitoringData(ApplicationScenario.getInstance(),
				null, 15);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void shouldThrowIllegalArgumentExceptionWhenCountIsNegative() {
		monitoringRepos.getMonitoringData(ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, -1);
	}

	@Test(expected = NullPointerException.class)
	public final void shouldThrowNullPointerExceptionWhenTimeFrameIsNull() {
		monitoringRepos.getMonitoringData(ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, (TimeFrame) null);
	}

	@Test
	public final void shouldCreateEmptyDefaultModelAndInitRepository() {
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));
		Model model = ModelFactory.createDefaultModel();
		monitoringRepos.createDefaultModel(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL), model, false);
		StmtIterator iterator = model.listStatements();
		Assert.assertFalse("Empty model has Statements in it!",
				iterator.hasNext());
	}

	@Test
	public final void shouldLoadEmptyDefaultModelAndInitRepository()
			throws URISyntaxException {
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));
		URL fileUri = Thread.currentThread().getContextClassLoader()
				.getResource(EMPTY_MONITORINGDATA_MODEL_PATH);
		Assert.assertTrue("File " + fileUri + " ("
				+ EMPTY_MONITORINGDATA_MODEL_PATH + ") could not be found",
				(fileUri != null) && (new File(fileUri.toURI()).exists()));
		Model model = monitoringRepos.createDefaultModel(Model.class,
				BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL), fileUri.toString(), false);
		StmtIterator iterator = model.listStatements();
		Assert.assertFalse("Empty model has Statements in it!",
				iterator.hasNext());
	}

	@Test
	public final void shouldUseDummyModelAndInitRepository()
			throws URISyntaxException {
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));
		URL fileUri = Thread.currentThread().getContextClassLoader()
				.getResource(DUMMY_MONITORINGDATA_MODEL_PATH);
		Assert.assertTrue("File " + fileUri + " ("
				+ DUMMY_MONITORINGDATA_MODEL_PATH + ") could not be found",
				(fileUri != null) && (new File(fileUri.toURI()).exists()));
		monitoringRepos.createDefaultModel(Model.class, BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL),
				fileUri.toString(), false);
	}

	@Test
	public final void shouldAddPersistenceProcessorsAndIgnoreAddingWithSameIdsAgain() {
		Assert.assertTrue(monitoringRepos.addPersistencePostProcessor(
				ApplicationScenario.getInstance(), post));
		Assert.assertTrue(monitoringRepos.addPersistencePreProcessor(
				ApplicationScenario.getInstance(), pre));

		Assert.assertFalse(monitoringRepos.addPersistencePostProcessor(
				ApplicationScenario.getInstance(), post));
		Assert.assertFalse(monitoringRepos.addPersistencePreProcessor(
				ApplicationScenario.getInstance(), pre));
	}

	@Test
    @Ignore
	public final void shouldGetDummyModelFromRepositoryByCountAndValidate() {
		DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
		List<DummyMonitoringDataModel> models = monitoringRepos
				.getMonitoringData(ApplicationScenario.getInstance(),
						DummyMonitoringDataModel.class, 1);
		Assert.assertTrue("Models is null or empty!", (models != null)
				&& (models.size() > 0) && (models.get(0) != null));
		IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> model = models
				.get(0);
		Assert.assertTrue(validateModel(model, dummy));
	}

	@Test
    @Ignore
	public final void shouldGetDummyModelFromRepositoryByTimeFrameAndValidate() {
		List<DummyMonitoringDataModel> models = null;
		DummyMonitoringDataModel model = null;

		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

		DummyMonitoringDataModel old = new DummyMonitoringDataModel();
		old.setMonitoredAt(getDateFromLastWeek());
		monitoringRepos.persist(old);

		DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
		monitoringRepos.persist(dummy);
		TimeFrame timeFrame = new TimeFrame(null, new Date());

		// get models older than today
		models = monitoringRepos.getMonitoringData(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
		Assert.assertTrue("Models is null or empty!", (models != null)
				&& (models.size() > 0));
		model = models.get(0);

		Assert.assertTrue(validateModel(model, dummy));

		// get models newer than 1970-1-1
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(1970, 0, 1, 0, 0);
		timeFrame = new TimeFrame(cal.getTime(), null);
		models = monitoringRepos.getMonitoringData(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
		Assert.assertTrue("Models is null or empty!", (models != null)
				&& (models.size() > 0));
		model = models.get(0);
		Assert.assertTrue(validateModel(model, dummy));

		Date startDate = old.getMonitoredAt();
		Date endDate = new Date();

		timeFrame = new TimeFrame(startDate, endDate);
		models = monitoringRepos.getMonitoringData(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
		Assert.assertTrue("Models is null!", models != null);
		Assert.assertTrue("#Models " + models.size() + " != 2!",
				models.size() == 2);
		model = models.get(0);
		Assert.assertTrue(validateModel(model, dummy));
	}

	@Test(expected = IllegalArgumentException.class)
	public final void shouldThrowIllegalArgumentExceptionBecauseTimeFrameHasNoStartAndEnd() {
		TimeFrame timeFrame = new TimeFrame(null, null);
		monitoringRepos.getMonitoringData(ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
	}

	@Test
    @Ignore
	public final void shouldGetDummyModelListWithAtMaxSpecifiedSize() { //TODO check
		List<DummyMonitoringDataModel> models;
		models = monitoringRepos.getMonitoringData(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, 3);
		Assert.assertTrue(models.size() + " > 3", models.size() <= 3);
		models = monitoringRepos.getMonitoringData(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, 2);
		Assert.assertTrue(models.size() + " > 2", models.size() <= 2);
		models = monitoringRepos.getMonitoringData(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, 1);
		Assert.assertTrue(models.size() + " > 1", models.size() <= 1);
	}

	@Test
    @Ignore
	public final void shouldPersistTwoDifferentDummyModelsInEmptyRepositoryAndRetrieveAndValidateThem() { //TODO check
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

		DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
		monitoringRepos.persist(dummy);

		List<DummyMonitoringDataModel> persistedDummies = monitoringRepos
				.getMonitoringData(ApplicationScenario.getInstance(),
						DummyMonitoringDataModel.class, 1);

		Assert.assertTrue(persistedDummies.size() == 1);
		Assert.assertTrue(validateModel(dummy, persistedDummies.get(0)));
	}

	@Test
    @Ignore
	public final void shouldPersistTwoDifferentDummyModelsInEmptyRepositoryAndRetrieveAndNotValidateThem()
			throws InterruptedException { //TODO check
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

		DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
		Thread.sleep(1000);
		DummyMonitoringDataModel dummy2 = new DummyMonitoringDataModel();
		dummy2.setDummyName("bladl");

		monitoringRepos.persist(dummy);
		monitoringRepos.persist(dummy2);

		List<DummyMonitoringDataModel> persistedDummies = monitoringRepos
				.getMonitoringData(ApplicationScenario.getInstance(),
						DummyMonitoringDataModel.class, 2);
		Assert.assertTrue("Could not persist and retrieve 2 dummies!",
				persistedDummies.size() == 2);
		IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> persistedDummy = persistedDummies
				.get(1);
		IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> persistedDummy2 = persistedDummies
				.get(0);
		Assert.assertFalse(validateModel(dummy, persistedDummy2));
		Assert.assertFalse(validateModel(dummy2, persistedDummy));
	}

	@Test
	public final void shouldPersist2ModelsAndRetrieveTheirIdsByCount()
			throws InterruptedException {
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

		DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
		Thread.sleep(1000);
		DummyMonitoringDataModel dummy2 = new DummyMonitoringDataModel();

		monitoringRepos.persist(dummy);
		monitoringRepos.persist(dummy2);
		String id1 = dummy.getIdentifier().toString();
		String id2 = dummy2.getIdentifier().toString();

		List<String> ids = monitoringRepos.getLastIds(
				ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, 3);
		Assert.assertTrue(
				"Could not persist 2 dummies and retrieve their ids (should be 2, but is "
						+ ids.size() + " !", ids.size() == 2);
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 1 = '" + id1 + "'",
				ids.contains(id1));
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 2 = '" + id2 + "'",
				ids.contains(id2));
	}

	@Test
	public final void shouldRemovePersistenceProcessorsAndIgnoreRemovalOfNotExistentOnes() {
		monitoringRepos.addPersistencePostProcessor(
				ApplicationScenario.getInstance(), post);
		monitoringRepos.addPersistencePreProcessor(
				ApplicationScenario.getInstance(), pre);
		Assert.assertTrue(monitoringRepos.removePersistencePostProcessor(
				ApplicationScenario.getInstance(), post.getId()));
		Assert.assertTrue(monitoringRepos.removePersistencePreProcessor(
				ApplicationScenario.getInstance(), pre.getId()));
		Assert.assertFalse(monitoringRepos.removePersistencePostProcessor(
				ApplicationScenario.getInstance(), post.getId()));
		Assert.assertFalse(monitoringRepos.removePersistencePreProcessor(
				ApplicationScenario.getInstance(), pre.getId()));
	}

	@Test
    @Ignore
	public final void shouldPersist2ModelsAndRetrieveTheirIdsByTimeFrame()
			throws InterruptedException {
		monitoringRepos.reset(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL));

		DummyMonitoringDataModel old = new DummyMonitoringDataModel();
		old.setMonitoredAt(getDateFromLastWeek());
		monitoringRepos.persist(old);
		String id1 = old.getIdentifier().toString();

		DummyMonitoringDataModel dummy = new DummyMonitoringDataModel();
		monitoringRepos.persist(dummy);
		String id2 = dummy.getIdentifier().toString();

		TimeFrame timeFrame = new TimeFrame(null, new Date());
		List<String> ids = new ArrayList<String>();

		// get models older than today
		ids = monitoringRepos.getLastIds(ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
		Assert.assertTrue("Ids are null or empty!",
				(ids != null) && (ids.size() > 0));
		Assert.assertTrue("Ids are <> 2 (=" + ids.size() + ")", ids.size() == 2);
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 1 = '" + id1 + "'",
				ids.contains(id1));
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 2 = '" + id2 + "'",
				ids.contains(id2));

		// get models newer than 1970-1-1
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.set(1970, 0, 1, 0, 0);
		timeFrame = new TimeFrame(cal.getTime(), null);
		ids = monitoringRepos.getLastIds(ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
		Assert.assertTrue("Ids are null or empty!",
				(ids != null) && (ids.size() > 0));
		Assert.assertTrue("Ids are <> 2 (=" + ids.size() + ")", ids.size() == 2);
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 1 = '" + id1 + "'",
				ids.contains(id1));
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 2 = '" + id2 + "'",
				ids.contains(id2));

		Date startDate = old.getMonitoredAt();
		Date endDate = new Date();

		timeFrame = new TimeFrame(startDate, endDate);
		ids = monitoringRepos.getLastIds(ApplicationScenario.getInstance(),
				DummyMonitoringDataModel.class, timeFrame);
		Assert.assertTrue("Ids are null or empty!",
				(ids != null) && (ids.size() > 0));
		Assert.assertTrue("Ids are <> 2 (=" + ids.size() + ")", ids.size() == 2);
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 1 = '" + id1 + "'",
				ids.contains(id1));
		Assert.assertTrue("List of returned Ids (" + ids
				+ ") did not contain id for dummy 2 = '" + id2 + "'",
				ids.contains(id2));
	}

	protected final Date getDateFromLastWeek() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) - 1);
		return cal.getTime();
	}

	public static boolean validateModel(
			IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> validate2,
			IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> against2) {
		DummyMonitoringDataModel validate = (DummyMonitoringDataModel) validate2;
		DummyMonitoringDataModel against = (DummyMonitoringDataModel) against2;

		if ((validate == null) && (against == null)) {
			return false;
		}
		if ((validate == null) && (against != null)) {
			return false;
		}
		if ((validate != null) && (against == null)) {
			return false;
		}

		boolean equal2 = validate.getDocumentIndexId().equals(
				against.getDocumentIndexId());
		boolean equal3 = validate.getDocumentUri().equals(
				against.getDocumentUri());
		boolean equal4 = validate.getImplementingClassName().equals(
				against.getImplementingClassName());
		boolean equal5 = validate.getMonitoringDataVersion().equals(
				against.getMonitoringDataVersion());
		boolean equal6 = validate.getDummyName().equals(against.getDummyName());
		boolean equal7 = validate.getDummyValue().equals(
				against.getDummyValue());

		return equal2 && equal3 && equal4 && equal5 && equal6
				&& equal7;
	}

	@AfterClass
	public static void afterClass() {
		monitoringRepos.shutdown();
	}
}
