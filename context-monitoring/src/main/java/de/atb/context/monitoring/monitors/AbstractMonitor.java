package de.atb.context.monitoring.monitors;

/*-
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.List;

import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.monitoring.events.MonitoringProgressListener;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractMonitor<P> implements MonitoringProgressListener<P, IMonitoringDataModel<?, ?>> {

    protected final Logger logger = LoggerFactory.getLogger(AbstractMonitor.class);

    protected AmIMonitoringDataRepositoryServiceWrapper amiRepository;

    public void setAmiRepository(AmIMonitoringDataRepositoryServiceWrapper amiRepository) {
        this.amiRepository = amiRepository;
    }

    @Override
    public void documentIndexed(final String indexId, final Document document) {
    }

    @Override
    public void documentParsed(final P parsed, final Document document) {
    }

    @Override
    public void documentAnalysed(final List<IMonitoringDataModel<?, ?>> analysed, final P parsed, final Document document) {
        if ((analysed != null) && (analysed.size() > 0)) {
            for (IMonitoringDataModel<?, ?> dataModel : analysed) {
                final ApplicationScenario applicationScenario = dataModel.getApplicationScenario();
                final String identifier = dataModel.getIdentifier();
                logger.info("Created monitoring data with id {} for {}", identifier, applicationScenario);
                try {
                    this.amiRepository.persist(dataModel);
                    logger.info("Persisted monitoring data with id {} for {}", identifier, applicationScenario);
                } catch (ContextFault e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        }
    }
}
