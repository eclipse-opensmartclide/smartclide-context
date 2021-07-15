package de.atb.context.monitoring.monitors.webservice;

/*
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2020 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import de.atb.context.monitoring.analyser.webservice.WebServiceAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.WebServiceDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.models.IWebService;
import de.atb.context.monitoring.monitors.PeriodicScheduledExecutorThreadedMonitor;
import de.atb.context.monitoring.parser.webservice.WebServiceParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

/**
 * WebServiceMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class WebServiceMonitor extends PeriodicScheduledExecutorThreadedMonitor<IWebService, IMonitoringDataModel<?, ?>> {

    private final WebServiceDataSource dataSource;

    public WebServiceMonitor(final DataSource dataSource,
                             final Interpreter interpreter,
                             final Monitor monitor,
                             final Indexer indexer,
                             final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (dataSource.getType().equals(DataSourceType.WebService) && (dataSource instanceof WebServiceDataSource)) {
            this.dataSource = (WebServiceDataSource) dataSource;
        } else {
            throw new IllegalArgumentException("Given dataSource must be of type WebServiceDataSource!");
        }
        this.logger.info("Initializing WebServiceMonitor for uri: " + dataSource.getUri());
    }

    @Override
    protected WebServiceParser getParser(final InterpreterConfiguration setting) {
        return setting.createParser(this.dataSource, this.indexer, this.amiConfiguration);
    }

    @Override
    protected long getSchedulePeriod() {
        return this.dataSource.getInterval() != null ? this.dataSource.getInterval() : 15000L;
    }

    @Override
    protected InterpreterConfiguration getInterpreterConfiguration() {
        return this.interpreter.getConfiguration("monitoring-config.xml");
    }

    @Override
    protected long getScheduleInitialDelay() {
        return this.dataSource.getStartDelay() != null ? this.dataSource.getStartDelay() : getSchedulePeriod();
    }

    @Override
    protected void doMonitor(final InterpreterConfiguration setting) throws Exception {
        if (setting != null) {
            this.logger.debug("Handling URI " + this.dataSource.getUri() + "...");
            if ((this.dataSource.getUri() != null)) {
                WebServiceParser parser = getParser(setting);
                WebServiceAnalyser analyser = (WebServiceAnalyser) parser.getAnalyser();
                final IWebService webService = this.dataSource.toWebService();

                parseAndAnalyse(webService, parser, analyser);
            }
        } else {
            this.logger.debug("URI " + this.dataSource.getUri() + " will be ignored!");
        }
    }
}
