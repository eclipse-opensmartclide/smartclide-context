package org.eclipse.opensmartclide.context.services;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.extraction.ContextContainer;
import org.eclipse.opensmartclide.context.extraction.ContextContainerWrapper;
import org.eclipse.opensmartclide.context.persistence.ModelOutputLanguage;
import org.eclipse.opensmartclide.context.persistence.context.ContextRepository;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.eclipse.opensmartclide.context.services.interfaces.Input;
import org.eclipse.opensmartclide.context.persistence.context.IContextRepository;
import org.eclipse.opensmartclide.context.services.interfaces.IRepositoryService;
import org.eclipse.opensmartclide.context.services.interfaces.Output;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.tools.datalayer.models.OutputDataModel;
import org.eclipse.opensmartclide.context.tools.ontology.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextRepositoryService
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
public class ContextRepositoryService extends PersistenceUnitService<ContextContainer, ContextRepository> implements
        IContextRepositoryService {

    protected static final Logger logger = LoggerFactory.getLogger(ContextRepositoryService.class);
    protected IContextRepositoryService service;

    @Override
    public String invokeForData(String s) throws ContextFault {
        return null;
    }

    @Override
    public void setOutputModel(OutputDataModel outputDataModel) throws ContextFault {

    }

    @Override
    public void setOutputIds(ArrayList<String> arrayList) throws ContextFault {

    }

    @Override
    public boolean setupRepos(String s, int i, String s1, String s2, OutputDataModel outputDataModel, ArrayList<String> arrayList, String s3) throws ContextFault {
        return false;
    }

    @Override
    public boolean startPES() throws ContextFault {
        return false;
    }

    protected static final ModelOutputLanguage serializer = ModelOutputLanguage.TURTLE;

    public ContextRepositoryService() {
        super(ContextRepository.getInstance());
    }

    @Override
    public final void start() throws ContextFault {
        ContextRepositoryService.logger.info(String.format("Starting %s ...", getClass().getSimpleName()));
    }

    @Override
    public final void stop() throws ContextFault {
        ContextRepositoryService.logger.info(String.format("Stopping %s ...", getClass().getSimpleName()));
    }

    @Override
    public final void restart() throws ContextFault {
        ContextRepositoryService.logger.info(String.format("Restarting %s ...", getClass().getSimpleName()));
    }

    @Override
    public final String ping() throws ContextFault {
        ContextRepositoryService.logger.info(String.format("%s was pinged", getClass().getSimpleName()));
        return ServiceManager.PING_RESPONSE;
    }

    public final Output invokeS(final Input input) throws ContextFault {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public final Output invokeA(final Input input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public final Future<?> invokeAAsync(final Input input, final AsyncHandler<?> asyncHandler) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public final Response<?> invokeAAsync(final Input input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#persistContext(ContextContainerWrapper)
     */
    @Override
    public final synchronized void persistContext(final ContextContainerWrapper wrapper) throws ContextFault {
        if (wrapper != null) {
            ContextContainer context = ContextContainerWrapper.toContextContainer(wrapper);
            repos.persist(context);
        }
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getContext(ApplicationScenario, java.lang.String)
     */
    @Override
    public final synchronized ContextContainerWrapper getContext(final ApplicationScenario applicationScenario, final String contextId)
            throws ContextFault {
        ContextContainer container = repos.getContext(applicationScenario, contextId);
        if (container != null) {
            return ContextContainerWrapper.fromContextContainer(container);
        }
        return null;
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getContext(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized ContextContainerWrapper getContext(final BusinessCase businessCase, final String contextId) throws ContextFault {
        ContextContainer container = repos.getContext(businessCase, contextId);
        if (container != null) {
            return ContextContainerWrapper.fromContextContainer(container);
        }
        return null;
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getRawContext(ApplicationScenario, java.lang.String)
     */
    @Override
    public final synchronized ContextContainerWrapper getRawContext(final ApplicationScenario applicationScenario, final String contextId)
            throws ContextFault {
        ContextContainer context = repos.getRawContext(applicationScenario, contextId);
        if (context != null) {
            return ContextContainerWrapper.fromContextContainer(context);
        }
        return null;
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getRawContext(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized ContextContainerWrapper getRawContext(final BusinessCase businessCase, final String contextId) throws ContextFault {
        ContextContainer context = repos.getRawContext(businessCase, contextId);
        if (context != null) {
            return ContextContainerWrapper.fromContextContainer(context);
        }
        return null;
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlAskQuery(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized Boolean executeSparqlAskQuery(final BusinessCase businessCase, final String query) throws ContextFault {
        return repos.executeSparqlAskQuery(businessCase, query);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlSelectQuery(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized String executeSparqlSelectQuery(final BusinessCase businessCase, final String query) throws ContextFault {
        ResultSet results = repos.executeSparqlSelectQuery(businessCase, query);
        return ResultSetFormatter.asXMLString(results);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlDescribeQuery(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized String executeSparqlDescribeQuery(final BusinessCase businessCase, final String query) throws ContextFault {
        try {
            Model model = repos.executeSparqlDescribeQuery(businessCase, query);
            return getModelAsString(model, businessCase);
        } catch (Throwable t) {
            throw new ContextFault(t);
        }
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlConstructQuery(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized String executeSparqlConstructQuery(final BusinessCase businessCase, final String query) throws ContextFault {
        try {
            Model model = repos.executeSparqlConstructQuery(businessCase, query);
            return getModelAsString(model, businessCase);
        } catch (Throwable t) {
            throw new ContextFault(t);
        }
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlAskQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public final synchronized Boolean executeSparqlAskQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
            throws ContextFault {
        return repos.executeSparqlAskQuery(businessCase, query, useReasoner);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlSelectQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public final synchronized String executeSparqlSelectQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
            throws ContextFault {
        ResultSet results = repos.executeSparqlSelectQuery(businessCase, query, useReasoner);
        return ResultSetFormatter.asXMLString(results);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlDescribeQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public final synchronized String executeSparqlDescribeQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
            throws ContextFault {
        try {
            Model model = repos.executeSparqlDescribeQuery(businessCase, query, useReasoner);
            return getModelAsString(model, businessCase);
        } catch (Throwable t) {
            throw new ContextFault(t);
        }
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#executeSparqlConstructQuery(BusinessCase, java.lang.String, boolean)
     */
    @Override
    public final synchronized String executeSparqlConstructQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
            throws ContextFault {
        try {
            Model model = repos.executeSparqlConstructQuery(businessCase, query, useReasoner);
            return getModelAsString(model, businessCase);
        } catch (Throwable t) {
            throw new ContextFault(t);
        }
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#getLastContextsIds(BusinessCase, int)
     */
    @Override
    public final synchronized List<String> getLastContextsIds(final BusinessCase businessCase, final int count) throws ContextFault {
        return repos.getLastContextsIds(businessCase, count);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#getLastContextsIds(BusinessCase, TimeFrame)
     */
    @Override
    public final synchronized List<String> getLastContextsIds(final BusinessCase businessCase, final TimeFrame timeFrame) throws ContextFault {
        return repos.getLastContextsIds(businessCase, timeFrame);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getLastContextsIds(ApplicationScenario, int)
     */
    @Override
    public final synchronized List<String> getLastContextsIds(final ApplicationScenario applicationScenario, final int count) throws ContextFault {
        return repos.getLastContextsIds(applicationScenario, count);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getLastContextsIds(ApplicationScenario, TimeFrame)
     */
    @Override
    public final synchronized List<String> getLastContextsIds(final ApplicationScenario applicationScenario, final TimeFrame timeFrame)
            throws ContextFault {
        return repos.getLastContextsIds(applicationScenario, timeFrame);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepository#initializeRepository(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized void initializeRepository(final BusinessCase bc, final String modelUri) throws ContextFault {
        repos.initializeRepository(bc, modelUri);
    }

    /**
	 * (non-Javadoc)
	 *
	 * @see IContextRepositoryService#getDefaultModel(BusinessCase)
     */
    @Override
    public final synchronized String getDefaultModel(final BusinessCase businessCase) throws ContextFault {
        try {
            Model model = repos.getDefaultModel(businessCase);
            return getModelAsString(model, businessCase);
        } catch (Throwable t) {
            throw new ContextFault(t);
        }
    }

    protected final synchronized String getModelAsString(final Model model, final BusinessCase businessCase) {
        return ContextRepositoryService.serializer.getModelAsString(model);
    }

    public final <T extends Configuration> boolean configureService(final T Configuration) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * (non-Javadoc)
     *
     * @see IRepositoryService#store(java.lang.Object)
     */
    @Override
    public final String store(final Object Element) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * (non-Javadoc)
     *
     * @see IRepositoryService#remove(java.lang.Object)
     */
    @Override
    public final boolean remove(final Object Element) {
        // TODO Auto-generated method stub
        return false;
    }

}
