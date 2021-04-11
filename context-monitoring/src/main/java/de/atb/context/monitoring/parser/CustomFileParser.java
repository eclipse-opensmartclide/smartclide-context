package de.atb.context.monitoring.parser;

import de.atb.context.monitoring.analyser.IndexingAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IFileSystem;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.parser.file.FileParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

import java.io.File;

public class CustomFileParser extends FileParser {
    public CustomFileParser(DataSource dataSource, InterpreterConfiguration interpreterConfiguration, Indexer indexer, AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
    }

    @Override
    protected boolean parseObject(File file) {
        return true;
    }


}
