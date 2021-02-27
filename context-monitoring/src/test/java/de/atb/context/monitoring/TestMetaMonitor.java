package de.atb.context.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import de.atb.context.common.exceptions.ConfigurationException;
import de.atb.context.monitoring.config.MonitoringConfiguration;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Index;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.monitors.ThreadedMonitor;

/**
 * TestMetaMonitor
 * 
 * @author scholze
 * @version $LastChangedRevision: 699 $
 * 
 */
public class TestMetaMonitor {

	private final Logger logger = LoggerFactory.getLogger(TestMetaMonitor.class);

	private static MonitoringConfiguration config;
	private static List<ThreadedMonitor<?, ?>> monitors = new ArrayList<ThreadedMonitor<?, ?>>();

	@BeforeClass
	public static void beforeClass() {
		config = MonitoringConfiguration.getInstance();
	}

	@Test
	public final void shouldCheckConfigForMonitorsAndSettings() {
		Assert.assertTrue("No monitors configured!", (config.getMonitors() != null) && (config.getMonitors().size() > 0));
		String val = "";
		for (Monitor monitor : config.getMonitors()) {
			DataSource ds = config.getDataSource(monitor.getDataSourceId());
			val = String.valueOf(ds.getId());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));
			val = String.valueOf(ds.getMonitor());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));
			val = String.valueOf(ds.getUri());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));
			val = String.valueOf(ds.getUri());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));

			Interpreter interpreter = config.getInterpreter(monitor.getInterpreterId());
			val = String.valueOf(interpreter.getId());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));
			val = String.valueOf(interpreter.getFilenameFilter());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));

			Index index = config.getIndex(monitor.getIndexId());
			val = String.valueOf(index.getId());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));
			val = String.valueOf(index.getLocation());
			Assert.assertTrue((!val.equals("null") && (val.trim().length() > 0)));
		}
	}

	@Test
	public final void shouldCreateThreadedMonitorsFromConfig() {
		for (Monitor monitor : config.getMonitors()) {
			DataSource ds = config.getDataSource(monitor.getDataSourceId());
			Interpreter interpreter = config.getInterpreter(monitor.getInterpreterId());
			Index index = config.getIndex(monitor.getIndexId());
			AmIMonitoringConfiguration amiConfiguration = null;
			try {
				// if (monitor.getDataSource().getType() !=
				// DataSourceType.WebService) {
				Indexer indexer = new Indexer(index);
				ThreadedMonitor<?, ?> tm = MetaMonitor.createThreadedMonitor(monitor, ds, interpreter, indexer, amiConfiguration, null); //// FIXME: 17.10.2016
				monitors.add(tm);
				// }
			} catch (ConfigurationException e) {
				logger.error(e.getMessage(), e);
				Assert.fail(e.getMessage());
			}
		}
	}

	@AfterClass
	public static void afterClass() {
		for (ThreadedMonitor<?, ?> monitor : monitors) {
			if (monitor.getIndexer() != null) {
				monitor.getIndexer().close();
			}
			monitor.stop();
		}
	}

}
