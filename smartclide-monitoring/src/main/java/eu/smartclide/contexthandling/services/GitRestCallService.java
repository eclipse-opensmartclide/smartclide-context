package eu.smartclide.contexthandling.services;

import com.google.gson.*;
import de.atb.context.monitoring.models.GitMessage;
import de.atb.context.monitoring.models.GitMessageHeader;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.InvalidPathException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;

public class GitRestCallService {

    private static final Logger logger = LoggerFactory.getLogger(GitRestCallService.class);

    private final String accessTokenParam = "private_token=YFTwy2E727PbGHaiNx4e";
    private final String baseUri = "https://gitlab.atb-bremen.de/api/v4/projects/";
    private final String membershipParam = "&membership=true";
    private final String paginationParam = "&per_page=100";
    private final String refNameParam = "&ref_name=";
    private final String sinceParam = "&since=2022-04-25T13:05:00";
    private final String uriParams = "?" + accessTokenParam + membershipParam + paginationParam;
    private final String uriPartForBranches = "/repository/branches/";
    private final String uriPartForCommits = "/repository/commits/";
    private final String uriPartForDiff = "/diff/";

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
                // get all new commits for given branch
                JsonArray newCommitsInBranch = getNewCommitsForGivenBranch(projectId, branchName);
                for (JsonElement newCommit : newCommitsInBranch) {
                    String newCommitId = newCommit.getAsJsonObject().get("id").getAsString();

                    GitMessage gitMessage = new GitMessage();
                    gitMessage.setHeader(GitMessageHeader.NEW_COMMIT.getHeader());
                    gitMessage.setState("info");
                    gitMessage.setUser(newCommit.getAsJsonObject().get("author_name").getAsString());
                    gitMessage.setRepository(project.getAsJsonObject().get("path_with_namespace").getAsString());
                    gitMessage.setBranch(branchName);

                    String lastCommitId = newCommit.getAsJsonObject().get("parent_ids").getAsString();
                    String newCommitCreation = newCommit.getAsJsonObject().get("created_at").getAsString();
                    String lastCommitCreation = getLastCommitsCreationDate(projectId, lastCommitId);
                    gitMessage.setTimeSinceLastCommit(calculateTimeSinceLastCommit(newCommitCreation, lastCommitCreation));

                    JsonArray newCommitDiff = getCommitDiff(projectId, newCommitId);
                    gitMessage.setNoOfModifiedFiles(newCommitDiff.size());

                    gitMessages.add(gitMessage);
                }
            }
        }
        return gitMessages;
    }

    private Integer calculateTimeSinceLastCommit(String newCommitDateStr, String lastCommitDateStr) {
        int difference = 0;
        try {
            Date newCommitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(newCommitDateStr);
            Date lastCommitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(lastCommitDateStr);
            difference = Math.toIntExact(Math.abs(newCommitDate.getTime() - lastCommitDate.getTime()));
        } catch (java.text.ParseException e) {
            logger.error("date to string parse error", e);
        }
        return difference /1000;
    }

    private JsonArray getAllBranchesForGivenProject(String projectId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId + uriPartForBranches + uriParams));
    }

    private JsonArray getCommitDiff(String projectId, String commitId) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + commitId + uriPartForDiff + uriParams));
    }

    private String getLastCommitsCreationDate(String projectId, String commitId) {
        String lastCommitsCreationDate;
        JsonObject lastCommit = parseHttpResponseToJsonObject(makeGetCallToGitlab(baseUri + projectId
            + uriPartForCommits + commitId + uriParams));
        lastCommitsCreationDate = lastCommit.getAsJsonObject().get("created_at").getAsString();
        return lastCommitsCreationDate;
    }

    private JsonArray getNewCommitsForGivenBranch(String projectId, String branchName) {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + projectId
                + uriPartForCommits + uriParams + refNameParam + branchName + sinceParam));
    }

    private JsonArray getUserProjects() {
        return parseHttpResponseToJsonArray(makeGetCallToGitlab(baseUri + uriParams));
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

    private JsonObject parseHttpResponseToJsonObject(HttpResponse<String> response) {
        JsonObject returnJsonObject = new JsonObject();
        JsonParser parser = new JsonParser();
        try {
            Object object = (Object) parser.parse(response.body());
            returnJsonObject = (JsonObject) object;
        } catch (ParseException e) {
            logger.error("JSON Parse exception", e);
        }
        return returnJsonObject;
    }
}
