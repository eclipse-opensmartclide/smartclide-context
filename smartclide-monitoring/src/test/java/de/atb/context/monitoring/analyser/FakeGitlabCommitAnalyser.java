package de.atb.context.monitoring.analyser;

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.GitlabCommitDataModel;
import de.atb.context.monitoring.models.GitlabCommitMessage;
import de.atb.context.monitoring.models.IWebService;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class FakeGitlabCommitAnalyser extends GitlabCommitAnalyser {

    public static final List<GitlabCommitMessage> FAKE_GITLAB_COMMIT_MESSAGES = List.of(
            GitlabCommitMessage.builder()
                    .user("John Doe")
                    .repository("smartclide/foo")
                    .branch("main")
                    .noOfModifiedFiles(29)
                    .timeSinceLastCommit(0)
                    .build(),
            GitlabCommitMessage.builder()
                    .user("John Doe")
                    .repository("smartclide/bar")
                    .branch("fix/4711/npe-in-constructor")
                    .noOfModifiedFiles(4)
                    .timeSinceLastCommit(766)
                    .build(),
            GitlabCommitMessage.builder()
                    .user("Jane Doe")
                    .repository("smartclide/bar")
                    .branch("main")
                    .noOfModifiedFiles(17)
                    .timeSinceLastCommit(98438)
                    .build()
    );

    private static final Logger logger = LoggerFactory.getLogger(GitlabCommitAnalyser.class);

    public FakeGitlabCommitAnalyser(final DataSource dataSource,
                                    final InterpreterConfiguration interpreterConfiguration,
                                    final Indexer indexer,
                                    final Document document,
                                    final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
    }

    @Override
    public List<GitlabCommitDataModel> analyseObject(final IWebService service) {
        final List<GitlabCommitMessage> gitlabCommitMessages = FAKE_GITLAB_COMMIT_MESSAGES;
        final GitlabCommitDataModel model = new GitlabCommitDataModel();
        model.setGitlabCommitMessages(gitlabCommitMessages);
        model.setMonitoredAt(new Date());
        logger.info("Analysed {} GitlabCommitMessages", gitlabCommitMessages.size());
        return List.of(model);
    }
}
