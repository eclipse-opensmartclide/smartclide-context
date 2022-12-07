package org.eclipse.opensmartclide.context.monitoring.config;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSourceType;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertTrue;

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
        final Path configFilePath = ContextPathUtils.getConfigDirPath();
        settings = MonitoringConfiguration.getInstance("monitoring-config.xml", configFilePath.toString());
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
