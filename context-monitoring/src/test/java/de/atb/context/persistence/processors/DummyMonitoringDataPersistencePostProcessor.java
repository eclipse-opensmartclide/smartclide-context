package de.atb.context.persistence.processors;

import de.atb.context.monitoring.models.DummyMonitoringDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DummyPersistencePostProcessor
 *
 * @author scholze
 * @version $LastChangedRevision: 699 $
 *
 */
public class DummyMonitoringDataPersistencePostProcessor extends BasePersistenceProcessor<DummyMonitoringDataModel> implements
        IPersistencePostProcessor<DummyMonitoringDataModel> {

	private final Logger logger = LoggerFactory.getLogger(DummyMonitoringDataPersistencePostProcessor.class);

	/**
	 * (non-Javadoc)
	 *
	 * @see de.atb.context.persistence.processors.IPersistenceProcessor#process(java.lang.Object)
	 */
	@Override
	public final DummyMonitoringDataModel process(DummyMonitoringDataModel object) {
		logger.debug("Starting Post-Processing " + object.getClass() + " with id " + object.getIdentifier());

		// heavy processing in here

		logger.debug("Finished Post-Processing " + object.getClass() + " with id " + object.getIdentifier());
		return object;
	}

}
