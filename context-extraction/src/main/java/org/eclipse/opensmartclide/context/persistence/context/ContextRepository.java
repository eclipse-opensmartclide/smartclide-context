package org.eclipse.opensmartclide.context.persistence.context;

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


import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.IApplicationScenarioProvider;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.extraction.ContextContainer;
import org.eclipse.opensmartclide.context.extraction.util.base.BaseDatatypeProperties;
import org.eclipse.opensmartclide.context.extraction.util.base.BaseOntologyClasses;
import org.eclipse.opensmartclide.context.persistence.common.RepositoryTDB;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.opensmartclide.context.persistence.common.IPersistenceUnit;
import org.eclipse.opensmartclide.context.services.IContextRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getRawContext(ApplicationScenario, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getRawContext(ApplicationScenario applicationScenario, String contextId) {
        validateNotNull(applicationScenario, "applicationScenario");
        validateString(contextId, "contextId");

        return getRawContext(applicationScenario.getBusinessCase(), contextId);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getRawContext(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getRawContext(BusinessCase businessCase, String contextId) {
        validateNotNull(businessCase, "businessCase");
        validateString(contextId, "contextId");

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
     * @see IContextRepository#getContext(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getContext(ApplicationScenario applicationScenario, String contextId) {
        validateNotNull(applicationScenario, "applicationScenario");
        validateNotNull(applicationScenario.getBusinessCase(), "BusinessCase for ApplicationScenario");
        validateString(contextId, "contextId");

        return getRawContext(applicationScenario, contextId);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepositoryService#getContext(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized ContextContainer getContext(BusinessCase businessCase, String contextId) {
        validateNotNull(businessCase, "businessCase");
        validateString(contextId, "contextId");

        Model model = getDataSet(businessCase).getNamedModel(contextId);
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.add(model);

        ContextContainer context = new ContextContainer(ontModel);
        context.addDefaultNamespaces();
        context.setIdentifier(contextId);
        return prepareRawContextContainer(context);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getLastContextsIds(ApplicationScenario, int)
     */
    @Override
    public synchronized List<String> getLastContextsIds(ApplicationScenario applicationScenario, int count) {
        validateNotNull(applicationScenario, "applicationScenario");
        if (count < 0) {
            throw new IllegalArgumentException("Count has to be > -1!");
        }

        String queryString = String.format(
            "SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x} ORDER BY DESC(?x) LIMIT %6$d",
            BaseOntologyClasses.Context,
            BaseDatatypeProperties.Identifier,
            BaseDatatypeProperties.ApplicationScenarioIdentifier,
            applicationScenario,
            BaseDatatypeProperties.CapturedAt,
            count
        );

        return getLastIds(queryString, applicationScenario.getBusinessCase());
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getLastContextsIds(ApplicationScenario, TimeFrame)
     */
    @Override
    public synchronized List<String> getLastContextsIds(ApplicationScenario applicationScenario, TimeFrame timeFrame) {
        validateNotNull(applicationScenario, "applicationScenario");
        validateTimeFrame(timeFrame);

        String queryString = String.format(
            "SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x . ?x <= %6$s . ?x >= %7$s} ORDER BY DESC(?x)",
            BaseOntologyClasses.Context,
            BaseDatatypeProperties.Identifier,
            BaseDatatypeProperties.ApplicationScenarioIdentifier,
            applicationScenario,
            BaseDatatypeProperties.CapturedAt,
            timeFrame.getXSDLexicalFormForStartTime(),
            timeFrame.getXSDLexicalFormForEndTime()
        );

        return getLastIds(queryString, applicationScenario.getBusinessCase());
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getLastContextsIds(BusinessCase, int)
     */
    @Override
    public synchronized List<String> getLastContextsIds(BusinessCase businessCase, int count) {
        validateNotNull(businessCase, "businessCase");
        if (count < 0) {
            throw new IllegalArgumentException("Count has to be > -1!");
        }

        String queryString = String.format(
            "SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x} ORDER BY DESC(?x) LIMIT %6$d",
            BaseOntologyClasses.Context,
            BaseDatatypeProperties.Identifier,
            BaseDatatypeProperties.BusinessCaseIdentifier,
            businessCase,
            BaseDatatypeProperties.CapturedAt,
            count
        );

        return getLastIds(queryString, businessCase);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getLastContextsIds(BusinessCase, TimeFrame)
     */
    @Override
    public synchronized List<String> getLastContextsIds(BusinessCase businessCase, TimeFrame timeFrame) {
        validateNotNull(businessCase, "businessCase");
        validateTimeFrame(timeFrame);

        String queryString = String.format(
            "SELECT ?identifier WHERE {?c rdf:type :%1$s . ?c :%2$s ?identifier . ?c :%3$s \"%4$s\"^^xsd:string . ?c :%5$s ?x . ?x <= %6$s . ?x >= %7$s} ORDER BY DESC(?x)",
            BaseOntologyClasses.Context,
            BaseDatatypeProperties.Identifier,
            BaseDatatypeProperties.BusinessCaseIdentifier,
            businessCase,
            BaseDatatypeProperties.CapturedAt,
            timeFrame.getXSDLexicalFormForStartTime(),
            timeFrame.getXSDLexicalFormForEndTime()
        );

        return getLastIds(queryString, businessCase);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#persist(IApplicationScenarioProvider)
     */
    @Override
    public void persist(ContextContainer context) {
        validateNotNull(context, "context");
        validateNotNull(context.getApplicationScenario(), "ApplicationScenario for Context");
        validateNotNull(context.getBusinessCase(), "BusinessCase for Context");
        validateNotNull(context.getIdentifier(), "Context Identifier");
        validateString(context.getIdentifier(), "Context Identifier");

        logger.debug(
            "Persisting context '{}' for {} in BC {}",
            context.getIdentifier(),
            context.getApplicationScenario(),
            context.getBusinessCase()
        );
        triggerPreProcessors(context.getApplicationScenario(), context);
        persistRawContext(context);
        persistReasonableContext(context);
        persistNamedContext(context);
        triggerPostProcessors(context.getApplicationScenario(), context);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#executeSparqlDescribeQuery(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized Model executeSparqlDescribeQuery(BusinessCase businessCase, String query) {
        return executeSparqlDescribeQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#executeSparqlDescribeQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized Model executeSparqlDescribeQuery(BusinessCase businessCase, String query, boolean useReasoner) {
        validateNotNull(businessCase, "businessCase");
        validateString(query, "query");

        final Dataset dataset = getDataSet(businessCase);
        return transactional(dataset, null, () -> getQueryExecution(query, useReasoner, dataset).execDescribe());
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#executeSparqlConstructQuery(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized Model executeSparqlConstructQuery(BusinessCase businessCase, String query) {
        return executeSparqlConstructQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#executeSparqlConstructQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized Model executeSparqlConstructQuery(BusinessCase businessCase,
                                                          String query,
                                                          boolean useReasoner) {
        validateNotNull(businessCase, "businessCase");
        validateString(query, "query");

        final Dataset dataset = getDataSet(businessCase);
        return transactional(dataset, null, () -> getQueryExecution(query, useReasoner, dataset).execConstruct());
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#executeSparqlAskQuery(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized Boolean executeSparqlAskQuery(BusinessCase businessCase, String query) {
        return executeSparqlAskQuery(businessCase, query, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#executeSparqlAskQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public synchronized Boolean executeSparqlAskQuery(BusinessCase businessCase, String query, boolean useReasoner) {
        validateNotNull(businessCase, "businessCase");
        validateString(query, "query");

        final Dataset dataset = getDataSet(businessCase);
        return transactional(dataset, null, () -> getQueryExecution(query, useReasoner, dataset).execAsk());
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#initializeRepository(BusinessCase, java.lang.String)
     */
    @Override
    public synchronized void initializeRepository(BusinessCase businessCase, String modelUri) {
        validateNotNull(businessCase, "businessCase");
        validateString(modelUri, "modelUri");

        logger.info("Initializing repository for BusinessCase '{}', loading from url '{}'", businessCase, modelUri);
        createDefaultModel(OntModel.class, businessCase, modelUri, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see IContextRepository#getDefaultModel(BusinessCase)
     */
    @Override
    public synchronized Model getDefaultModel(BusinessCase businessCase) {
        validateNotNull(businessCase, "businessCase");

        Dataset dataset = getDataSet(businessCase);
        return dataset.getDefaultModel();
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
            if (new File(fileLocation).mkdirs()) {
                String fileName = String.format("%s%s%s.owl", fileLocation, File.separator, context.getIdentifier());
                File modelFile = new File(fileName);
                try {
                    if (modelFile.exists() || modelFile.createNewFile()) {
                        logger.debug(
                            "Writing raw context with id '{}' to '{}'",
                            context.getIdentifier(),
                            modelFile.getAbsolutePath()
                        );
                        context.writeToFile(modelFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                throw new ContextFault("Location for context repository could not be created");
            }
        }
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

    private synchronized List<String> getLastIds(String queryString, BusinessCase businessCase) throws QueryException {
        final String query = prepareSparqlQuery(queryString);
        return getIds(businessCase, query, "identifier");
    }
}
