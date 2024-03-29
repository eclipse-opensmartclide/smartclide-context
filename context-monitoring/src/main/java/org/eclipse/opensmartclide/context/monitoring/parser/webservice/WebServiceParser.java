package org.eclipse.opensmartclide.context.monitoring.parser.webservice;

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

import static org.eclipse.opensmartclide.context.monitoring.parser.IndexedFields.MonitoredAt;
import static org.eclipse.opensmartclide.context.monitoring.parser.IndexedFields.Uri;

import java.util.Date;

import org.eclipse.opensmartclide.context.monitoring.parser.IndexingParser;
import org.eclipse.opensmartclide.context.monitoring.parser.file.IndexedFileFields;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;

import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.context.monitoring.analyser.IndexingAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.models.IWebService;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexedFields;

/**
 * WebServiceParser
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public abstract class WebServiceParser extends IndexingParser<IWebService> {

    protected IndexingAnalyser<IMonitoringDataModel<?, ?>, IWebService> serviceAnalyser;

    public WebServiceParser(final DataSource dataSource,
                            final InterpreterConfiguration interpreterConfiguration,
                            final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
        this.serviceAnalyser = this.interpreterConfiguration.createAnalyser(
            dataSource, indexer, this.document, amiConfiguration);
    }

    @Override
    public final synchronized boolean parse(final IWebService object) {
        // some generic webservice handling stuff could be done here
        // like indexing webservice status, modification etc.

        this.document.add(IndexedFields.createField(Uri,
            String.valueOf(object.getURI())));
        this.document.add(IndexedFields.createField(IndexedFileFields.FilePath,
            String.valueOf(object.getURI())));
        this.document.add(IndexedFields.createField(MonitoredAt,
            DateTools.timeToString(new Date().getTime(),
                DateTools.Resolution.SECOND)));

        // TODO add some webserivce-specific fields to the document's index
        return parseObject(object, this.document);
    }

    @Override
    public final synchronized IndexingAnalyser<IMonitoringDataModel<?, ?>, IWebService> getAnalyser() {
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
    protected abstract boolean parseObject(IWebService service,
                                           Document document);

}
