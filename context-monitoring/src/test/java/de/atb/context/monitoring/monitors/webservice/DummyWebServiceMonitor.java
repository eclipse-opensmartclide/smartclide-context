package de.atb.context.monitoring.monitors.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.index.Indexer;

/**
 * DummyWebServiceMonitor
 * 
 * @author scholze
 * @version $LastChangedRevision: 577 $
 * 
 */
public class DummyWebServiceMonitor extends WebServiceMonitor {

	private static final Logger logger = LoggerFactory.getLogger(DummyWebServiceMonitor.class);

	public DummyWebServiceMonitor(final DataSource dataSource, final Interpreter interpreter, final Monitor monitor, final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
		super(dataSource, interpreter, monitor, indexer, amiConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ThreadedMonitor#monitor()
	 */
	@Override
	public final void monitor() {
		logger.debug("Method monitor() called");
	}

}
