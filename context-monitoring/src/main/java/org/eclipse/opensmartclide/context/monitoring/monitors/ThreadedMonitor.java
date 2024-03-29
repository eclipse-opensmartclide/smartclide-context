package org.eclipse.opensmartclide.context.monitoring.monitors;

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
import org.eclipse.opensmartclide.context.monitoring.config.models.Index;
import org.eclipse.opensmartclide.context.monitoring.config.models.Interpreter;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.config.models.Monitor;
import org.eclipse.opensmartclide.context.monitoring.events.MonitoringProgressListener;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.parser.IndexingParser;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ThreadedMonitor
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public abstract class ThreadedMonitor<P, A extends IMonitoringDataModel<?, ?>> extends AbstractMonitor<P> implements Runnable {
    protected boolean running = false;
    protected Indexer indexer;
    protected DataSource dataSource;
    protected Interpreter interpreter;
    protected Monitor monitor;
    protected AmIMonitoringConfiguration amiConfiguration;
    protected final List<MonitoringProgressListener<P, A>> progressListeners = new ArrayList<>();

    private Thread thread;

    private IndexingParser<P> parser;

    protected ThreadedMonitor(final DataSource dataSource,
                              final Interpreter interpreter,
                              final Monitor monitor,
                              final Indexer indexer,
                              final AmIMonitoringConfiguration configuration) {

        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource may not be null!");
        }
        if (interpreter == null) {
            throw new IllegalArgumentException("interpreter may not be null!");
        }
        if (indexer == null) {
            throw new IllegalArgumentException("indexer may not be null!");
        }
        if (monitor == null) {
            throw new IllegalArgumentException("monitor may not be null!");
        }

        this.dataSource = dataSource;
        this.interpreter = interpreter;
        this.monitor = monitor;
        this.indexer = indexer;
        this.amiConfiguration = configuration;
    }

    /**
     * Gets whether the ThreadedMonitor is currently running or paused.
     *
     * @return <code>true</code> if the resources are monitored right now by
     * this ThreadedMonitor, <code>false</code> otherwise.
     */
    public abstract boolean isRunning();

    /**
     * Pauses this thread
     */
    public abstract void pause();

    /**
     * Resumes this thread
     */
    public abstract void restart();

    protected abstract void shutdown();

    protected abstract void shutdown(long timeOut, TimeUnit units);

    public abstract void monitor() throws Exception;

    protected abstract boolean isIndexEnabled();

    public final void stop() {
        // if (this.isRunning()) {
        this.shutdown();
        if (this.thread != null) {
            this.thread.interrupt();
        }
        // }
    }

    public final void start() {
        // if (!this.isRunning()) {
        this.thread = new Thread(this);
        this.thread.start();
        // }
    }

    /**
     * Adds a MonitoringProgressListener to the list of subscribers. Subscribers
     * will be informed about and Document added to an Index by this
     * MonitoringThread.
     *
     * @param listener the MonitoringProgressListener to be added to the list of
     *                 subscribers.
     */
    public final void addProgressListener(
        final MonitoringProgressListener<P, A> listener) {
        synchronized (this.progressListeners) {
            this.progressListeners.add(listener);
        }
    }

    /**
     * Removes the MonitoringProgressListener from the list of subscribers.
     * Removed subscribers will no longer be informed about and Document added
     * to an Index by this MonitoringThread.
     *
     * @param listener the MonitoringProgressListener to be removed from the list of
     *                 subscribers.
     */
    public final void removeProgressListener(
        final MonitoringProgressListener<P, A> listener) {
        synchronized (this.progressListeners) {
            this.progressListeners.remove(listener);
        }
    }

    protected final IndexingParser<P> getParser(final InterpreterConfiguration setting) {
        if (parser == null) {
            parser = setting.createParser(this.dataSource, this.indexer, this.amiConfiguration);
        }
        return parser;
    }

    protected final void parseAndAnalyse(final P objectToParse,
                                         final IndexingParser<P> parser,
                                         final IndexingAnalyser<A, P> analyser) {
        if (parser.parse(objectToParse)) {
            final Document document = parser.getDocument();
            this.raiseParsedEvent(objectToParse, document);
            if (isIndexEnabled()) {
                this.indexer.addDocumentToIndex(document);
                this.raiseIndexedEvent(document);
            }
            final List<A> analysedModels = analyser.analyse(objectToParse);
            this.raiseAnalysedEvent(analysedModels, objectToParse, analyser.getDocument());
        }
    }

    /**
     * Notifies all registered MonitoringProgressListeners about the document
     * that was indexed recently.
     *
     * @param document the document that just has been indexed.
     */
    protected final void raiseIndexedEvent(final Document document) {
        for (MonitoringProgressListener<P, A> mpl : this.progressListeners) {
            mpl.documentIndexed(this.indexer.getIndexId(), document);
        }
    }

    /**
     * Notifies all registered MonitoringProgressListeners about the document
     * that and the underlying resource that was parsed recently.
     *
     * @param document the document for the resource that just has been parsed.
     * @param parsed   the resource that has just been parsed.
     */
    protected final void raiseParsedEvent(final P parsed,
                                          final Document document) {
        for (MonitoringProgressListener<P, A> mpl : this.progressListeners) {
            mpl.documentParsed(parsed, document);
        }
    }

    /**
     * Notifies all registered MonitoringProgressListeners about the document
     * that and the underlying resource that was analysed recently.
     *
     * @param document the document for the resource that just has been analysed.
     * @param parsed   the original resource that has just been analysed.
     * @param analysed the outcome of the analysing progress.
     */
    protected final void raiseAnalysedEvent(final List<A> analysed,
                                            final P parsed, final Document document) {
        for (MonitoringProgressListener<P, A> mpl : this.progressListeners) {
            mpl.documentAnalysed(analysed, parsed, document);
        }
    }

    /**
     * Gets the INDEXER associated with this ThreadedMonitor.
     *
     * @return the INDEXER associated with this ThreadedMonitor.
     */
    public final Indexer getIndexer() {
        return this.indexer;
    }

    /**
     * Gets the Index associated with this ThreadedMonitor.
     *
     * @return the Index associated with this ThreadedMonitor.
     */
    public final Index getIndex() {
        if (this.indexer != null) {
            return this.indexer.getIndex();
        } else {
            return null;
        }
    }

    /**
     * Gets the DataSource associated with this ThreadedMonitor.
     *
     * @return the DataSource associated with this ThreadedMonitor.
     */
    public final DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Gets the Interpreter associated with this ThreadedMonitor.
     *
     * @return the Interpreter associated with this ThreadedMonitor.
     */
    public final Interpreter getInterpreter() {
        return this.interpreter;
    }
}
