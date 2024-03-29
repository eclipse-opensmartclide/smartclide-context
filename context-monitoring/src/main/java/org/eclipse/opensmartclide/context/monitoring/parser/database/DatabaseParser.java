package org.eclipse.opensmartclide.context.monitoring.parser.database;

/*
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.eclipse.opensmartclide.context.monitoring.analyser.IndexingAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IDatabase;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexedFields;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexingParser;
import org.eclipse.opensmartclide.context.monitoring.parser.file.IndexedFileFields;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;

import java.util.Date;

/**
 * DatabaseParser
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public abstract class DatabaseParser extends IndexingParser<IDatabase> {

    protected IndexingAnalyser<IMonitoringDataModel<?, ?>, IDatabase> serviceAnalyser;

    public DatabaseParser(final DataSource dataSource,
                          final InterpreterConfiguration interpreterConfiguration,
                          final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
        this.serviceAnalyser = this.interpreterConfiguration.createAnalyser(
            dataSource, indexer, this.document, amiConfiguration);
    }

    @Override
    public final synchronized boolean parse(final IDatabase object) {
        this.document.add(IndexedFields.createField(IndexedFields.Uri,
            String.valueOf(object.getURI())));
        this.document.add(IndexedFields.createField(IndexedFileFields.FilePath,
            String.valueOf(object.getURI())));
        this.document.add(IndexedFields.createField(IndexedFields.MonitoredAt,
            DateTools.timeToString(new Date().getTime(),
                DateTools.Resolution.SECOND)));

        return parseObject(object, this.document);
    }

    @Override
    public final synchronized IndexingAnalyser<IMonitoringDataModel<?, ?>, IDatabase> getAnalyser() {
        return this.serviceAnalyser;
    }

    /**
     * Abstract method to be implemented by the webservice specific parser.
     *
     * @param service  the actual webservice to parsed.
     * @param document the document to add indexed fields to.
     * @return <code>true</code> if parsing was successful, <code>false</code>
     * otherwise.
     */
    protected abstract boolean parseObject(IDatabase service,
                                           Document document);

}
