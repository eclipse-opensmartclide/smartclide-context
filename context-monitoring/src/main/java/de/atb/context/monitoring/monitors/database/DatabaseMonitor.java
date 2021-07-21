package de.atb.context.monitoring.monitors.database;

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


import de.atb.context.monitoring.analyser.database.DatabaseAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.config.models.Interpreter;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.Monitor;
import de.atb.context.monitoring.config.models.datasources.DatabaseDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.IDatabase;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.monitors.PeriodicScheduledExecutorThreadedMonitor;
import de.atb.context.monitoring.parser.database.DatabaseParser;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

/**
 * DatabaseMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public class DatabaseMonitor extends PeriodicScheduledExecutorThreadedMonitor<IDatabase, IMonitoringDataModel<?, ?>> {

    private final DatabaseDataSource dataSource;

    public DatabaseMonitor(final DataSource dataSource,
                           final Interpreter interpreter,
                           final Monitor monitor,
                           final Indexer indexer,
                           final AmIMonitoringConfiguration configuration) {
        super(dataSource, interpreter, monitor, indexer, configuration);
        if (dataSource.getType().equals(DataSourceType.Database) && (dataSource instanceof DatabaseDataSource)) {
            this.dataSource = (DatabaseDataSource) dataSource;
        } else {
            throw new IllegalArgumentException("Given dataSource must be of type DatabaseDataSource!");
        }
        this.logger.info("Initializing DatabaseMonitor for uri: " + dataSource.getUri());
    }

    @Override
    protected DatabaseParser getParser(final InterpreterConfiguration setting) {
        return setting.createParser(this.dataSource, this.indexer, this.amiConfiguration);
    }

    @Override
    protected long getSchedulePeriod() {
        return this.dataSource.getInterval() != null ? this.dataSource.getInterval() : 15000L;
    }

    @Override
    protected long getScheduleInitialDelay() {
        return this.dataSource.getStartDelay() != null ? this.dataSource.getStartDelay() : getSchedulePeriod();
    }

    @Override
    protected InterpreterConfiguration getInterpreterConfiguration() {
        return this.interpreter.getConfiguration("monitoring-config.xml");
    }

    @Override
    protected void doMonitor(final InterpreterConfiguration setting) throws Exception {
        if (setting != null) {
            this.logger.debug("Handling URI " + this.dataSource.getUri() + "...");
            if ((this.dataSource.getUri() != null)) {
                DatabaseParser parser = getParser(setting);
                DatabaseAnalyser analyser = (DatabaseAnalyser) parser.getAnalyser();
                final IDatabase database = this.dataSource.toDatabase();

                parseAndAnalyse(database, parser, analyser);
            }
        } else {
            this.logger.debug("URI " + this.dataSource.getUri() + " will be ignored!");
        }
    }
}
