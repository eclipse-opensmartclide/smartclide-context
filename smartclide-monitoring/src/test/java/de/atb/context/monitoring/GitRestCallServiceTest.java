package de.atb.context.monitoring;

import de.atb.context.monitoring.config.models.datasources.GitlabDataSourceTest;
import de.atb.context.monitoring.models.GitMessage;
import eu.smartclide.contexthandling.services.GitRestCallService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GitRestCallServiceTest {

    private GitRestCallService gitRestCallService;
    private static final Logger logger = LoggerFactory.getLogger(GitlabDataSourceTest.class);

    @Before
    public void setup() {
        final String gitlabApiToken = System.getenv("SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN");
        if (StringUtils.isBlank(gitlabApiToken)) {
            throw new IllegalStateException("Did not find valid GitLab API token in \"SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN\" environment variable!");
        }
        gitRestCallService = new GitRestCallService(gitlabApiToken);
    }

    @Test
    public void getUserProjects() {

        List<GitMessage> response = gitRestCallService.getGitMessages();
        // TODO: some assertions here..

        logger.info("response: " + response.size());
    }
}
