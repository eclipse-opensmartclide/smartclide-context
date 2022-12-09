package org.eclipse.opensmartclide.context.monitoring.config;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.Index;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;

public class TestGetInstanceForMonitoringConfiguration {

    private static Path configDirPath;
    private static final String CONFIG_FILE_NAME = "monitoring-config.xml";
    private static final String EXPECTED_DATASOURCE_ID = "datasource-dummy";
    private static final String EXPECTED_INDEX_ID = "index-dummy";
    private static final String EXPECTED_INTERPRETER_ID = "interpreter-dummy";
    private static final String EXPECTED_MONITOR_ID = "monitor-dummy";

    @BeforeClass
    public static void beforeClass() {
        configDirPath = ContextPathUtils.getConfigDirPath();
    }

    @Test
    public final void readConfigurationFileWithDefaultConfig() {
        final MonitoringConfiguration config = MonitoringConfiguration.getInstance();
        this.commonTests(config);
    }

    @Test
    public final void readConfigurationFileWithAmIConfig() throws IOException {
        final String monitoringConfig = configDirPath.resolve(CONFIG_FILE_NAME).toString();

        final AmIMonitoringConfiguration amiConfiguration = new AmIMonitoringConfiguration();
        amiConfiguration.setId("TEST_PES");
        amiConfiguration.setServiceConfiguration(readFile(monitoringConfig));

        final MonitoringConfiguration config = MonitoringConfiguration.getInstance(amiConfiguration);
        this.commonTests(config);
    }

    @Test
    public final void readConfigurationFileWithGivenFileConfig() {
        final MonitoringConfiguration config = MonitoringConfiguration.getInstance(CONFIG_FILE_NAME, configDirPath.toString());
        this.commonTests(config);
    }

    private void commonTests(MonitoringConfiguration config) {
        assertThat(config, is(notNullValue()));

        assertThat(config.getDataSources(), is(notNullValue()));
        final List<Monitor> monitors = config.getMonitors();
        assertFalse(monitors.isEmpty());

        final List<Index> indexes = config.getIndexes();
        assertFalse(indexes.isEmpty());

        final List<Interpreter> interpreters = config.getInterpreters();
        assertFalse(interpreters.isEmpty());

        for (Monitor monitor : config.getMonitors()) {
            Assert.assertEquals(EXPECTED_MONITOR_ID, monitor.getId());

            DataSource ds = config.getDataSource(monitor.getDataSourceId());
            Assert.assertEquals(EXPECTED_DATASOURCE_ID, ds.getId());

            Interpreter interpreter = config.getInterpreter(monitor.getInterpreterId());
            Assert.assertEquals(EXPECTED_INTERPRETER_ID, interpreter.getId());

            Index index = config.getIndex(monitor.getIndexId());
            Assert.assertEquals(EXPECTED_INDEX_ID, index.getId());
        }
    }

    private static String readFile(String filename) throws IOException {
        File f = new File(filename);
        byte[] bytes = Files.readAllBytes(f.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
