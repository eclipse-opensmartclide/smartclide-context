package de.atb.context.persistence.context;

/*
 * #%L
 * ATB Context Extraction Core Service
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
import de.atb.context.common.util.IApplicationScenarioProvider;
import de.atb.context.common.util.TimeFrame;
import de.atb.context.context.util.OntologyNamespace;
import de.atb.context.extraction.ContextContainer;
import de.atb.context.extraction.util.base.BaseDatatypeProperties;
import de.atb.context.extraction.util.base.BaseOntologyClasses;
import de.atb.context.persistence.common.RepositoryTDB;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ContextRepository
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 */
@Setter
@Getter
public final class ContextRepository extends RepositoryTDB<ContextContainer> implements IContextRepository {

    private static final Logger logger = LoggerFactory.getLogger(ContextRepository.class);
    private static final String internalBaseUri = "contexts";
    @Getter
    private static final ContextRepository instance;
    private final boolean writeRawContextFiles = true;

    static {
        synchronized (ContextRepository.class) {
            instance = new ContextRepository();
        }
    }

    private ContextRepository() {
        super(ContextRepository.internalBaseUri);
    }

    private synchronized void persistNamedContext(ContextContainer context) {
        Model named = getDataSet(context.getBusinessCase()).getNamedModel(context.getIdentifier());
        named.removeAll();
        named.add(context);
        named.close();
    }

    private synchronized void persistReasonableContext(ContextContainer context) {
        Dataset ds = getDataSet(context.getBusinessCase());
        Model model = ds.getDefaultModel();
        model.add(context);
    }

    private synchronized void persistRawContext(ContextContainer context) {
        if (writeRawContextFiles) {
            String fileLocation = getLocationForBusinessCase(context.getBusinessCase());
            new File(fileLocation).mkdirs();
            String fileName = String.format("%s%s%s.owl", fileLocation, File.separator, context.getIdentifier());
            File modelFile = new File(fileName);
            try {
                if (modelFile.exists() || modelFile.createNewFile()) {
                    ContextRepository.logger.debug(String.format("Writing raw context with id '%s' to '%s'", context.getIdentifier(),
                        modelFile.getAbsolutePath()));
                    context.writeToFile(modelFile.getAbsolutePath());
                }
            } catch (IOException e) {
                ContextRepository.logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getRawContext(de.atb.context.common.util.ApplicationScenario, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getRawContext(ApplicationScenario applicationScenario, String contextId) {
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
        return getRawContext(applicationScenario.getBusinessCase(), contextId);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getRawContext(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getRawContext(BusinessCase businessCase, String contextId) {
        if (contextId == null) {
            throw new NullPointerException("ContextId may not be null!");
        }
        if (contextId.trim().length() == 0) {
            throw new IllegalArgumentException("ContextId may not be empty!");
        }
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }

        String fileLocation = getLocationForBusinessCase(businessCase);
        String fileName = String.format("%s%s%s.owl", fileLocation, File.separator, contextId);
        File modelFile = new File(fileName);
        if (modelFile.canRead()) {
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            String absPath = String.valueOf(modelFile.toURI());
            model.read(absPath);
            ContextContainer context = new ContextContainer(model, false);
            context.setIdentifier(contextId);
            return prepareRawContextContainer(context);
        } else {
            return null;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getContext(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getContext(ApplicationScenario applicationScenario, String contextId) {
        if (contextId == null) {
            throw new NullPointerException("ContextId may not be null!");
        }
        if (contextId.trim().length() == 0) {
            throw new IllegalArgumentException("ContextId may not be empty!");
        }
        if (applicationScenario == null) {
            throw new NullPointerException("ApplicationScenario may not be null!");
        }
        if (applicationScenario.getBusinessCase() == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        return getRawContext(applicationScenario, contextId);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IContextRepositoryService#getContext(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getContext(BusinessCase businessCase, String contextId) {
        if (contextId == null) {
            throw new NullPointerException("ContextId may not be null!");
        }
        if (contextId.trim().length() == 0) {
            throw new IllegalArgumentException("ContextId may not be empty!");
        }
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }

        Model model = getDataSet(businessCase).getNamedModel(contextId);
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.add(model);

        ContextContainer context = new ContextContainer(ontModel);
        context.addDefaultNamespaces();
        context.setIdentifier(contextId);
        return prepareRawContextContainer(context);
    }

    private ContextContainer getStaticContext(ApplicationScenario appScenario, String identifier) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        URL fileUri = Thread.currentThread().getContextClassLoader().getResource(identifier);
        if (fileUri != null) {
            model.read(fileUri.toString());
            ContextContainer cc = new ContextContainer(appScenario, model, false);
            prepareRawContextContainer(cc);
            return cc;
        }
        return null;
    }

    private ContextContainer getStaticContext(BusinessCase businessCase, String filePath, String identifier) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        URL fileUri = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (fileUri != null) {
            model.read(fileUri.toString());
            ContextContainer cc = new ContextContainer(model, false);
            cc.setIdentifier(identifier);
            prepareRawContextContainer(cc);
            return cc;
        }
        return null;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlSelectQuery(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ResultSet executeSparqlSelectQuery(BusinessCase businessCase, String query) {
        return executeSparqlSelectQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlDescribeQuery(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized Model executeSparqlDescribeQuery(BusinessCase businessCase, String query) {
        return executeSparqlDescribeQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlConstructQuery(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized Model executeSparqlConstructQuery(BusinessCase businessCase, String query) {
        return executeSparqlConstructQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlAskQuery(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized Boolean executeSparqlAskQuery(BusinessCase businessCase, String query) {
        return executeSparqlAskQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlAskQuery(de.atb.context.common.util.BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized Boolean executeSparqlAskQuery(BusinessCase businessCase, String query, boolean useReasoner) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        Dataset dataset = getDataSet(businessCase);
        QueryExecution qexec = getQueryExecution(businessCase, query, useReasoner, dataset);
        Boolean result = null;
        try {
            result = qexec.execAsk();
        } catch (QueryException qe) {
            ContextRepository.logger.error(qe.getMessage(), qe);
        }
        dataset.commit();
        dataset.end();
        dataset.close();
        return result;
    }

    private QueryExecution getQueryExecution(BusinessCase businessCase, String query, boolean useReasoner, Dataset dataset) {
        if (query == null) {
            throw new NullPointerException("Query may not be null!");
        }
        if (query.trim().length() == 0) {
            throw new IllegalArgumentException("Query may not be empty!");
        }
        String finalQuery = prepareSparqlQuery(businessCase, query);
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        OntModel ontModel = null;
        if (useReasoner) {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model);
        } else {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        }
        QueryExecution qexec = QueryExecutionFactory.create(finalQuery, ontModel);
        return qexec;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlSelectQuery(de.atb.context.common.util.BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized ResultSet executeSparqlSelectQuery(BusinessCase businessCase, String query, boolean useReasoner) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        Dataset dataset = getDataSet(businessCase);
        QueryExecution qexec = getQueryExecution(businessCase, query, useReasoner, dataset);
        ResultSet result = null;
        try {
            result = qexec.execSelect();
        } catch (QueryException qe) {
            ContextRepository.logger.error(qe.getMessage(), qe);
        }
        dataset.commit();
        dataset.end();
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlDescribeQuery(de.atb.context.common.util.BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized Model executeSparqlDescribeQuery(BusinessCase businessCase, String query, boolean useReasoner) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        Dataset dataset = getDataSet(businessCase);
        QueryExecution qexec = getQueryExecution(businessCase, query, useReasoner, dataset);
        Model result = null;
        try {
            result = qexec.execDescribe();
        } catch (QueryException qe) {
            ContextRepository.logger.error(qe.getMessage(), qe);
        }
        dataset.commit();
        dataset.end();
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#executeSparqlConstructQuery(de.atb.context.common.util.BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized Model executeSparqlConstructQuery(BusinessCase businessCase, String query, boolean useReasoner) {
        if (businessCase == null) {
            throw new NullPointerException("BusinessCase may not be null!");
        }
        Dataset dataset = getDataSet(businessCase);
        QueryExecution qexec = getQueryExecution(businessCase, query, useReasoner, dataset);
        Model result = null;
        try {
            result = qexec.execConstruct();
        } catch (QueryException qe) {
            ContextRepository.logger.error(qe.getMessage(), qe);
        }
        dataset.commit();
        dataset.end();
        return result;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#initializeRepository(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public synchronized void initializeRepository(BusinessCase bc, String modelUri) {
        ContextRepository.logger.info(String.format("Initializing repository for BusinessCase '%s', loading from url '%s'", bc, modelUri));
        createDefaultModel(OntModel.class, bc, modelUri, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getDefaultModel(de.atb.context.common.util.BusinessCase)
     */
    @Override
    public synchronized Model getDefaultModel(BusinessCase businessCase) {
        Dataset dataset = getDataSet(businessCase);
        return dataset.getDefaultModel();
    }

    private synchronized ContextContainer prepareRawContextContainer(ContextContainer container) {
        ApplicationScenario applicationScenario = container.inferApplicationScenario();
        container.setApplicationScenario(applicationScenario);

        Date capturedAt = container.inferCapturedAt();
        container.setCapturedAt(capturedAt);

        String monitoringDataId = container.inferMonitoringDataId();
        if (monitoringDataId != null) {
            container.setMonitoringDataId(monitoringDataId);
        }
        return container;
    }

    private synchronized String prepareSparqlQuery(BusinessCase businessCase, String query) {
        ContextRepository.logger.debug("Preparing sparql query '" + query + "' for business case " + businessCase);
        String finalQuery = OntologyNamespace.prepareSparqlQuery(query);
        ContextRepository.logger.debug("Final query is " + finalQuery);
        return finalQuery;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getLastContextsIds(de.atb.context.common.util.ApplicationScenario, int)
     */
    @Override
    public synchronized List<String> getLastContextsIds(ApplicationScenario applicationScenario, int count) {
        String queryString = String
            .format("SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x} ORDER BY DESC(?x) LIMIT %6$d",
                BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, BaseDatatypeProperties.ApplicationScenarioIdentifier, applicationScenario.toString(), BaseDatatypeProperties.CapturedAt,
                count);

        List<String> ids = new ArrayList<>();
        try {
            ids = getLastIds(queryString, applicationScenario.getBusinessCase());
        } catch (QueryException e) {
            ContextRepository.logger.error(e.getMessage(), e);
        }
        return ids;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getLastContextsIds(de.atb.context.common.util.ApplicationScenario, de.atb.context.common.util.TimeFrame)
     */
    @Override
    public synchronized List<String> getLastContextsIds(ApplicationScenario applicationScenario, TimeFrame timeFrame) {
        String queryString = String
            .format("SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x . ?x <= %6$s . ?x >= %7$s} ORDER BY DESC(?x)",
                BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, BaseDatatypeProperties.ApplicationScenarioIdentifier, applicationScenario.toString(), BaseDatatypeProperties.CapturedAt,
                timeFrame.getXSDLexicalFormForStartTime(), timeFrame.getXSDLexicalFormForEndTime());

        List<String> ids = new ArrayList<>();
        try {
            ids = getLastIds(queryString, applicationScenario.getBusinessCase());
        } catch (QueryException e) {
            ContextRepository.logger.error(e.getMessage(), e);
        }
        return ids;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getLastContextsIds(de.atb.context.common.util.BusinessCase, int)
     */
    @Override
    public synchronized List<String> getLastContextsIds(BusinessCase bc, int count) {
        String queryString = String
            .format("SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x} ORDER BY DESC(?x) LIMIT %6$d",
                BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, BaseDatatypeProperties.BusinessCaseIdentifier, bc.toString(), BaseDatatypeProperties.CapturedAt, count);

        List<String> ids = new ArrayList<>();
        try {
            ids = getLastIds(queryString, bc);
        } catch (QueryException e) {
            ContextRepository.logger.error(e.getMessage(), e);
        }
        return ids;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.context.IContextRepository#getLastContextsIds(de.atb.context.common.util.BusinessCase, de.atb.context.common.util.TimeFrame)
     */
    @Override
    public synchronized List<String> getLastContextsIds(BusinessCase bc, TimeFrame timeFrame) {
        String queryString = String
            .format("SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x . ?x <= %6$s . ?x >= %7$s} ORDER BY DESC(?x)",
                BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, BaseDatatypeProperties.BusinessCaseIdentifier, bc.toString(), BaseDatatypeProperties.CapturedAt, timeFrame.getXSDLexicalFormForStartTime(),
                timeFrame.getXSDLexicalFormForEndTime());

        List<String> ids = new ArrayList<>();
        try {
            ids = getLastIds(queryString, bc);
        } catch (QueryException e) {
            ContextRepository.logger.error(e.getMessage(), e);
        }
        return ids;
    }

    private synchronized List<String> getLastIds(String queryString, BusinessCase bc) throws QueryException {
        String finalQuery = prepareSparqlQuery(bc, queryString);

        List<String> ids = new ArrayList<>();
        Dataset ds = getDataSet(bc);
        ds.begin(ReadWrite.WRITE);
        ResultSet set = executeSelectSparqlQuery(finalQuery, ds.getDefaultModel());
        while (set.hasNext()) {
            QuerySolution solution = set.nextSolution();
            Literal literal = solution.getLiteral("identifier");
            if (literal != null) {
                ids.add(literal.getString());
            }
        }
        ds.commit();
        ds.end();
        return ids;
    }

    private ResultSet executeSelectSparqlQuery(String sparqlQuery, Model model) {
        Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ContextRepository.logger.debug("Executing SparQL select query '" + query + "'");
        return qexec.execSelect();
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.persistence.common.IPersistenceUnit#persist(IApplicationScenarioProvider)
     */
    @Override
    public void persist(ContextContainer context) {
        if (context == null) {
            throw new NullPointerException("Context may not be null!");
        }

        if (context.getApplicationScenario() == null) {
            throw new NullPointerException("ApplicationScenario for Context may not be null!");
        }

        if (context.getBusinessCase() == null) {
            throw new NullPointerException("BusinessCase for Context may not be null!");
        }

        if (context.getIdentifier() == null) {
            throw new NullPointerException("Context Identifier may not be null!");
        }

        if (context.getIdentifier().trim().length() < 1) {
            throw new IllegalArgumentException("Context Identifier may not be empty!");
        }
        ContextRepository.logger.debug("Persisting context '" + context.getIdentifier() + " for " + context.getApplicationScenario() + " in BC "
            + context.getBusinessCase());
        triggerPreProcessors(context.getApplicationScenario(), context);
        persistRawContext(context);
        persistReasonableContext(context);
        persistNamedContext(context);
        triggerPostProcessors(context.getApplicationScenario(), context);
    }
}
