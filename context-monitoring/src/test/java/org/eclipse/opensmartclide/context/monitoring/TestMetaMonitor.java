package org.eclipse.opensmartclide.context.monitoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.context.common.exceptions.ConfigurationException;
import org.eclipse.opensmartclide.context.monitoring.config.MonitoringConfiguration;
import org.eclipse.opensmartclide.context.monitoring.config.models.Index;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.monitors.ThreadedMonitor;

/**
 * TestMetaMonitor
 *
 * @author scholze
 * @version $LastChangedRevision: 699 $
 *
 */
public class TestMetaMonitor {

	private static final Logger logger = LoggerFactory.getLogger(TestMetaMonitor.class);

	private static MonitoringConfiguration config;
	private static List<ThreadedMonitor<?, ?>> monitors = new ArrayList<>();

	@BeforeClass
	public static void beforeClass() {
        final Path configFilePath = ContextPathUtils.getConfigDirPath();
	    config = MonitoringConfiguration.getInstance("monitoring-config.xml", configFilePath.toString());
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
		for (ThreadedMonitor<?, ?> monitor : monitors) {
			if (monitor.getIndexer() != null) {
				monitor.getIndexer().close();
			}
			monitor.stop();
		}
	}

}
