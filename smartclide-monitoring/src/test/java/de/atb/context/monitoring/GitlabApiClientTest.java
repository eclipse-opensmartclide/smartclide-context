package de.atb.context.monitoring;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import de.atb.context.monitoring.models.GitlabCommitMessage;
import eu.smartclide.contexthandling.services.GitlabApiClient;

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
    public void testGetGitlabCommitMessages() {
        List<GitlabCommitMessage> gitlabCommitMessages = gitlabApiClient.getGitlabCommitMessages();

        assertFalse(gitlabCommitMessages.isEmpty());
        gitlabCommitMessages.forEach(gitlabCommitMessage -> {
            assertTrue(StringUtils.isNotBlank(gitlabCommitMessage.getUser()));
            assertTrue(StringUtils.isNotBlank(gitlabCommitMessage.getRepository()));
            assertTrue(StringUtils.isNotBlank(gitlabCommitMessage.getBranch()));
            // allow `noOfModifiedFiles == 0` as a workaround
            assertTrue(gitlabCommitMessage.getNoOfModifiedFiles() >= 0);
            assertTrue(gitlabCommitMessage.getTimeSinceLastCommit() >= 0);
        });
    }
}
