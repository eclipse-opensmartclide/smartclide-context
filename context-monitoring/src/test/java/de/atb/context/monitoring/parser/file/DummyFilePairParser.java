package de.atb.context.monitoring.parser.file;

import java.io.File;

import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;

/**
 * DummyFilePairParser
 * 
 * @author scholze
 * @version $LastChangedRevision: 639 $
 * 
 */
public class DummyFilePairParser extends FilePairParser {

	public DummyFilePairParser(final DataSource dataSource,
			final InterpreterConfiguration interpreterConfiguration,
			final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
		super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see FilePairParser#parseObject
	 * (de.atb.context.common.util.Pair)
	 */
	@Override
	protected final boolean parseObject(
			final org.javatuples.Pair<File, File> file) {
		return true;
	}

}
