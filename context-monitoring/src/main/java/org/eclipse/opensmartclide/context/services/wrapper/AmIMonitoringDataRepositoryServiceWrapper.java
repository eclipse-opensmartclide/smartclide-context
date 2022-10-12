package org.eclipse.opensmartclide.context.services.wrapper;

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


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.IApplicationScenarioProvider;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.rdf.RdfHelper;
import org.eclipse.opensmartclide.context.persistence.common.IPersistenceUnit;
import org.eclipse.opensmartclide.context.persistence.monitoring.IMonitoringDataRepository;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistencePostProcessor;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistencePreProcessor;
import org.eclipse.opensmartclide.context.services.IAmIMonitoringDataRepositoryService;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;

/**
 * MonitoringDataRepositoryWrapper
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 239 $
 */
public class AmIMonitoringDataRepositoryServiceWrapper<Type extends IMonitoringDataModel<?, ?>> extends
    RepositoryServiceWrapper<IAmIMonitoringDataRepositoryService<Type>> implements IMonitoringDataRepository<Type> {

    public AmIMonitoringDataRepositoryServiceWrapper(final IAmIMonitoringDataRepositoryService<Type> service) throws IllegalArgumentException {
        super(service);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(ApplicationScenario, java.lang.Class, int)
     */
    @Override
    public final synchronized List<String> getLastIds(final ApplicationScenario applicationScenario, final Class<Type> clazz, final int count)
        throws ContextFault {
        return service.getLastIds(applicationScenario, clazz.getName(), count);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(BusinessCase, java.lang.Class, int)
     */
    @Override
    public final synchronized List<String> getLastIds(final BusinessCase businessCase, final Class<Type> clazz, final int count) throws ContextFault {
        return service.getLastIds(businessCase, clazz.getName(), count);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(ApplicationScenario, java.lang.Class, TimeFrame)
     */
    @Override
    public final synchronized List<String> getLastIds(final ApplicationScenario applicationScenario, final Class<Type> clazz, final TimeFrame timeFrame)
        throws ContextFault {
        return service.getLastIds(applicationScenario, clazz.getName(), timeFrame);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getLastIds(BusinessCase, java.lang.Class, TimeFrame)
     */
    @Override
    public final synchronized List<String> getLastIds(final BusinessCase businessCase, final Class<Type> clazz, final TimeFrame timeFrame) throws ContextFault {
        return service.getLastIds(businessCase, clazz.getName(), timeFrame);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(ApplicationScenario, java.lang.Class, int)
     */
    @Override
    public final List<Type> getMonitoringData(final ApplicationScenario applicationScenario, final Class<Type> clazz, final int count) throws ContextFault {
        List<String> modelStrings = service.getMonitoringData(applicationScenario, clazz.getName(), count);
        return convertStringsToModels(modelStrings, clazz);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(BusinessCase, java.lang.Class, int)
     */
    @Override
    public final List<Type> getMonitoringData(final BusinessCase businessCase, final Class<Type> clazz, final int count) throws ContextFault {
        List<String> modelStrings = service.getMonitoringData(businessCase, clazz.getName(), count);
        return convertStringsToModels(modelStrings, clazz);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(ApplicationScenario, java.lang.Class, TimeFrame)
     */
    @Override
    public final List<Type> getMonitoringData(final ApplicationScenario applicationScenario, final Class<Type> clazz, final TimeFrame timeFrame)
        throws ContextFault {
        List<String> modelStrings = service.getMonitoringData(applicationScenario, clazz.getName(), timeFrame);
        return convertStringsToModels(modelStrings, clazz);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(BusinessCase, java.lang.Class, TimeFrame)
     */
    @Override
    public final List<Type> getMonitoringData(final BusinessCase businessCase, final Class<Type> clazz, final TimeFrame timeFrame) {
        List<String> modelStrings = service.getMonitoringData(businessCase, clazz.getName(), timeFrame);
        return convertStringsToModels(modelStrings, clazz);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(ApplicationScenario, java.lang.Class, java.lang.String)
     */
    @Override
    public final Type getMonitoringData(final ApplicationScenario applicationScenario, final Class<Type> clazz, final String identifier) throws ContextFault {
        String modelString = service.getMonitoringData(applicationScenario, clazz.getName(), identifier);
        return convertStringToModel(modelString, clazz);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#getMonitoringData(BusinessCase, java.lang.Class, java.lang.String)
     */
    @Override
    public final Type getMonitoringData(final BusinessCase businessCase, final Class<Type> clazz, final String identifier) {
        String modelString = service.getMonitoringData(businessCase, clazz.getName(), identifier);
        return convertStringToModel(modelString, clazz);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#executeSparqlSelectQuery(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized ResultSet executeSparqlSelectQuery(final BusinessCase businessCase, final String query) throws ContextFault {
        String resultSetXmlString = this.service.executeSparqlSelectQuery(businessCase, query);
        return ResultSetFactory.fromXML(IOUtils.toInputStream(resultSetXmlString, StandardCharsets.UTF_8));
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#executeSparqlUpdateQuery(BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized void executeSparqlUpdateQuery(final BusinessCase businessCase, final String query) throws ContextFault {
        this.service.executeSparqlUpdateQuery(businessCase, query);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#executeSparqlUpdateQueries(BusinessCase, java.lang.String[])
     */
    @Override
    public final synchronized void executeSparqlUpdateQueries(final BusinessCase businessCase, final String... queries) throws ContextFault {
        this.service.executeSparqlUpdateQueries(businessCase, queries);
    }

    /**
     * (non-Javadoc)
     *
     * @see IMonitoringDataRepository#reset(BusinessCase)
     */
    @Override
    public final boolean reset(final BusinessCase bc) {
        return service.reset(bc);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#persist(IApplicationScenarioProvider)
     */
    @Override
    public final void persist(final Type object) throws ContextFault {
        service.persist(object.toRdfString(), object.getClass().getName(), object.getApplicationScenario());
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#addPersistencePreProcessor(ApplicationScenario, IPersistencePreProcessor)
     */
    @Override
    public final boolean addPersistencePreProcessor(final ApplicationScenario scenario, final IPersistencePreProcessor<Type> preProcessor)
        throws ContextFault {
        return service.addPersistencePreProcessor(scenario, preProcessor.getId(), preProcessor.getClass().getName());
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#addPersistencePostProcessor(ApplicationScenario, IPersistencePostProcessor)
     */
    @Override
    public final boolean addPersistencePostProcessor(final ApplicationScenario scenario, final IPersistencePostProcessor<Type> postProcessor)
        throws ContextFault {
        return service.addPersistencePostProcessor(scenario, postProcessor.getId(), postProcessor.getClass().getName());
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#removePersistencePreProcessor(ApplicationScenario, IPersistencePreProcessor)
     */
    @Override
    public final boolean removePersistencePreProcessor(final ApplicationScenario scenario, final IPersistencePreProcessor<Type> preProcessor)
        throws ContextFault {
        return service.removePersistencePreProcessor(scenario, preProcessor.getId(), preProcessor.getClass().getName());
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#removePersistencePostProcessor(ApplicationScenario, IPersistencePostProcessor)
     */
    @Override
    public final boolean removePersistencePostProcessor(final ApplicationScenario scenario, final IPersistencePostProcessor<Type> postProcessor)
        throws ContextFault {
        return service.removePersistencePostProcessor(scenario, postProcessor.getId(), postProcessor.getClass().getName());
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#removePersistencePreProcessor(ApplicationScenario, java.lang.String)
     */
    @Override
    public final boolean removePersistencePreProcessor(final ApplicationScenario scenario, final String id) throws ContextFault {
        return service.removePersistencePreProcessor(scenario, id);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#removePersistencePostProcessor(ApplicationScenario, java.lang.String)
     */
    @Override
    public final boolean removePersistencePostProcessor(final ApplicationScenario scenario, final String id) throws ContextFault {
        return service.removePersistencePostProcessor(scenario, id);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#triggerPreProcessors(ApplicationScenario, IApplicationScenarioProvider)
     */
    @Override
    public final void triggerPreProcessors(final ApplicationScenario scenario, final Type object) {
        service.triggerPostProcessors(object.getApplicationScenario(), object.toRdfString(), object.getClass().getName());
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#triggerPostProcessors(ApplicationScenario, IApplicationScenarioProvider)
     */
    @Override
    public final void triggerPostProcessors(final ApplicationScenario scenario, final Type object) {
        service.triggerPostProcessors(object.getApplicationScenario(), object.toRdfString(), object.getClass().getName());
    }

    public final void persist(final String rdfString, final Class<Type> clazz, final ApplicationScenario applicationScenario) throws ContextFault {
        service.persist(rdfString, clazz.getName(), applicationScenario);
    }

    @SuppressWarnings("unchecked")
    protected final <T extends Type> List<T> convertStringsToModels(final List<String> modelStrings, final Class<T> clazz) {
        List<T> models;
        if (modelStrings != null) {
            models = new ArrayList<>(modelStrings.size());
            for (String modelString : modelStrings) {
                models.add((T) convertStringToModel(modelString, clazz));
            }
        } else {
            models = new ArrayList<>(0);
        }
        return models;
    }

    @SuppressWarnings("unchecked")
    protected final <T extends Type, D extends DataSource> Type convertStringToModel(final String modelString, final Class<T> clazz) {
        return RdfHelper.createMonitoringData(modelString, (Class<? extends IMonitoringDataModel<T, D>>) clazz);
    }

    @Override
    public void shutdown() {
        this.service.shutdown();
    }
}
