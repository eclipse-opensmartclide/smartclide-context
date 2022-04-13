package de.atb.context.monitoring.config.models.datasources;

import de.atb.context.monitoring.config.MonitoringConfiguration;
import de.atb.context.monitoring.config.models.DataSource;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class GitlabDataSourceTest {

    @Test
    public void configFileShouldBeDeserializedToCorrectGitlabDataSource() {
        final String expectedId = "datasource-gitlab";
        final String expectedMonitor = "de.atb.context.monitoring.monitors.GitlabCommitMonitor";
        final String expectedUri = "https://gitlab.example.com/api/v4";
        final String expectedAccessToken = "foobar";
        final String expectedMessageBrokerHost = "localhost";
        final int expectedMessageBrokerPort = 5672;
        final String expectedUsername = "username";
        final String expectedPassword = "password";
        final String expectedMessageBrokerExchange = "smartclide-monitoring";
        final String expectedMessageBrokerTopicReceive = "monitoring.git.*";
        final String expectedMessageBrokerTopicSend = "dle.git.commits";
        URI temp = null;
        try {
            temp = this.getClass().getResource("/config/gitlab-monitoring").toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        final String configDirPath =
                Objects.requireNonNull(Paths.get(temp).toString());
        final String configFileName = "monitoring-config.xml";

        final MonitoringConfiguration config = MonitoringConfiguration.getInstance(configFileName, configDirPath);

        assertThat(config, is(notNullValue()));
        assertThat(config.getDataSources(), is(notNullValue()));
        assertThat(config.getDataSources().size(), equalTo(1));
        final DataSource dataSource = config.getDataSources().get(0);
        assertThat(dataSource, is(IsInstanceOf.instanceOf(GitlabDataSource.class)));
        final GitlabDataSource gitlabDataSource = (GitlabDataSource) dataSource;
        assertThat(gitlabDataSource.getId(), equalTo(expectedId));
        assertThat(gitlabDataSource.getMonitor(), equalTo(expectedMonitor));
        assertThat(gitlabDataSource.getUri(), equalTo(expectedUri));
        assertThat(gitlabDataSource.getGitLabAccessToken(), equalTo(expectedAccessToken));
        assertThat(gitlabDataSource.getMessageBrokerServer(), equalTo(expectedMessageBrokerHost));
        assertThat(gitlabDataSource.getMessageBrokerPort(), equalTo(expectedMessageBrokerPort));
        assertThat(gitlabDataSource.getUserName(), equalTo(expectedUsername));
        assertThat(gitlabDataSource.getPassword(), equalTo(expectedPassword));
        assertThat(gitlabDataSource.getExchange(), equalTo(expectedMessageBrokerExchange));
        assertThat(gitlabDataSource.getTopic(), equalTo(expectedMessageBrokerTopicReceive));
        assertThat(gitlabDataSource.getDleTopic(), equalTo(expectedMessageBrokerTopicSend));
    }
}
