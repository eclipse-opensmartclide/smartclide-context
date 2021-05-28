package de.atb.context.monitoring.monitors.file;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import de.atb.context.common.exceptions.ConfigurationException;
import de.atb.context.monitoring.MetaMonitor;
import de.atb.context.monitoring.config.MonitoringConfiguration;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Index;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.FilePairSystemDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.monitors.ThreadedMonitor;

import java.io.File;
import java.nio.file.Path;

/**
 * TestFilePairSystemMonitor
 * 
 * @author scholze
 * @version $LastChangedRevision: 138 $
 * 
 */
public class TestFilePairSystemMonitor {

	private static MonitoringConfiguration config;
	private static Monitor monitor;
	private static DataSource datasource;
	private static Interpreter interpreter;
	private static Index index;
	private static Indexer indexer;
	private static ThreadedMonitor<?, ?> threadedMonitor;

	@BeforeClass
	public static void beforeClass() throws ConfigurationException {
        String absolutefilePath = Path.of("src", "test", "resources").toAbsolutePath().toString();

		config = MonitoringConfiguration.getInstance("monitoring-filepair-config.xml", absolutefilePath);

		monitor = config.getMonitor("monitor-dummy");
		Assert.assertTrue("No monitors 'monitor-dummy' specified!", config.getMonitor("monitor-dummy") != null);

		datasource = config.getDataSource(monitor.getDataSourceId());
		Assert.assertTrue("No datasource '" + monitor.getDataSourceId() + "' for monitor '" + monitor.getId() + "' found!",
				datasource != null);

		interpreter = config.getInterpreter(monitor.getInterpreterId());
		Assert.assertTrue("No interpreter '" + monitor.getInterpreterId() + "' for monitor '" + monitor.getId() + "' found!",
				interpreter != null);

		index = config.getIndex(monitor.getIndexId());
		Assert.assertTrue("No index '" + monitor.getIndexId() + "' for monitor '" + monitor.getId() + "' found!", index != null);
		indexer = new Indexer(index);
		indexer.dropIndex();

		AmIMonitoringConfiguration amiConfiguration = null;
		
		threadedMonitor = MetaMonitor.createThreadedMonitor(monitor, datasource, interpreter, indexer, amiConfiguration, null); //// FIXME: 17.10.2016
	}

	@Test
	public final void shouldGetPairFileExtensions1And2FromDataSource() {
		FilePairSystemDataSource ds = datasource.convertTo(DataSourceType.FilePairSystem);
		String extOne = ds.getFirstExtension();
		Assert.assertTrue("Extension from datasource for file one in Pair != 1", "1".equals(extOne));
		String extTwo = ds.getSecondExtension();
		Assert.assertTrue("Extension from datasource for file two in Pair != 2", "2".equals(extTwo));
	}

	@Test
	public final void shouldStartThreadedMonitorAndWaitFor2Seconds() throws ConfigurationException, InterruptedException {
		threadedMonitor.start();
		Thread.sleep(100L);
		Assert.assertTrue(threadedMonitor.isRunning());
		Thread.sleep(2000L);
		Assert.assertTrue(threadedMonitor.isRunning());
	}

	@AfterClass
	public static void afterClass() {
		if (threadedMonitor != null) {
			threadedMonitor.stop();
		}

		if (indexer != null) {
			indexer.close();
		}
	}

}
