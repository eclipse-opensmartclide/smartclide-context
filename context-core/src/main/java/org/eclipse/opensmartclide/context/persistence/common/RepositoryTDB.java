package org.eclipse.opensmartclide.context.persistence.common;

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

import org.eclipse.opensmartclide.context.common.exceptions.ConfigurationException;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.IApplicationScenarioProvider;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.context.util.OntologyNamespace;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * (non-Javadoc)
     *
     * @see Repository#reset(BusinessCase)
     */
    @Override
    public final boolean reset(final BusinessCase businessCase) {
        validateNotNull(businessCase, "businessCase");

        final Dataset set = this.datasets.remove(businessCase);
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
            initializeDataset(businessCase);
            return true;
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public final synchronized Dataset getDataSet(final BusinessCase businessCase) {
        validateNotNull(businessCase, "businessCase");

        Dataset ds = datasets.get(businessCase);
        if (ds == null) {
            try {
                ds = initializeDataset(businessCase);
            } catch (ConfigurationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return ds;
    }

    public synchronized ResultSet executeSparqlSelectQuery(final BusinessCase businessCase, final String query) {
        return executeSparqlSelectQuery(businessCase, query, false);
    }

    public synchronized ResultSet executeSparqlSelectQuery(final BusinessCase businessCase,
                                                           final String query,
                                                           final boolean useReasoner) {
        validateNotNull(businessCase, "businessCase");
        validateString(query, "query");

        final Dataset dataset = getDataSet(businessCase);
        return transactional(dataset, null, () -> getQueryExecution(query, useReasoner, dataset).execSelect());
    }

    protected RepositoryTDB(final String baseLocation) {
        super(baseLocation);
    }

    /**
     * (non-Javadoc)
     *
     * @see Repository#shuttingDown()
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

    protected final List<String> getIds(final BusinessCase businessCase,
                                        final String query,
                                        final String idLiteralName) {
        validateNotNull(businessCase, "businessCase");
        validateString(query, "query");
        validateString(idLiteralName, "idLiteralName");

        logger.debug(query);
        final Dataset dataSet = getDataSet(businessCase);

        return transactional(dataSet, new ArrayList<>(), () -> {
            final Model m = dataSet.getDefaultModel();
            final Query q = QueryFactory.create(query);
            final QueryExecution qe = QueryExecutionFactory.create(q, m);
            final ResultSet rs = qe.execSelect();
            final List<String> ids = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.nextSolution();
                final Literal idLiteral = qs.getLiteral(idLiteralName);
                if (idLiteral != null) {
                    ids.add(idLiteral.getString());
                }
            }
            return ids;
        });
    }

    protected final void transactional(final Dataset dataset, final Consumer<Dataset> consumer) {
        validateNotNull(dataset, "dataset");
        validateNotNull(consumer, "consumer");

        dataset.begin(ReadWrite.WRITE);
        try {
            consumer.accept(dataset);
            dataset.commit();
        } catch (Exception e) {
            logger.error("Error occurred, rolling back.", e);
            dataset.abort();
        } finally {
            dataset.end();
        }
    }

    protected final <R> R transactional(final Dataset dataset, final R defaultResult, final Supplier<R> supplier) {
        validateNotNull(dataset, "dataset");
        validateNotNull(supplier, "supplier");

        dataset.begin(ReadWrite.WRITE);
        try {
            R result = supplier.get();
            dataset.commit();
            return result;
        } catch (Exception e) {
            logger.error("Error occurred, rolling back.", e);
            dataset.abort();
            return defaultResult;
        } finally {
            dataset.end();
        }
    }

    protected final QueryExecution getQueryExecution(String query, boolean useReasoner, Dataset dataset) {
        validateString(query, "query");
        validateNotNull(dataset, "dataset");

        final String finalQuery = prepareSparqlQuery(query);
        final Model model = dataset.getDefaultModel();
        final OntModel ontModel;
        if (useReasoner) {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model);
        } else {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        }
        return QueryExecutionFactory.create(finalQuery, ontModel);
    }

    protected final synchronized String prepareSparqlQuery(final String query) {
        validateString(query, "query");
        logger.debug("Preparing sparql query '" + query);
        String finalQuery = OntologyNamespace.prepareSparqlQuery(query);
        logger.debug("Final query is " + finalQuery);
        return finalQuery;
    }

    protected final void validateTimeFrame(final TimeFrame timeFrame) {
        validateNotNull(timeFrame, "timeFrame");
        if ((timeFrame.getStartTime() == null) && (timeFrame.getEndTime() == null)) {
            throw new IllegalArgumentException("TimeFrame has to have at least a start date or an and date!");
        }
    }

    private synchronized Dataset initializeDataset(final BusinessCase bc) throws ConfigurationException {
        try {
            final Path dataDir = Paths.get(getLocationForBusinessCase(bc));
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
