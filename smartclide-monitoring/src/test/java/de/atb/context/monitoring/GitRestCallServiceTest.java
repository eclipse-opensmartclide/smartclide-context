package de.atb.context.monitoring;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.atb.context.monitoring.config.models.datasources.GitlabDataSourceTest;
import eu.smartclide.contexthandling.services.GitRestCallService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class GitRestCallServiceTest {

    private GitRestCallService gitRestCallService;

    @Before
    public void setup() {
        final String gitlabApiToken = System.getenv("SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN");
        if (StringUtils.isBlank(gitlabApiToken)) {
            throw new IllegalStateException("Did not find valid GitLab API token in \"SMARTCLIDE_CONTEXT_GITLAB_API_TOKEN\" environment variable!");
        }
        gitRestCallService = new GitRestCallService(gitlabApiToken);
    }

    @Test
    public void testGitServices() {
        final int expectedNumberOfProjects = 5;
        final String todoProjectId = "233";
        final int expectedNumberOfBranches = 2;
        final int expectedNumberOfCommits = 2;

        final String expectedBranchName = "main";
        final String expectedCommitId = "dc5b9dedf4d1c83ed6ce162196d411aa32e5e08b";
        final Integer expectedCommitTimeSinceLastCommit = 41996279;
        final Integer expectedCommitNoOfFilesChanged = 1;
        final String sinceParam = "&since=2020-01-26T13:05:00";

        JsonArray projects = gitRestCallService.getUserProjects();
        assertThat(projects.size(), equalTo(expectedNumberOfProjects));

        JsonArray branches = gitRestCallService.getAllBranchesForGivenProject(todoProjectId);
        assertThat(branches.size(), equalTo(expectedNumberOfBranches));

        JsonArray commits = gitRestCallService.getCommitsForGivenBranchAndSince(todoProjectId, expectedBranchName, sinceParam);
        assertThat(commits.size(), equalTo(expectedNumberOfCommits));

        JsonObject commit = gitRestCallService.getCommitById(todoProjectId, expectedCommitId);
        Integer timeSinceLastCommit = gitRestCallService.calculateTimeSinceLastCommit(todoProjectId, commit);
        assertThat(timeSinceLastCommit, equalTo(expectedCommitTimeSinceLastCommit));

        JsonArray newCommitDiff = gitRestCallService.getCommitDiff(todoProjectId, expectedCommitId);
        assertThat(newCommitDiff.size(), equalTo(expectedCommitNoOfFilesChanged));
    }
}
