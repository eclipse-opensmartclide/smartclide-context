package eu.smartclide.contexthandling.services;

import com.google.gson.*;
import de.atb.context.monitoring.models.GitMessage;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.InvalidPathException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;

public class GitRestCallService {

    private static final Logger logger = LoggerFactory.getLogger(GitRestCallService.class);

    private final String accessToken = "private_token=YFTwy2E727PbGHaiNx4e";
    private final String baseUri = "https://gitlab.atb-bremen.de/api/v4/projects/";
    private final String membership = "&membership=true";
    private final String pagination = "&per_page=100";
    private final String uriNormalEndPart = accessToken + membership + pagination;

    /**
     * generates gitMessages for given user
     * creates separate message for the commit in all the branches
     *
     * @return
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
                // get all commits for given branch
                JsonArray allCommitsInBranch = getAllCommitsForGivenBranch(projectId, branchName);
                Integer noOfCommitsInBranch = allCommitsInBranch.size();
                for (JsonElement commit : allCommitsInBranch) {
                    String commitId = commit.getAsJsonObject().get("id").getAsString();

                    GitMessage gitMessage = new GitMessage();
                    gitMessage.setTimestamp(commit.getAsJsonObject().get("created_at").getAsString());
                    gitMessage.setUser(commit.getAsJsonObject().get("author_name").getAsString());
                    gitMessage.setRepository(project.getAsJsonObject().get("path_with_namespace").getAsString());
                    gitMessage.setBranch(branchName);
                    gitMessage.setNoOfCommitsInBranch(noOfCommitsInBranch);

                    JsonArray commitDiff = getCommitDiff(projectId, commitId);
                    gitMessage.setNoOfModifiedFiles(commitDiff.size());

                    gitMessages.add(gitMessage);
                }
            }
        }
        return gitMessages;
    }

    private JsonArray getAllBranchesForGivenProject(String projectId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + "/repository/branches/?" + uriNormalEndPart));
    }

    private JsonArray getAllCommitsForGivenBranch(String projectId, String branchName) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + "/repository/commits/" + "?ref_name=" + branchName + "&" + uriNormalEndPart));
    }

    private JsonArray getCommitDiff(String projectId, String commitId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + "/repository/commits/" + commitId + "/diff/?" + uriNormalEndPart));
    }

    private JsonArray getUserProjects() {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + "?" + uriNormalEndPart));
    }

    /**
     * this method makes get call to gitlab server with given uri
     *
     * @param uri, string as uri for get api endpoint
     * @return, JsonArray as response
     */
    private HttpResponse<String> makeGetCallToGitlab(String uri) {

        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(5 * 60))
                .build();

        HttpRequest request = null;

        try {
            // create a GET request
            request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(uri))
                    .build();
        } catch (InvalidPathException e) {
            logger.error("HttpRequest exception", e);
        }

        HttpResponse<String> response = null;
        try {
            // receive response from Gitlab
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


        } catch (IOException | InterruptedException | InvalidPathException e) {
            logger.error("HTTP Client connection interruption exception", e);
        }
        return response;
    }

    private JsonArray parseHttpResponseToJsonArray(HttpResponse<String> response) {
        JsonArray returnJsonArray = new JsonArray();

        JsonParser parser = new JsonParser();
        try {
            Object object = (Object) parser.parse(response.body());
            returnJsonArray = (JsonArray) object;
        } catch (ParseException e) {
            logger.error("JSON Parse exception", e);
        }
        return returnJsonArray;
    }
}
