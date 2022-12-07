package org.eclipse.opensmartclide.context.monitoring.config;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.monitoring.config.models.Index;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.services.TestDataRetrieval;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class TestMonitoringConfigurationModified {

    private static Path configDir;
    private static final Logger logger = LoggerFactory.getLogger(TestDataRetrieval.class);
    private static final String CONFIG_FILE_NAME = "monitoring-config.xml";

    @BeforeClass
    public static void beforeClass() {
        configDir = ContextPathUtils.getConfigDirPath();
    }

    @Test
    public final void readConfigurationFileWithDefaultConfig() {
        final MonitoringConfiguration config = MonitoringConfiguration.getInstance();
        this.commonTests(config);
    }

    @Test
    public final void readConfigurationFileWithAmIConfig() {
        final String monitoringConfig = configDir.resolve(CONFIG_FILE_NAME).toString();

        final AmIMonitoringConfiguration amiConfiguration = new AmIMonitoringConfiguration();
        amiConfiguration.setId("TEST_PES");
        amiConfiguration.setServiceConfiguration(readFile(monitoringConfig));

        final MonitoringConfiguration config = MonitoringConfiguration.getInstance(amiConfiguration);
        this.commonTests(config);
    }

    @Test
    public final void readConfigurationFileWithGivenFileConfig() {
        final MonitoringConfiguration config = MonitoringConfiguration.getInstance(CONFIG_FILE_NAME, configDir.toString());
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
    }

    private static String readFile(String filename) {
        File f = new File(filename);
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }
}
