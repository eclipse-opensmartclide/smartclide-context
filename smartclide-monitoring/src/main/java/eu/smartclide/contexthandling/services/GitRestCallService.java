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

    final String baseUri = "https://gitlab.atb-bremen.de/api/v4/projects/";
    final String accessToken = "?private_token=YFTwy2E727PbGHaiNx4e";
    final String membership = "&membership=true";
    final String uriCommitStatesComponent = "with_stats=yes";
    final String uriPagination = "&per_page=100";
    final String uriNormalEndPart = accessToken + uriPagination + membership;

    final String uriProjectGetCall = baseUri + uriNormalEndPart;

    final String uriBranchEndPart = "/refs/" +
         accessToken + uriPagination + membership + "&type=all";

    /**
     * this method makes get call to gitlab server with given uri
     * @param uri, string as uri for get api endpoint
     * @return, JsonArray as response
     */
    private JsonArray makeGetCallToGitlab(String uri) {
        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = null;
        JsonArray returnJsonArray = new JsonArray();

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
            // parse response to JsonArray
            JsonParser parser = new JsonParser();
            try {
                Object object = (Object) parser.parse(response.body());
                returnJsonArray = (JsonArray) object;
            } catch (ParseException e) {
                logger.error("JSON Parse exception", e);
            }

        } catch (IOException | InterruptedException | InvalidPathException e) {
            logger.error("HTTP Client connection interruption exception", e);
        }
        return returnJsonArray;
    }

    /**
     * get user projects from Gitlab
     * @param
     * @return
     */
    public JsonArray getUserProjects() {
        return makeGetCallToGitlab(uriProjectGetCall);
    }

    /**
     * generates gitMessages for given user
     * creates separate message for the commit in all the branches
     * @return
     */
    public List<GitMessage> getGitMessages() {
        // first get all user projects
        JsonArray projects = getUserProjects();
        JsonArray allCommits = new JsonArray();
        List<GitMessage> gitMessages = new LinkedList<>();
        // for each project, get commit messages for all branches
        for (JsonElement project : projects) {
            // get project id
            String projectId = project.getAsJsonObject().get("id").getAsString();
            // get all commits for given project with uri
            allCommits = makeGetCallToGitlab(baseUri + projectId + "/repository/commits/" + uriNormalEndPart);
            for (JsonElement commit : allCommits) {
                String commitId = commit.getAsJsonObject().get("id").getAsString();
                // get all branches for given commit, create a new GitMessage
                JsonArray branches = getAllBranchesForGivenCommit(projectId, commitId);
                 for (JsonElement branch : branches) {
                    // create a new GitMessage
                    GitMessage gitMessage = new GitMessage();
                    // set message time stamp as commit created_at
                    gitMessage.setTimestamp(commit.getAsJsonObject().get("created_at").getAsString());
                    // set user as commit author_name
                    gitMessage.setUser(commit.getAsJsonObject().get("author_name").getAsString());
                    // set repository for git message as path_with_namespace, e.g. "smartclide/keycloak-client-ng"
                    gitMessage.setRepository(project.getAsJsonObject().get("path_with_namespace").getAsString());
                    // set branch as commit author_name
                    gitMessage.setUser(commit.getAsJsonObject().get("author_name").getAsString());
                    // set branch name
                    gitMessage.setBranch(branch.getAsJsonObject().get("name").getAsString());
                    // TODO: imrove this: get difference as file changed for commit
                    //gitMessage.setNoOfModifiedFiles(commit.getAsJsonObject().get("stats").getAsJsonObject().get("total").getAsInt());
                    // add GitMessage to list of messages
                    gitMessages.add(gitMessage);
                }


            }
        }
        return gitMessages;
    }

    public JsonArray getAllBranchesForGivenCommit(String projectId, String commitId) {
        return makeGetCallToGitlab(baseUri + projectId + "/repository/commits/" + commitId + uriBranchEndPart);
    }

    public JsonArray getCommitDiff(String projectId, String commitId) {
        return makeGetCallToGitlab(baseUri + projectId + "/repository/commits/" + commitId + "/" + "diff");
    }
}
