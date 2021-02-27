package de.atb.context.persistence.processors;

import de.atb.context.monitoring.models.DummyMonitoringDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DummyPersistencePreProcessor
 * 
 * @author scholze
 * @version $LastChangedRevision: 699 $
 * 
 */
public class DummyMonitoringDataPersistencePreProcessor extends BasePersistenceProcessor<DummyMonitoringDataModel> implements
        IPersistencePreProcessor<DummyMonitoringDataModel> {

	private final Logger logger = LoggerFactory.getLogger(DummyMonitoringDataPersistencePreProcessor.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.context.persistence.processors.IPersistenceProcessor#process
	 * (java.lang.Object)
	 */
	@Override
	public final DummyMonitoringDataModel process(DummyMonitoringDataModel object) {
		logger.debug("Starting Pre-Processing " + object.getClass() + " with id " + object.getIdentifier());

		// heavy processing in here

		logger.debug("Finished Pre-Processing " + object.getClass() + " with id " + object.getIdentifier());
		return object;
	}

}
