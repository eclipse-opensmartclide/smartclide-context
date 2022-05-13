package eu.smartclide.contexthandling.services;

/*-
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2015 - 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.atb.context.monitoring.models.GitlabCommitMessage;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;

public class GitlabApiClient {

    private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Logger logger = LoggerFactory.getLogger(GitlabApiClient.class);

    private static final String membershipParam = "&membership=true";
    private static final String paginationParam = "&per_page=100";
    private static final String refNameParam = "&ref_name=";
    private static final String sinceDate = "2022-04-25T13:05:00"; // TODO change this based on requirement
    private static final String sinceParam = "&since=" + sinceDate; // TODO change this based on requirement
    private static final String uriPartForBranches = "/repository/branches/";
    private static final String uriPartForCommits = "/repository/commits/";
    private static final String uriPartForDiff = "/diff/";
    private static final String uriPartForProjects = "/api/v4/projects/";
    private final String baseUri;
    private final String uriParams;

    public GitlabApiClient(String accessToken, String gitlabBaseUri) {
        String accessTokenParam = "private_token=" + accessToken;
        this.uriParams = "?" + accessTokenParam + membershipParam + paginationParam;
        this.baseUri = gitlabBaseUri + uriPartForProjects;
    }

    /**
     * generates gitMessages for given user
     * creates separate message for the commit in all the branches
     *
     * @return List<GitMessage>
     */
    public List<GitlabCommitMessage> getGitlabCommitMessages() {
        // first get all user projects
        JsonArray projects = getUserProjects();
        logger.info("Total {} user projects are found", projects.size());
        List<GitlabCommitMessage> gitlabCommitMessages = new LinkedList<>();
        for (JsonElement project : projects) {
            JsonObject projectJsonObject = project.getAsJsonObject();
            // get project id
            String projectId = projectJsonObject.get("id").getAsString();
            // get all branches for given project, create a new GitMessage
            JsonArray branches = getAllBranchesForGivenProject(projectId);
            logger.info("Total {} branches for project with given ID {} are found", branches.size(), projectId);

            for (JsonElement branch : branches) {
                String branchName = branch.getAsJsonObject().get("name").getAsString();
                // get all commits for given branch
                JsonArray commitsInBranch = getCommitsForGivenBranch(projectId, branchName);
                logger.info("Total {} commits are found in a given branch with name '{}' since {}",
                        commitsInBranch.size(), branchName, sinceDate);

                for (JsonElement commit : commitsInBranch) {
                    JsonObject commitJsonObject = commit.getAsJsonObject();
                    String commitId = commitJsonObject.get("id").getAsString();

                    GitlabCommitMessage gitlabCommitMessage = new GitlabCommitMessage();
                    gitlabCommitMessage.setUser(commitJsonObject.get("author_name").getAsString());
                    gitlabCommitMessage.setRepository(projectJsonObject.get("path_with_namespace").getAsString());
                    gitlabCommitMessage.setBranch(branchName);
                    gitlabCommitMessage.setTimeSinceLastCommit(calculateTimeSinceLastCommit(projectId, commitJsonObject));
                    JsonArray newCommitDiff = getCommitDiff(projectId, commitId);
                    gitlabCommitMessage.setNoOfModifiedFiles(newCommitDiff.size());
                    gitlabCommitMessages.add(gitlabCommitMessage);
                    logger.info("Git commit message: " + gitlabCommitMessage);
                }
            }
        }
        return gitlabCommitMessages;
    }

    private Integer calculateTimeSinceLastCommit(String projectId, JsonObject commit) {
        int difference = 0;
        // check if parent id exists for given commit
        if (commit.get("parent_ids").getAsJsonArray().size() > 0) {
            String parentCommitId = commit.get("parent_ids").getAsString();
            String commitCreationDateStr = commit.get("created_at").getAsString();
            JsonObject parentCommit = getCommitById(projectId, parentCommitId);
            String parentCommitCreationDateStr = parentCommit.getAsJsonObject().get("created_at").getAsString();

            try {
                Date commitCreationDate = formatter.parse(commitCreationDateStr);
                Date parentCommitCreationDate = formatter.parse(parentCommitCreationDateStr);
                difference = Math.toIntExact(
                        Math.abs(commitCreationDate.getTime() - parentCommitCreationDate.getTime()) / 1000);
            } catch (java.text.ParseException e) {
                logger.error("date to string parse error", e);
            }
        } else {
            logger.info("No previous commit exist for given commit with ID: " + commit.get("id").getAsString());
        }
        return difference;
    }

    private JsonArray getAllBranchesForGivenProject(String projectId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId + uriPartForBranches + uriParams));
    }

    private JsonObject getCommitById(String projectId, String commitId) {
        return parseHttpResponseToJsonObject(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + commitId + uriParams));
    }

    private JsonArray getCommitDiff(String projectId, String commitId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + commitId + uriPartForDiff + uriParams));
    }

    private JsonArray getCommitsForGivenBranch(String projectId, String branchName) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + uriParams + refNameParam + branchName + sinceParam));
    }

    private JsonArray getUserProjects() {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + uriParams));
    }

    /**
     * this method makes get call to gitlab server with given uri
     *
     * @param uri as string
     * @return HttpResponse<String> as response
     */
    private HttpResponse<String> makeGetCallToGitlab(String uri) {

        HttpResponse<String> response = null;
        try {
            final HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofMinutes(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();

            // receive response from Gitlab
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatus.SC_OK) {
                logger.error("Http response error:" + response.statusCode() + response.body());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("HTTP Client connection interruption exception", e);
        }
        return response;
    }

    private JsonArray parseHttpResponseToJsonArray(HttpResponse<String> response) {
        if (response != null) {
            return JsonParser.parseString(response.body()).getAsJsonArray();
        }
        return new JsonArray();
    }

    private JsonObject parseHttpResponseToJsonObject(HttpResponse<String> response) {
        if (response != null) {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        }
        return new JsonObject();
    }
}
