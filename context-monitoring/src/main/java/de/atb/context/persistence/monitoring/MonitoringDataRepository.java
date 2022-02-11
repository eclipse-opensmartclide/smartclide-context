package de.atb.context.persistence.monitoring;

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


import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.SPARQLHelper;
import de.atb.context.common.util.TimeFrame;
import de.atb.context.context.util.OntologyNamespace;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.common.RepositoryTDB;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thewebsemantic.RDF2Bean;
import thewebsemantic.Sparql;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
     * @see de.atb.context.persistence.common.IPersistenceUnit#persist(de.atb.context.common.util.IApplicationScenarioProvider)
     */
    @Override
    public synchronized void persist(final Type monitoringData) {
        triggerPreProcessors(monitoringData.getApplicationScenario(), monitoringData);
        persist(monitoringData.toRdfModel(), monitoringData.getApplicationScenario());
        triggerPostProcessors(monitoringData.getApplicationScenario(), monitoringData);
    }

    @SuppressWarnings("unchecked")
    public synchronized <D extends DataSource> void persist(final String rdfString, final Class<Type> clazz) {
        Type bean = RdfHelper.createMonitoringData(rdfString, (Class<? extends IMonitoringDataModel<Type, D>>) clazz);
        persist(bean);
    }

    public synchronized void persist(final Model monitoringData, final ApplicationScenario applicationScenario) {
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
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, count);
    }

    @Override
    public synchronized List<Type> getMonitoringData(final BusinessCase businessCase,
                                                     final Class<Type> clazz,
                                                     final TimeFrame timeFrame) {
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

    private List<Type> getData(BusinessCase businessCase, Class<Type> clazz, String selectQuery) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        if (clazz == null) {
            throw new NullPointerException("Clazz may not be null!");
        }

        final String query = SPARQLHelper.appendDefaultPrefixes(selectQuery);
        logger.debug(query);
        final Dataset dataSet = getDataSet(businessCase);

        return transactional(dataSet, new ArrayList<>(), () -> {
            final Model model = dataSet.getDefaultModel();
            return Sparql.exec(model, clazz, query)
                .stream()
                .map(type -> (Type) initLazyModel(model, type))
                .collect(Collectors.toList());
        });
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
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, timeFrame);
    }

    @Override
    public synchronized Type getMonitoringData(final BusinessCase businessCase,
                                               final Class<Type> clazz,
                                               final String identifier) {
        if (identifier == null) {
            throw new NullPointerException("Identifier may not be null!");
        }
        if (identifier.trim().length() == 0) {
            throw new IllegalArgumentException("Identifier may not be empty!");
        }

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
     * @see IMonitoringDataRepository#getMonitoringData(de.atb.context.common.util.ApplicationScenario, java.lang.Class, java.lang.String)
     */
    @Override
    public synchronized Type getMonitoringData(final ApplicationScenario applicationScenario,
                                               final Class<Type> clazz,
                                               final String identifier) {
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
        if (identifier == null) {
            throw new NullPointerException("Identifier may not be null!");
        }

        final String folder = "monitoring/";

        for (ApplicationScenario scenario : ApplicationScenario.values()) {
            if (identifier.equals(scenario.toString())) {
                return getStaticMonitoringData(applicationScenario, clazz, folder + identifier + ".rdf");
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
        if (clazz == null) {
            throw new NullPointerException("Clazz may not be null!");
        }
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

    private List<String> getIDs(BusinessCase businessCase, String selectQuery) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        if (selectQuery == null) {
            throw new NullPointerException("selectQuery may not be null!");
        }

        final String query = SPARQLHelper.appendDefaultPrefixes(selectQuery);
        logger.debug(query);
        final Dataset dataSet = getDataSet(businessCase);

        return transactional(dataSet, new ArrayList<>(), () -> {
            final Model model = dataSet.getDefaultModel();
            final Query q = QueryFactory.create(query);
            final QueryExecution qe = QueryExecutionFactory.create(q, model);
            final ResultSet rs = qe.execSelect();
            final List<String> ids = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                RDFNode node = qs.get("id");
                if ((node != null) && node.isLiteral()) {
                    ids.add(node.asLiteral().getString());
                }
            }
            return ids;
        });
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
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
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
        if (clazz == null) {
            throw new NullPointerException("Clazz may not be null!");
        }
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
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
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
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        if (queries == null) {
            throw new NullPointerException("Queries may not be null!");
        }
        if (queries.length <= 0) {
            throw new NullPointerException("Queries may not be empty!");
        }
        UpdateRequest ur = UpdateFactory.create();
        for (String query : queries) {
            ur.add(prepareSparqlQuery(query));
        }
        try {
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

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#executeSparqlSelectQuery(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ResultSet executeSparqlSelectQuery(final BusinessCase businessCase, final String query) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        if (query == null) {
            throw new NullPointerException("Query may not be null!");
        }
        if (query.trim().length() == 0) {
            throw new IllegalArgumentException("Query may not be empty!");
        }

        final String finalQuery = prepareSparqlQuery(query);
        final Dataset dataset = getDataSet(businessCase);

        return transactional(dataset, null, () -> {
            final Model model = dataset.getDefaultModel();
            final OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
            final QueryExecution qexec = QueryExecutionFactory.create(finalQuery, ontModel);
            return qexec.execSelect();
        });
    }

    public static synchronized String prepareSparqlQuery(final String query) {
        logger.trace("Preparing sparql query '" + query);
        String finalQuery = OntologyNamespace.prepareSparqlQuery(query);
        logger.trace("Final query is " + finalQuery);
        return finalQuery;
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

    private void validateTimeFrame(TimeFrame timeFrame) {
        if (timeFrame == null) {
            throw new NullPointerException("TimeFrame may not be null!");
        }
        if ((timeFrame.getStartTime() == null) && (timeFrame.getEndTime() == null)) {
            throw new IllegalArgumentException("TimeFrame has to have at least a start date or an and date!");
        }
    }
}
