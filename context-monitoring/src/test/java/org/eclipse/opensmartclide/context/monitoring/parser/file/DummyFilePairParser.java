package org.eclipse.opensmartclide.context.monitoring.parser.file;

import java.io.File;

import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;

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

	/**
	 * (non-Javadoc)
	 *
	 * @see FilePairParser#parseObject(org.javatuples.Pair)
	 */
	@Override
	protected final boolean parseObject(final org.javatuples.Pair<File, File> file) {
		return true;
	}

}
