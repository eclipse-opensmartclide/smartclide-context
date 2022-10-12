package org.eclipse.opensmartclide.context.monitoring.analyser.file;

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


import java.io.File;
import java.util.List;

import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.apache.lucene.document.Document;
import org.eclipse.opensmartclide.context.monitoring.analyser.IndexingAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.javatuples.Pair;

/**
 * FilePairAnalyser
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public abstract class FilePairAnalyser<OutputType extends IMonitoringDataModel<?, ?>> extends
        IndexingAnalyser<OutputType, Pair<File, File>> {

    public FilePairAnalyser(final DataSource dataSource, final InterpreterConfiguration interpreterConfiguration, final Indexer indexer, final Document document, final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
    }

    /**
     * (non-Javadoc)
     *
     * @see IndexingAnalyser#analyseObject(java.lang.Object, org.apache.lucene.document.Document)
     */
    @Override
    public final List<OutputType> analyseObject(final Pair<File, File> filePair, final Document document) {
        // some generic handling stuff could be done here
        // like indexing file creation, modification etc.

        return analyseObject(filePair);
    }

    public abstract List<OutputType> analyseObject(Pair<File, File> filePair);

}
