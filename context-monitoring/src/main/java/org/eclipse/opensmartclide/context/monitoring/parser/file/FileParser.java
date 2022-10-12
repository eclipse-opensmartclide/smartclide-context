package org.eclipse.opensmartclide.context.monitoring.parser.file;

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
import java.util.Date;

import org.eclipse.opensmartclide.context.monitoring.analyser.file.FileAnalyser;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexingParser;
import org.apache.lucene.document.DateTools;

import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.eclipse.opensmartclide.context.common.util.Hashing;
import org.eclipse.opensmartclide.context.monitoring.analyser.IndexingAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexedFields;

/**
 * A FileParser parses a certain type of FileSystem and returns the model or
 * structure inside of it. The structure itself has to be typed for each parser,
 * implementing this class.
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public abstract class FileParser extends IndexingParser<File> {

    protected FileAnalyser<IMonitoringDataModel<?, ?>> fileAnalyser;

    public FileParser(final DataSource dataSource,
                      final InterpreterConfiguration interpreterConfiguration,
                      final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
        this.fileAnalyser = this.interpreterConfiguration.createFileAnalyser(
            dataSource, indexer, this.document, amiConfiguration);
    }

    /**
     * (non-Javadoc)
     *
     * @see IndexingParser#parse(java.lang.Object)
     */
    @Override
    public final synchronized boolean parse(final File file) {
        // some generic file handling stuff could be done here
        // like indexing file creation, modification etc.
        String uri = file.getAbsolutePath();

        if (this.isIndexUpToDate(uri, file.lastModified())) {
            return false;
        }

        String filepath = file.getAbsolutePath().substring(0,
            file.getAbsolutePath().length() - file.getName().length());

        this.document.add(IndexedFields.createField(IndexedFields.Uri, uri));
        this.document.add(IndexedFields.createField(IndexedFileFields.FileName, file.getName()));
        this.document.add(IndexedFields.createField(IndexedFileFields.FilePath, filepath));
        this.document.add(IndexedFields.createField(IndexedFileFields.Hidden,
            Boolean.toString(file.isHidden())));
        this.document
            .add(IndexedFields.createField(IndexedFields.LastModified, DateTools
                .timeToString(file.lastModified(),
                    DateTools.Resolution.MINUTE)));
        this.document.add(IndexedFields.createField(IndexedFileFields.FileSize,
            Long.toString(file.length())));
        this.document.add(IndexedFields.createField(IndexedFileFields.Hash,
            Hashing.getSHA256Checksum(file)));
        this.document.add(IndexedFields.createField(IndexedFields.MonitoredAt,
            DateTools.timeToString(new Date().getTime(),
                DateTools.Resolution.SECOND)));

        return parseObject(file);
    }

    /**
     * (non-Javadoc)
     *
     * @see IndexingParser#getAnalyser()
     */
    @Override
    public final synchronized IndexingAnalyser<IMonitoringDataModel<?, ?>, File> getAnalyser() {
        return this.fileAnalyser;
    }

    /**
     * Abstract method to be implemented by the file specific parser.
     *
     * @param file the actual file to parsed.
     * @return <code>true</code> if parsing was successful, <code>false</code>
     * otherwise.
     */
    protected abstract boolean parseObject(File file);

}
