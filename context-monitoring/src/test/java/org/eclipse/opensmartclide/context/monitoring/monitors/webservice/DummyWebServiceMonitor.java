package org.eclipse.opensmartclide.context.monitoring.monitors.webservice;

import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.context.monitoring.monitors.ScheduledExecutorThreadedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DummyWebServiceMonitor
 *
 * @author scholze
 * @version $LastChangedRevision: 577 $
 */
public class DummyWebServiceMonitor extends WebServiceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DummyWebServiceMonitor.class);

    public DummyWebServiceMonitor(final DataSource dataSource,
                                  final Interpreter interpreter,
                                  final Monitor monitor,
                                  final Indexer indexer,
                                  final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
    }

    /**
     * (non-Javadoc)
     *
     * @see ScheduledExecutorThreadedMonitor#monitor()
     */
    @Override
    public final void monitor() throws Exception {
        logger.debug("Method monitor() called");
    }

}
