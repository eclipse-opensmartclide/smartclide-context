package de.atb.context.monitoring;

import de.atb.context.monitoring.models.GitlabCommitMessage;
import eu.smartclide.contexthandling.services.GitlabApiClient;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class GitlabApiClientTest {

    private static final String gitlabBaseUri = "https://gitlab.atb-bremen.de";
    private GitlabApiClient gitlabApiClient;

    @Before
    public void setup() {
        final String gitlabApiToken = System.getenv("SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN");
        if (StringUtils.isBlank(gitlabApiToken)) {
            throw new IllegalStateException("Did not find valid GitLab API token in \"SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN\" environment variable!");
        }
        gitlabApiClient = new GitlabApiClient(gitlabApiToken, gitlabBaseUri);
    }

    @Test
    public void testGitServices() {

        List<GitlabCommitMessage> gitlabCommitMessages = gitlabApiClient.getGitlabCommitMessages();
        assertTrue(gitlabCommitMessages.size() > 0);
        gitlabCommitMessages.forEach(gitlabCommitMessage -> {
            assertTrue(StringUtils.isNotBlank(gitlabCommitMessage.getUser()));
            assertTrue(StringUtils.isNotBlank(gitlabCommitMessage.getRepository()));
            assertTrue(StringUtils.isNotBlank(gitlabCommitMessage.getBranch()));
            assertTrue(gitlabCommitMessage.getNoOfModifiedFiles() > 0);
            assertTrue(gitlabCommitMessage.getTimeSinceLastCommit() >= 0);
        });
    }
}
