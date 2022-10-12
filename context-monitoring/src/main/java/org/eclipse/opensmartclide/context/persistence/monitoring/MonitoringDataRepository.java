package org.eclipse.opensmartclide.context.persistence.monitoring;

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


import org.eclipse.opensmartclide.context.common.util.*;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.rdf.RdfHelper;
import org.eclipse.opensmartclide.context.persistence.common.RepositoryTDB;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.eclipse.opensmartclide.context.persistence.common.IPersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thewebsemantic.RDF2Bean;
import thewebsemantic.Sparql;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MonitoringDataRepository
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public final class MonitoringDataRepository<Type extends IMonitoringDataModel<?, ?>>
    extends RepositoryTDB<Type>
    implements IMonitoringDataRepository<Type> {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringDataRepository.class);
    private static final String internalBaseUri = "monitoring";
    private static final MonitoringDataRepository<? extends IMonitoringDataModel<?, ?>> instance;

    static {
        synchronized (new Object()) {
            instance = new MonitoringDataRepository<>();
        }
    }

    @SuppressWarnings("unchecked")
    public static <Type> MonitoringDataRepository<? extends Type> getInstance() {
        return (MonitoringDataRepository<? extends Type>) instance;
    }

    private MonitoringDataRepository() {
        super(internalBaseUri);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#persist(IApplicationScenarioProvider)
     */
    @Override
    public synchronized void persist(final Type monitoringData) {
        validateNotNull(monitoringData, "monitoringData");

        triggerPreProcessors(monitoringData.getApplicationScenario(), monitoringData);
        persist(monitoringData.toRdfModel(), monitoringData.getApplicationScenario());
        triggerPostProcessors(monitoringData.getApplicationScenario(), monitoringData);
    }

    @SuppressWarnings("unchecked")
    public synchronized <D extends DataSource> void persist(final String rdfString, final Class<Type> clazz) {
        validateString(rdfString, "rdfString");
        validateNotNull(clazz, "clazz");

        Type bean = RdfHelper.createMonitoringData(rdfString, (Class<? extends IMonitoringDataModel<Type, D>>) clazz);
        persist(bean);
    }

    public synchronized void persist(final Model monitoringData, final ApplicationScenario applicationScenario) {
        validateNotNull(monitoringData, "monitoringData");
        validateNotNull(applicationScenario, "applicationScenario");

        Model model = getDataSet(applicationScenario.getBusinessCase()).getDefaultModel();
        model.begin();
        try {
            model.add(monitoringData);
            model.commit();
        } catch (Exception e) {
            model.abort();
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(BusinessCase, java.lang.Class, int)
     */
    @Override
    public synchronized List<Type> getMonitoringData(final BusinessCase businessCase,
                                                     final Class<Type> clazz,
                                                     final int count) {
        validateNotNull(businessCase, "businessCase");
        validateNotNull(clazz, "clazz");
        if (count < 0) {
            throw new IllegalArgumentException("Count has to be > -1!");
        }

        String selectQuery;
        if (count > 0) {
            selectQuery = String.format(
                "SELECT ?s WHERE { ?s a %s ; %s ?mon } ORDER BY DESC (?mon) LIMIT %d",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt"),
                count
            );
        } else {
            selectQuery = String.format(
                "SELECT ?s WHERE { ?s a %s ; %s ?mon } ORDER BY DESC (?mon)",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt")
            );
        }

        return getData(businessCase, clazz, selectQuery);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(ApplicationScenario, Class, int)
     */
    @Override
    public synchronized List<Type> getMonitoringData(final ApplicationScenario applicationScenario,
                                                     final Class<Type> clazz,
                                                     final int count) {
        validateNotNull(applicationScenario, "applicationScenario");

        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, count);
    }

    @Override
    public synchronized List<Type> getMonitoringData(final BusinessCase businessCase,
                                                     final Class<Type> clazz,
                                                     final TimeFrame timeFrame) {
        validateNotNull(businessCase, "businessCase");
        validateNotNull(clazz, "clazz");
        validateTimeFrame(timeFrame);

        String endTime = timeFrame.getXSDLexicalFormForEndTime();
        String startTime = timeFrame.getXSDLexicalFormForStartTime();
        String selectQuery = "";

        if ((endTime != null) && (startTime != null)) {
            selectQuery = String.format(
                "SELECT ?s WHERE { ?s a %s . ?s %s ?mon . FILTER (?mon <= %s && ?mon >= %s)} ORDER BY DESC (?mon)",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt"),
                endTime,
                startTime
            );
        }

        return getData(businessCase, clazz, selectQuery);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(ApplicationScenario, Class, TimeFrame)
     */
    @Override
    public synchronized List<Type> getMonitoringData(final ApplicationScenario applicationScenario,
                                                     final Class<Type> clazz,
                                                     final TimeFrame timeFrame) {
        validateNotNull(applicationScenario, "applicationScenario");

        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, timeFrame);
    }

    @Override
    public synchronized Type getMonitoringData(final BusinessCase businessCase,
                                               final Class<Type> clazz,
                                               final String identifier) {
        validateNotNull(businessCase, "businessCase");
        validateNotNull(clazz, "clazz");
        validateString(identifier, "identifier");

        String selectQuery = String.format(
            "SELECT ?s WHERE { ?s rdf:type %s . ?s %s \"%s\"^^xsd:string}",
            SPARQLHelper.getRdfClassQualifier(clazz),
            SPARQLHelper.getRdfPropertyQualifier(clazz, "identifier"),
            identifier
        );

        List<Type> resultList = getData(businessCase, clazz, selectQuery);
        Type result = null;
        if (!resultList.isEmpty()) {
            result = resultList.get(0);
            if (result != null) {
                result.initialize();
            }
        } else {
            logger.warn("No Monitoring Data with id '" + identifier + "' found!");
        }
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(ApplicationScenario, java.lang.Class, java.lang.String)
     */
    @Override
    public synchronized Type getMonitoringData(final ApplicationScenario applicationScenario,
                                               final Class<Type> clazz,
                                               final String identifier) {
        validateNotNull(applicationScenario, "applicationScenario");
        validateNotNull(clazz, "clazz");
        validateString(identifier, "identifier");

        for (ApplicationScenario scenario : ApplicationScenario.values()) {
            if (identifier.equals(scenario.toString())) {
                return getStaticMonitoringData(applicationScenario, clazz, "monitoring/" + identifier + ".rdf");
            }
        }

        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, identifier);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(BusinessCase, java.lang.Class, int)
     */
    @Override
    public synchronized List<String> getLastIds(final BusinessCase businessCase,
                                                final Class<Type> clazz,
                                                final int count) {
        validateNotNull(businessCase, "businessCase");
        validateNotNull(clazz, "clazz");
        if (count < 0) {
            throw new IllegalArgumentException("Count has to be > -1!");
        }

        String selectQuery;
        if (count > 0) {
            selectQuery = String.format(
                "SELECT ?id WHERE { ?c rdf:type %1$s . ?c %2$s ?id . ?c %3$s ?mon } ORDER BY DESC (?mon) LIMIT %4$d",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "identifier"),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt"),
                count
            );
        } else {
            selectQuery = String.format(
                "SELECT ?id WHERE { ?c rdf:type %1$s . ?c %2$s ?id . ?c %3$s ?mon } ORDER BY DESC (?mon)",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "identifier"),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt")
            );
        }
        return getIDs(businessCase, selectQuery);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(ApplicationScenario, java.lang.Class, int)
     */
    @Override
    public synchronized List<String> getLastIds(final ApplicationScenario applicationScenario,
                                                final Class<Type> clazz,
                                                final int count) {
        validateNotNull(applicationScenario, "applicationScenario");

        return getLastIds(applicationScenario.getBusinessCase(), clazz, count);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(BusinessCase, java.lang.Class, TimeFrame)
     */
    @Override
    public synchronized List<String> getLastIds(final BusinessCase businessCase,
                                                final Class<Type> clazz,
                                                final TimeFrame timeFrame) {
        validateNotNull(businessCase, "businessCase");
        validateNotNull(clazz, "clazz");
        validateTimeFrame(timeFrame);

        String endTime = timeFrame.getXSDLexicalFormForEndTime();
        String startTime = timeFrame.getXSDLexicalFormForStartTime();
        String selectQuery = "";

        if ((endTime != null) && (startTime != null)) {
            selectQuery = String.format(
                "SELECT ?id WHERE { ?c rdf:type %1$s . ?c %2$s ?id . ?c %3$s ?mon . FILTER (?mon <= %4$s && ?mon >= %5$s)} ORDER BY DESC (?mon)",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "identifier"),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt"),
                endTime,
                startTime
            );
        } else if (endTime == null) {
            selectQuery = String.format(
                "SELECT ?id WHERE { ?c rdf:type %1$s . ?c %2$s ?id . ?c %3$s ?mon . FILTER (?mon >= %4$s)} ORDER BY DESC (?mon)",
                SPARQLHelper.getRdfClassQualifier(clazz),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "identifier"),
                SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt"),
                startTime
            );
        }
        return getIDs(businessCase, selectQuery);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(ApplicationScenario, java.lang.Class, TimeFrame)
     */
    @Override
    public synchronized List<String> getLastIds(final ApplicationScenario applicationScenario,
                                                final Class<Type> clazz,
                                                final TimeFrame timeFrame) {
        validateNotNull(applicationScenario, "applicationScenario");

        return getLastIds(applicationScenario.getBusinessCase(), clazz, timeFrame);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#executeSparqlUpdateQuery(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized void executeSparqlUpdateQuery(final BusinessCase businessCase, final String query) {
        executeSparqlUpdateQueries(businessCase, query);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#executeSparqlUpdateQueries(BusinessCase, java.lang.String[])
     */
    @Override
    public synchronized void executeSparqlUpdateQueries(final BusinessCase businessCase, final String... queries) {
        validateNotNull(businessCase, "businessCase");
        validateNotNull(queries, "queries");
        if (queries.length <= 0) {
            throw new IllegalArgumentException("Queries may not be empty!");
        }

        UpdateRequest ur = UpdateFactory.create();
        for (String query : queries) {
            ur.add(prepareSparqlQuery(query));
        }
        try {
            // FIXME: should an UpdateAction be wrapped in a transaction?
            UpdateAction.execute(ur, getDataSet(businessCase));
        } catch (RuntimeException qe) {
            logger.error(qe.getMessage(), qe);
            throw qe;
        }
    }

    @Override
    public void shutdown() {
        shuttingDown();
    }

    private List<Type> getData(BusinessCase businessCase, Class<Type> clazz, String selectQuery) {
        final String query = SPARQLHelper.appendDefaultPrefixes(selectQuery);
        logger.debug(query);
        final Dataset dataSet = getDataSet(businessCase);

        return transactional(dataSet, new ArrayList<>(), () -> {
            final Model model = dataSet.getDefaultModel();
            //noinspection unchecked
            return Sparql.exec(model, clazz, query)
                .stream()
                .map(type -> (Type) initLazyModel(model, type))
                .collect(Collectors.toList());
        });
    }

    private List<String> getIDs(BusinessCase businessCase, String selectQuery) {
        if (selectQuery == null) {
            throw new NullPointerException("selectQuery may not be null!");
        }

        final String query = SPARQLHelper.appendDefaultPrefixes(selectQuery);
        return getIds(businessCase, query, "id");
    }

    @SuppressWarnings("unchecked")
    private <T, D extends DataSource> IMonitoringDataModel<T, D> initLazyModel(final Model model, final Type data) {
        RDF2Bean bb = new RDF2Bean(model);
        Class<T> implClass;
        if (data != null && data.getImplementingClassName() != null) {
            try {
                implClass = (Class<T>) Class.forName(data.getImplementingClassName());
                return (IMonitoringDataModel<T, D>) bb.loadDeep(implClass, data.getIdentifier());
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.warn("MonitoringDataModel has no implementing class name!");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private synchronized <D extends DataSource> Type getStaticMonitoringData(final ApplicationScenario scenario,
                                                                             final Class<Type> clazz,
                                                                             final String identifier) {
        Type type = null;
        Model model = ModelFactory.createDefaultModel();
        logger.debug("Trying to locate static monitoring data under " + identifier);
        URL fileUri = Thread.currentThread().getContextClassLoader().getResource(identifier);
        if (fileUri != null) {
            logger.debug("Loading static monitoring data for " + scenario + " under '" + identifier + "'");
            logger.debug("\t" + fileUri);
            model.read(fileUri.toString());
            type = RdfHelper.createMonitoringData(model, (Class<? extends IMonitoringDataModel<Type, D>>) clazz);
        } else {
            logger.debug("Unable to locate static monitoring data for " + scenario + " under '" + identifier + "'");
        }
        return type;
    }
}
