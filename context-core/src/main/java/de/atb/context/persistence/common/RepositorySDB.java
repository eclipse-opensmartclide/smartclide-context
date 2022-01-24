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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.sql.SDBConnection;
import org.apache.jena.sdb.store.DatabaseType;
import org.apache.jena.sdb.store.LayoutType;
import org.apache.jena.sdb.util.StoreUtils;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.IApplicationScenarioProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * RepositorySDB
 *
 * @author scholze
 * @version $LastChangedRevision: 703 $
 * @param <T>
 *            Param
 */
public abstract class RepositorySDB<T extends IApplicationScenarioProvider>
extends Repository<T> {
	private static final Logger logger = LoggerFactory
			.getLogger(RepositorySDB.class);

	private final Map<BusinessCase, Dataset> dataSets = new HashMap<>();
	private final Map<BusinessCase, Store> stores = new HashMap<>();

	protected RepositorySDB(final String baseLocation) {
		super(baseLocation);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected final synchronized <T extends Model> T initializeDefaultModel(
			final Class<T> clazz, final BusinessCase bc,
			final String modelLocation, final boolean useReasoner) { // TODO this should maybe replace by a call to DRM API
		final String modelDirStr = Repository.getLocationForBusinessCase(
				modelLocation, bc);
		final File modelDir = new File(modelDirStr);
		if (!modelDir.exists() && !modelDir.mkdirs()) {
			throw new IllegalArgumentException("Directory '" + modelDir
					+ "' does not exist and cannot be created.");
		}

		final Dataset ds = getDataSet(bc);
		T toReturn;
		final Model tdbModel = ds.getDefaultModel();
		if ((clazz == OntModel.class)
				|| implementsInterface(clazz, OntModel.class)) {
			final OntModel baseModel = ModelFactory.createOntologyModel(
					OntModelSpec.OWL_DL_MEM, tdbModel);
			OntModel ontModel;
			if (useReasoner) {
				ontModel = ModelFactory.createOntologyModel(
                    OntModelSpec.OWL_MEM, baseModel);
				ontModel.prepare();
			} else {
				ontModel = baseModel;
			}
			toReturn = (T) ontModel;
		} else if ((clazz == Model.class)
				|| implementsInterface(clazz, Model.class)) {
			toReturn = (T) tdbModel;
		} else {
			throw new IllegalArgumentException(
					"Clazz must be one of org.apache.jena.ontology.OntModel or org.apache.jena.rdf.Model!");
		}
		return toReturn;
	}

	private synchronized Dataset initializeDataSource(final BusinessCase bc) {
		RepositorySDB.logger.debug("Initializing DataSource for BC '{}'", bc);
		final Store store = setupStoreForBC(bc);
		final Dataset set = SDBFactory.connectDataset(store);
		dataSets.put(bc, set);
		return set;
	}

	private synchronized Store setupStoreForBC(final BusinessCase bc) {
		RepositorySDB.logger.debug("Setting up Store for BC '{}'", bc);
		final String location = getLocationForBusinessCase(bc);
		final SDBConnection conn = new SDBConnection("jdbc:h2:" + location,
				"sa", "");
		final DatabaseType dbType = DatabaseType.H2;
		final Store store = SDBFactory.connectStore(conn, new StoreDesc(
				LayoutType.LayoutTripleNodesIndex, dbType));
		try {
			if (!StoreUtils.isFormatted(store)) {
				store.getTableFormatter().create();
			}
		} catch (final SQLException e) {
			RepositorySDB.logger.error(e.getMessage(), e);
		}
		stores.put(bc, store);
		return store;
	}

	@Override
	public final synchronized Dataset getDataSet(final BusinessCase bc) {
        Dataset ds = dataSets.get(bc);
		if (ds == null) {
			ds = initializeDataSource(bc);
		}
		return ds;
	}

	protected final Store getStore(final BusinessCase bc) {
		Store store = stores.get(bc);
		if (bc == null) {
			store = setupStoreForBC(bc);
		}
		return store;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see de.atb.context.persistence.common.Repository#reset(de.atb.context.common.util.BusinessCase)
	 */
	@Override
	public final boolean reset(final BusinessCase bc) {
		// TODO reset maybe should add default model for BC
		RepositorySDB.logger.debug("Resetting DB for BC '{}'", bc);
		clearCache(bc);
		initializeDataSource(bc);
		final Store store = getStore(bc);
		if (store != null) {
			store.getTableFormatter().truncate();
		}
		return true;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see de.atb.context.persistence.common.Repository#shuttingDown()
	 */
	@Override
	protected final void shuttingDown() {
		clearCache();
	}

	private void clearCache() {
		clearCache(null);
	}

	public final synchronized void shutdown() {
		clearCache(null);
	}

	private synchronized void clearCache(final BusinessCase bc) {
		if (bc == null) {
			RepositorySDB.logger
					.debug("Clearing Repository DB Cache for all BCs");
			for (final Dataset set : dataSets.values()) {
				if (set.getDefaultModel() != null) {
					set.getDefaultModel().close();
				}
				final Iterator<String> names = set.listNames();
				while (names.hasNext()) {
					set.getNamedModel(names.next()).close();
				}
				set.close();
			}
			dataSets.clear();

			for (final Store store : stores.values()) {
				if (store.getConnection() != null) {
					store.getConnection().close();
				}
				store.close();
			}
			stores.clear();
		} else {
			RepositorySDB.logger.debug("Clearing Repository DB Cache for BC '{}'", bc);
			final Dataset set = dataSets.remove(bc);
			if (set != null) {
				if (set.getDefaultModel() != null) {
					set.getDefaultModel().close();
				}
				final Iterator<String> names = set.listNames();
				while (names.hasNext()) {
					final String name = names.next();
					set.getNamedModel(name).close();
				}
				set.close();
			}

			final Store store = stores.remove(bc);
			if (store != null) {
				if (store.getConnection() != null) {
					store.getConnection().close();
				}
				store.close();
			}
		}
	}
}
