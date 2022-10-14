package org.eclipse.opensmartclide.context.monitoring.analyser;

import org.apache.lucene.document.Document;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.GitlabCommitMessage;
import org.eclipse.opensmartclide.context.monitoring.models.IWebService;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
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
