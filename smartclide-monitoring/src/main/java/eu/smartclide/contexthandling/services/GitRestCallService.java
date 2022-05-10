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

import com.google.gson.*;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.GitMessageHeader;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.InvalidPathException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;

public class GitRestCallService {

    private static final Logger logger = LoggerFactory.getLogger(GitRestCallService.class);

    private final String accessTokenParam;
    private final String baseUri = "https://gitlab.atb-bremen.de/api/v4/projects/";
    private final String membershipParam = "&membership=true";
    private final String paginationParam = "&per_page=100";
    private final String refNameParam = "&ref_name=";
    private final String sinceParam = "&since=2022-04-25T13:05:00"; // TODO change this based on requirement
    private final String uriParams;
    private final String uriPartForBranches = "/repository/branches/";
    private final String uriPartForCommits = "/repository/commits/";
    private final String uriPartForDiff = "/diff/";

    public GitRestCallService(String accessTokenParam) {
        this.accessTokenParam = "private_token=" + accessTokenParam;
        this.uriParams = "?" + this.accessTokenParam + this.membershipParam + this.paginationParam;
    }

    /**
     * generates gitMessages for given user
     * creates separate message for the commit in all the branches
     *
     * @return List<GitMessage>
     */
    public List<GitMessage> getGitMessages() {
        // first get all user projects
        JsonArray projects = getUserProjects();
        List<GitMessage> gitMessages = new LinkedList<>();
        for (JsonElement project : projects) {
            // get project id
            String projectId = project.getAsJsonObject().get("id").getAsString();
            // get all branches for given project, create a new GitMessage
            JsonArray branches = getAllBranchesForGivenProject(projectId);
            for (JsonElement branch : branches) {
                String branchName = branch.getAsJsonObject().get("name").getAsString();
                // get all commits for given branch and since
                JsonArray commitsInBranch = getCommitsForGivenBranchAndSince(projectId, branchName, sinceParam);
                for (JsonElement commit : commitsInBranch) {
                    String commitId = commit.getAsJsonObject().get("id").getAsString();

                    GitMessage gitMessage = new GitMessage();
                    gitMessage.setHeader(GitMessageHeader.NEW_COMMIT.getHeader());
                    gitMessage.setState("info");
                    gitMessage.setUser(commit.getAsJsonObject().get("author_name").getAsString());
                    gitMessage.setRepository(project.getAsJsonObject().get("path_with_namespace").getAsString());
                    gitMessage.setBranch(branchName);
                    gitMessage.setTimeSinceLastCommit(calculateTimeSinceLastCommit(projectId, commit.getAsJsonObject()));

                    JsonArray newCommitDiff = getCommitDiff(projectId, commitId);
                    gitMessage.setNoOfModifiedFiles(newCommitDiff.size());

                    gitMessages.add(gitMessage);
                }
            }
        }
        return gitMessages;
    }

    public Integer calculateTimeSinceLastCommit(String projectId, JsonObject newCommit) {
        int difference = 0;
        String lastCommitId = newCommit.get("parent_ids").getAsString();
        String newCommitCreationDateStr = newCommit.get("created_at").getAsString();
        JsonObject lastCommit = getCommitById(projectId, lastCommitId);
        String lastCommitCreationDateStr = lastCommit.getAsJsonObject().get("created_at").getAsString();

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            Date newCommitCreationDate = formatter.parse(newCommitCreationDateStr);
            Date lastCommitCreationDate = formatter.parse(lastCommitCreationDateStr);
            difference = Math.toIntExact(Math.abs(newCommitCreationDate.getTime() - lastCommitCreationDate.getTime()) / 1000);
        } catch (java.text.ParseException e) {
            logger.error("date to string parse error", e);
        }
        return difference;
    }

    public JsonArray getAllBranchesForGivenProject(String projectId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId + uriPartForBranches + uriParams));
    }

    public JsonObject getCommitById(String projectId, String commitId) {
        return parseHttpResponseToJsonObject(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + commitId + uriParams));
    }

    public JsonArray getCommitDiff(String projectId, String commitId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + commitId + uriPartForDiff + uriParams));
    }

    public JsonArray getCommitsForGivenBranchAndSince(String projectId, String branchName, String sinceParam) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + uriParams + refNameParam + branchName + sinceParam));
    }

    public JsonArray getUserProjects() {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + uriParams));
    }

    /**
     * this method makes get call to gitlab server with given uri
     *
     * @param uri as string
     * @return HttpResponse<String> as response
     */
    private HttpResponse<String> makeGetCallToGitlab(String uri) {

        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5 * 60))
                .build();

        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(uri)).build();

        HttpResponse<String> response = null;
        try {
            // receive response from Gitlab
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpStatus.SC_OK) {
                logger.error("Http response error:" + response.statusCode() + response.body());
                return null;
            }
        } catch (IOException | InterruptedException | InvalidPathException e) {
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
