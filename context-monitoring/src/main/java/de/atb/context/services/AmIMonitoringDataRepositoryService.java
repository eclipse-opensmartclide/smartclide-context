package de.atb.context.services;

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
import de.atb.context.common.util.TimeFrame;
import de.atb.context.monitoring.models.IMonitoringDataModel;
import de.atb.context.persistence.monitoring.MonitoringDataRepository;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.manager.ServiceManager;
import de.atb.context.tools.datalayer.models.OutputDataModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * MonitoringDataRepositoryService
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 239 $
 */
public class AmIMonitoringDataRepositoryService<Type extends IMonitoringDataModel<?, ?>>
    extends PersistenceUnitService<Type, MonitoringDataRepository<Type>>
    implements IAmIMonitoringDataRepositoryService<Type> {

    private static final Logger logger = LoggerFactory.getLogger(AmIMonitoringDataRepositoryService.class);

    @SuppressWarnings("unchecked")
    public AmIMonitoringDataRepositoryService() {
        super((MonitoringDataRepository<Type>) MonitoringDataRepository.getInstance());
        ApplicationScenario.getInstance();
    }

    @Override
    public void setOutputIds(ArrayList<String> arrayList) throws ContextFault {
    }

    @Override
    public String invokeForData(String s) throws ContextFault {
        return null;
    }

    @Override
    public void setOutputModel(OutputDataModel outputDataModel) throws ContextFault {
    }

    @Override
    public boolean setupRepos(String host,
                              int port,
                              String className,
                              String pesId,
                              OutputDataModel outputDataModel,
                              ArrayList<String> arrayList,
                              String s3) throws ContextFault {
        return false;
    }

    @Override
    public boolean startPES() throws ContextFault {
        return false;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#persist(java.lang.String, java.lang.String, de.atb.context.common.util.ApplicationScenario)
     */
    @Override
    public final synchronized void persist(final String rdfString,
                                           final String clazz,
                                           final ApplicationScenario applicationScenario) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        this.repos.persist(rdfString, typeClass);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getMonitoringData(de.atb.context.common.util.BusinessCase, java.lang.String, java.lang.String)
     */
    @Override
    public final synchronized String getMonitoringData(final BusinessCase businessCase,
                                                       final String clazz,
                                                       final String identifier) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        Type model = this.repos.getMonitoringData(businessCase, typeClass, identifier);
        return (model != null) ? model.toRdfString() : "";
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getMonitoringData(de.atb.context.common.util.BusinessCase, java.lang.String, int)
     */
    @Override
    public final synchronized List<String> getMonitoringData(final BusinessCase businessCase,
                                                             final String clazz,
                                                             final int count) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        List<Type> models = this.repos.getMonitoringData(businessCase, typeClass, count);
        return toStringModels(models);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getMonitoringData(de.atb.context.common.util.BusinessCase, java.lang.String, de.atb.context.common.util.TimeFrame)
     */
    @Override
    public final synchronized List<String> getMonitoringData(final BusinessCase businessCase,
                                                             final String clazz,
                                                             final TimeFrame timeFrame) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        List<Type> models = this.repos.getMonitoringData(businessCase, typeClass, timeFrame);
        return toStringModels(models);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getMonitoringData(de.atb.context.common.util.ApplicationScenario, java.lang.String, int)
     */
    @Override
    public final synchronized List<String> getMonitoringData(final ApplicationScenario applicationScenario,
                                                             final String clazz,
                                                             final int count) throws ContextFault {
        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, count);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getMonitoringData(de.atb.context.common.util.ApplicationScenario, java.lang.String, de.atb.context.common.util.TimeFrame)
     */
    @Override
    public final synchronized List<String> getMonitoringData(final ApplicationScenario applicationScenario,
                                                             final String clazz,
                                                             final TimeFrame timeFrame) throws ContextFault {
        return getMonitoringData(applicationScenario.getBusinessCase(), clazz, timeFrame);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getMonitoringData(de.atb.context.common.util.ApplicationScenario, java.lang.String, java.lang.String)
     */
    @Override
    public final synchronized String getMonitoringData(final ApplicationScenario applicationScenario,
                                                       final String clazz,
                                                       final String identifier) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        Type model = this.repos.getMonitoringData(applicationScenario, typeClass, identifier);
        return (model != null) ? model.toRdfString() : "";
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#reset(de.atb.context.common.util.BusinessCase)
     */
    @Override
    public final synchronized boolean reset(final BusinessCase businessCase) {
        if (this.repos != null) {
            return this.repos.reset(businessCase);
        }
        return false;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getLastIds(de.atb.context.common.util.ApplicationScenario, java.lang.String, int)
     */
    @Override
    public final synchronized List<String> getLastIds(final ApplicationScenario applicationScenario,
                                                      final String clazz,
                                                      final int count) throws ContextFault {
        return getLastIds(applicationScenario.getBusinessCase(), clazz, count);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getLastIds(de.atb.context.common.util.ApplicationScenario, java.lang.String, de.atb.context.common.util.TimeFrame)
     */
    @Override
    public final synchronized List<String> getLastIds(final ApplicationScenario applicationScenario,
                                                      final String clazz,
                                                      final TimeFrame timeFrame) throws ContextFault {
        return getLastIds(applicationScenario.getBusinessCase(), clazz, timeFrame);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#executeSparqlSelectQuery(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public final synchronized String executeSparqlSelectQuery(final BusinessCase businessCase,
                                                              final String query) throws ContextFault {
        ResultSet results = repos.executeSparqlSelectQuery(businessCase, query);
        return ResultSetFormatter.asXMLString(results);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.interfaces.IPrimitiveService#start()
     */
    @Override
    public void start() throws ContextFault {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.interfaces.IPrimitiveService#stop()
     */
    @Override
    public void stop() throws ContextFault {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.interfaces.IPrimitiveService#restart()
     */
    @Override
    public void restart() throws ContextFault {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.interfaces.IPrimitiveService#ping()
     */
    @Override
    public final String ping() throws ContextFault {
        return ServiceManager.PING_RESPONSE;
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#executeSparqlUpdateQuery(de.atb.context.common.util.BusinessCase, java.lang.String)
     */
    @Override
    public final void executeSparqlUpdateQuery(final BusinessCase businessCase,
                                               final String query) throws ContextFault {
        repos.executeSparqlUpdateQueries(businessCase, query);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#executeSparqlUpdateQueries(de.atb.context.common.util.BusinessCase, java.lang.String[])
     */
    @Override
    public final void executeSparqlUpdateQueries(final BusinessCase businessCase,
                                                 final String... queries) throws ContextFault {
        repos.executeSparqlUpdateQueries(businessCase, queries);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getLastIds(de.atb.context.common.util.BusinessCase, java.lang.String, int)
     */
    @Override
    public final List<String> getLastIds(final BusinessCase businessCase,
                                         final String clazz,
                                         final int count) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        return repos.getLastIds(businessCase, typeClass, count);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.IAmIMonitoringDataRepositoryService#getLastIds(de.atb.context.common.util.BusinessCase, java.lang.String, de.atb.context.common.util.TimeFrame)
     */
    @Override
    public final List<String> getLastIds(final BusinessCase businessCase,
                                         final String clazz,
                                         final TimeFrame timeFrame) throws ContextFault {
        Class<Type> typeClass = getTypeClass(clazz);
        return repos.getLastIds(businessCase, typeClass, timeFrame);
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.interfaces.IRepositoryService#store(java.lang.Object)
     */
    @Override
    public String store(Object Element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * (non-Javadoc)
     *
     * @see de.atb.context.services.interfaces.IRepositoryService#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object Element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shutdown() {
        super.repos.shutdown();
    }

    @SuppressWarnings("unchecked")
    private Class<Type> getTypeClass(String clazz) {
        try {
            return (Class<Type>) Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ContextFault(e);
        }
    }

    private List<String> toStringModels(List<Type> models) {
        List<String> stringModels = new ArrayList<>();
        for (Type model : models) {
            stringModels.add(model.toRdfString());
        }
        return stringModels;
    }
}
