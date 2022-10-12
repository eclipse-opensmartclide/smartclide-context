package org.eclipse.opensmartclide.context.monitoring.analyser.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.DummyMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;
import org.javatuples.Pair;

/**
 * DummyFilePairAnalyser
 *
 * @author scholze
 * @version $LastChangedRevision: 688 $
 */
public class DummyFilePairAnalyser extends FilePairAnalyser<IMonitoringDataModel<DummyMonitoringDataModel, ?>> {

	public DummyFilePairAnalyser(final DataSource dataSource, final InterpreterConfiguration interpreterConfiguration, final Indexer indexer,
                                 final Document document, final AmIMonitoringConfiguration amiConfiguration) {
		super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see FilePairAnalyser#analyseObject(org.javatuples.Pair)
	 */
	@Override
	public final List<IMonitoringDataModel<DummyMonitoringDataModel, ?>> analyseObject(final Pair<File, File> filePair) {
		return new ArrayList<IMonitoringDataModel<DummyMonitoringDataModel, ?>>();
	}

}
