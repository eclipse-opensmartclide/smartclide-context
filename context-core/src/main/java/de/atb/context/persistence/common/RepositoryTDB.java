package de.atb.context.persistence.common;

/*
 * #%L
 * ATB Context Extraction Core Lib
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

import de.atb.context.common.exceptions.ConfigurationException;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.IApplicationScenarioProvider;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Repository
 *
 * @param <T> the IApplicationScenarioProvider type
 * @author scholze
 * @version $LastChangedRevision: 703 $
 */
public abstract class RepositoryTDB<T extends IApplicationScenarioProvider> extends Repository<T> {

    protected static final Logger logger = LoggerFactory.getLogger(RepositoryTDB.class);

    private final Map<BusinessCase, Dataset> datasets = new HashMap<>();

    protected RepositoryTDB(final String baseLocation) {
        super(baseLocation);
    }

    protected static synchronized boolean clearDirectory(final File dir) {
        if (dir.isDirectory()) {
            boolean oneFailed = false;
            String[] fileList = dir.list();
            if (fileList != null) {
                for (final String file : fileList) {
                    final boolean success = RepositoryTDB.clearDirectory(new File(dir, file));
                    if (!success) {
                        oneFailed = true;
                        logger.warn("Could not delete {}", dir + System.getProperty("file.separator") + file);
                    }
                }
            }
            logger.info("Cleared directory {}", dir.getAbsolutePath());
            return !oneFailed;
        } else if (dir.isFile()) {
            return dir.delete();
        } else {
            logger.warn("File or directory {} does not exist, creating directory!", dir.getAbsolutePath());
            return dir.mkdirs();
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.common.Repository#shuttingDown()
     */
    @Override
    protected final void shuttingDown() {
        for (final Dataset set : datasets.values()) {
            transactional(set, (ds) -> {
                TDB.sync(ds);
                if (ds.getDefaultModel() != null) {
                    TDB.sync(ds.getDefaultModel());
                    ds.getDefaultModel().close();
                }
            });
            set.close();
        }
        datasets.clear();
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.common.Repository#reset(de.atb.context.common.util.BusinessCase)
     */
    @Override
    public final boolean reset(final BusinessCase bc) {
        final Dataset set = this.datasets.remove(bc);
        if (set != null) {
            transactional(set, (ds) -> {
                TDB.sync(ds);
                if (ds.getDefaultModel() != null) {
                    TDB.sync(ds.getDefaultModel());
                    ds.getDefaultModel().removeAll();
                    TDB.sync(ds.getDefaultModel());
                    ds.getDefaultModel().close();
                    ds.asDatasetGraph().close();
                }
            });
            set.close();
            TDB.closedown();
        }
        try {
            initializeDataset(bc);
            return true;
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public final synchronized Dataset getDataSet(final BusinessCase bc) {
        Dataset ds = datasets.get(bc);
        if (ds == null) {
            try {
                ds = initializeDataset(bc);
            } catch (ConfigurationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return ds;
    }

    protected void transactional(final Dataset dataSet, final Consumer<Dataset> block) {
        dataSet.begin(ReadWrite.WRITE);
        try {
            block.accept(dataSet);
            dataSet.commit();
        } catch (Exception e) {
            logger.error("Error occurred, rolling back.", e);
            dataSet.abort();
        } finally {
            dataSet.end();
        }
    }

    protected <R> R transactional(final Dataset dataSet, final R defaultResult, final Supplier<R> block) {
        // FIXME: Should this be `begin(ReadWrite.READ)`? Because everything in `block` seems to be just "selects"?
        dataSet.begin(ReadWrite.WRITE);
        try {
            R result = block.get();
            dataSet.commit();
            return result;
        } catch (Exception e) {
            logger.error("Error occurred, rolling back.", e);
            dataSet.abort();
            return defaultResult;
        } finally {
            dataSet.end();
        }
    }

    private synchronized Dataset initializeDataset(final BusinessCase bc) throws ConfigurationException {
        try {
            Path dataDir = Paths.get(getLocationForBusinessCase(bc));
            if (Files.notExists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            final Dataset set = TDBFactory.createDataset(getLocationForBusinessCase(bc));
            datasets.put(bc, set);
            return set;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new ConfigurationException("Data directory for the TDB repository couldn't be created.");
        }
    }
}
