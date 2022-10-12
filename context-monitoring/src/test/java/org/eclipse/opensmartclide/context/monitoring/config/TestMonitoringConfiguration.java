package org.eclipse.opensmartclide.context.monitoring.config;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.DatabaseDataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.FilePairSystemDataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.FileSystemDataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.FileTripletSystemDataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.WebServiceDataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.opensmartclide.context.monitoring.config.models.DataSourceType;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;

/**
 * TestMonitoringConfiguration
 * 
 * @author scholze
 * @version $LastChangedRevision: 129 $
 * 
 */
public class TestMonitoringConfiguration {

	private static MonitoringConfiguration settings;
	private static List<DataSource> dataSources;
	private static List<Monitor> monitors;
	private static List<Interpreter> interpreters;

	@BeforeClass
	public static void beforeClass() {
        String absolutefilePath = Path.of("src", "test", "resources").toAbsolutePath().toString();
        settings = MonitoringConfiguration.getInstance("monitoring-config.xml", absolutefilePath);
		dataSources = settings.getDataSources();
		monitors = settings.getMonitors();
		interpreters = settings.getInterpreters();
	}

	@Test
	public final void shouldCheckDataSourcesFromConfig() {
		assertTrue("No Datasources defined!", dataSources.size() > 0);

		for (DataSource ds : dataSources) {
			if (ds.getType() == DataSourceType.FileSystem) {
				FileSystemDataSource fsds = ds.convertTo(ds.getType());
				assertTrue(fsds != null);
			} else if (ds.getType() == DataSourceType.FilePairSystem) {
				FilePairSystemDataSource fsds = ds.convertTo(ds.getType());
				assertTrue(fsds != null);
			} else if (ds.getType() == DataSourceType.FileTripletSystem) {
				FileTripletSystemDataSource fsds = ds.convertTo(ds.getType());
				assertTrue(fsds != null);
			} else if (ds.getType() == DataSourceType.Database) {
				DatabaseDataSource fsds = ds.convertTo(ds.getType());
				assertTrue(fsds != null);
			} else {
				WebServiceDataSource fsds = ds.convertTo(ds.getType());
				assertTrue(fsds != null);
			}
		}
	}

	@Test
	public final void shouldCheckMonitorsFromConfig() {
		assertTrue("No Monitors defined!", monitors.size() > 0);
		for (Monitor monitor : monitors) {
			boolean foundDsForMonitor = false;
			for (DataSource datasource : dataSources) {
				if (monitor.getDataSourceId().equals(datasource.getId())) {
					foundDsForMonitor = true;
					break;
				}
			}
			assertTrue("DataSource '" + monitor.getDataSourceId() + "' not configured!", foundDsForMonitor);

			boolean foundInterpreterForMonitor = false;
			for (Interpreter interpreter : interpreters) {
				if (monitor.getInterpreterId().equals(interpreter.getId())) {
					foundInterpreterForMonitor = true;
					break;
				}
			}
			assertTrue("DataSource '" + monitor.getDataSourceId() + "' not configured!", foundInterpreterForMonitor);
		}
	}
}
