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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.IApplicationScenarioProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Repository
 *
 * @author scholze
 * @version $LastChangedRevision: 703 $
 * @param <T>
 *            the IApplicationScenarioProvider type
 */
public abstract class RepositoryTDB<T extends IApplicationScenarioProvider>
extends Repository<T> {

	protected static final Logger logger = LoggerFactory
			.getLogger(RepositoryTDB.class);

	private final Map<BusinessCase, Dataset> datasets = new HashMap<>();

	protected RepositoryTDB(final String baseLocation) {
		super(baseLocation);
	}

	protected final synchronized boolean clearBaseDirectory() {
		for (final BusinessCase bc : BusinessCase.values()) {
			clearBusinessCaseDirectory(bc);
		}
		return RepositoryTDB.clearDirectory(new File(basicLocation));
	}

	protected final synchronized boolean clearBusinessCaseDirectory(
			final BusinessCase businessCase) {
		return RepositoryTDB.clearDirectory(new File(
				getLocationForBusinessCase(businessCase)));
	}

	protected static synchronized boolean clearDirectory(final File dir) {
		if (dir.isDirectory()) {
			boolean oneFailed = false;
			for (final String file : Objects.requireNonNull(dir.list())) {
				final boolean success = RepositoryTDB.clearDirectory(new File(
						dir, file));
				if (!success) {
					oneFailed = true;
					RepositoryTDB.logger.warn("Could not delete {}", dir.toString() + System.getProperty("file.separator") + file);
				}
			}
			RepositoryTDB.logger.info("Cleared directory "
					+ dir.getAbsolutePath());
			if (!oneFailed) {
				return true;
			}
		} else if (dir.isFile()) {
			return dir.delete();
		} else {
			RepositoryTDB.logger.warn("File or directory "
					+ dir.getAbsolutePath()
					+ " does not exist, creating directory!");
			return dir.mkdirs();
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see de.atb.context.persistence.common.Repository#shuttingDown()
	 */
	@Override
	protected final void shuttingDown() {
		for (final Dataset set : datasets.values()) {
            set.begin(ReadWrite.WRITE);
            try {
                TDB.sync(set);
                if (set.getDefaultModel() != null) {
                    TDB.sync(set.getDefaultModel());
                    set.getDefaultModel().close();
                }
                set.commit();
            } catch (Exception e) {
                set.abort();
            } finally {
                set.end();
            }
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
            set.begin(ReadWrite.WRITE);
            try {
                TDB.sync(set);
                if (set.getDefaultModel() != null) {
                    TDB.sync(set.getDefaultModel());
                    set.getDefaultModel().removeAll();
                    TDB.sync(set.getDefaultModel());
                    set.getDefaultModel().close();
                    set.asDatasetGraph().close();
                }
                set.commit();
            } catch (Exception e) {
                logger.error("Error occurred, rolling back.");
                set.abort();
            } finally {
                set.end();
            }
			set.close();
			TDB.closedown();
		}
        try {
            initializeDataset(bc);
            return true;
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        }
        return false;
	}

	@Override
	public final synchronized Dataset getDataSet(final BusinessCase bc) {
		Dataset ds = datasets.get(bc);
		if (ds == null) {
            try {
                ds = initializeDataset(bc);
            } catch (ConfigurationException e) {}
		}
		return ds;
	}

	private synchronized Dataset initializeDataset(final BusinessCase bc) throws ConfigurationException {
        try {
            Path dataDir = Paths.get(getLocationForBusinessCase(bc));
            if (Files.notExists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            final Dataset set = TDBFactory
                .createDataset(getLocationForBusinessCase(bc));
            datasets.put(bc, set);
            return set;

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new ConfigurationException("Data directory for the TDB repository couldn't be created.");
        }
	}
}
