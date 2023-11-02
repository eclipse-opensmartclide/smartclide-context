package org.eclipse.opensmartclide.context.monitoring;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitMessage;
import org.eclipse.opensmartclide.contexthandling.services.GitlabApiClient;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GitlabApiClientTest {

    private static final String gitlabBaseUri = "https://gitlab.com";
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
