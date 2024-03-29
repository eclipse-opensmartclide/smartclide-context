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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.opensmartclide.context.monitoring.analyser.IndexingAnalyser;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexedFields;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexingParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.document.DateTools;
import org.eclipse.opensmartclide.context.common.util.Hashing;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.javatuples.Pair;

/**
 * FilePairParser
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public abstract class FilePairParser extends IndexingParser<Pair<File, File>> {

    protected IndexingAnalyser<IMonitoringDataModel<?, ?>, Pair<File, File>> filePairAnalyser;

    public FilePairParser(final DataSource dataSource,
                          final InterpreterConfiguration interpreterConfiguration,
                          final Indexer indexer, final AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
        this.filePairAnalyser = this.interpreterConfiguration.createAnalyser(
            dataSource, indexer, this.document, amiConfiguration);
    }

    /**
     * (non-Javadoc)
     *
     * @see IndexingParser#parse(java.lang.Object)
     */
    @Override
    public final boolean parse(final Pair<File, File> filePair) {
        File fileOne = filePair.getValue0();

        // TODO handle fileTwo
        @SuppressWarnings("unused")
        File fileTwo = filePair.getValue1();

        // some generic file handling stuff could be done here
        // like indexing file creation, modification etc.
        String uri = fileOne.getAbsolutePath();

        if (this.isIndexUpToDate(uri, fileOne.lastModified())) {
            return false;
        }
        String filepath = fileOne.getAbsolutePath()
            .substring(
                0,
                fileOne.getAbsolutePath().length()
                    - fileOne.getName().length());

        this.document.add(IndexedFields.createField(IndexedFields.Uri, uri));
        this.document
            .add(IndexedFields.createField(IndexedFileFields.FileName, fileOne.getName()));
        this.document.add(IndexedFields.createField(IndexedFileFields.FilePath, filepath));
        this.document.add(IndexedFields.createField(IndexedFileFields.Hidden,
            Boolean.toString(fileOne.isHidden())));
        this.document.add(IndexedFields.createField(IndexedFields.LastModified, DateTools
            .timeToString(fileOne.lastModified(),
                DateTools.Resolution.MINUTE)));
        this.document.add(IndexedFields.createField(IndexedFileFields.FileSize,
            Long.toString(fileOne.length())));
        this.document.add(IndexedFields.createField(IndexedFileFields.Hash,
            Hashing.getSHA256Checksum(fileOne)));
        this.document.add(IndexedFields.createField(IndexedFields.MonitoredAt,
            DateTools.timeToString(new Date().getTime(),
                DateTools.Resolution.SECOND)));

        return parseObject(filePair);
    }

    /**
     * (non-Javadoc)
     *
     * @see IndexingParser#getAnalyser()
     */
    @Override
    public final IndexingAnalyser<IMonitoringDataModel<?, ?>, Pair<File, File>> getAnalyser() {
        return this.filePairAnalyser;
    }

    /**
     * Abstract method to be implemented by the file pair specific parser.
     *
     * @param filePair the actual pair of files to be parsed.
     * @return <code>true</code> if parsing was successful, <code>false</code>
     * otherwise.
     */
    protected abstract boolean parseObject(Pair<File, File> filePair);

    public static List<Pair<File, File>> getPairsFromFiles(final File[] files,
                                                           final String extensionOne, final String extensionTwo) {
        if (files == null) {
            throw new IllegalArgumentException("Files may not be null");
        }
        if (files.length % 2 != 0) {
            throw new IllegalArgumentException(
                "Lenght of files has to be divisibel by two, so that files.length % 2 == 0");
        }

        List<Pair<File, File>> filePairs = new ArrayList<>();
        if (files.length == 0) {
            return filePairs;
        } else {
            Arrays.sort(files, (o1, o2) -> {
                final String file1Extension = o1 != null ? FilenameUtils
                    .getExtension(o1.getAbsolutePath()) : "";
                final String file2Extension = o2 != null ? FilenameUtils
                    .getExtension(o2.getAbsolutePath()) : "";

                int extensionDiff = file1Extension
                    .compareTo(file2Extension);
                if (extensionDiff == 0 && o1 != null) {
                    return o1.compareTo(o2);
                }
                return extensionDiff;
            });
            for (int i = 0; i < files.length / 2; i++) {
                File file1 = files[i];
                File file2 = files[(files.length / 2) + i];
                if (file1.getName().endsWith(extensionOne)) {
                    if (!file2.getName().endsWith(extensionTwo)) {
                        throw new IllegalArgumentException(
                            "There should be exactly the same amount of files with extension '"
                                + extensionTwo
                                + "' as with extension '"
                                + extensionTwo + "'!");
                    }
                } else if (file2.getName().endsWith(extensionOne)) {
                    if (!file1.getName().endsWith(extensionOne)) {
                        throw new IllegalArgumentException(
                            "There should be exactly the same amount of files with extension '"
                                + extensionTwo
                                + "' as with extension '"
                                + extensionTwo + "'!");
                    }
                }
                filePairs.add(new Pair<>(file1, file2));
            }
        }
        return filePairs;
    }
}
