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


import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.SPARQLHelper;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.services.AmIMonitoringDataRepositoryService;
import org.eclipse.opensmartclide.context.services.wrapper.AmIMonitoringDataRepositoryServiceWrapper;

/**
 * MonitoringDataRepositoryHelper
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 239 $
 */
public abstract class MonitoringDataRepositoryHelper<Type extends IMonitoringDataModel<Type, ?>> {

    protected IMonitoringDataRepository<Type> repos;
    protected BusinessCase businessCase;

    protected MonitoringDataRepositoryHelper(final MonitoringDataRepository<Type> repository, final BusinessCase bc) {
        this.repos = repository;
        this.businessCase = bc;
    }

    public MonitoringDataRepositoryHelper(final AmIMonitoringDataRepositoryService<Type> service, final BusinessCase bc) {
        this.repos = new AmIMonitoringDataRepositoryServiceWrapper<>(service);
        this.businessCase = bc;
    }

    public MonitoringDataRepositoryHelper(final AmIMonitoringDataRepositoryServiceWrapper<Type> wrapper, final BusinessCase bc) {
        this.repos = wrapper;
        this.businessCase = bc;
    }

    public final String getNewestMonitoringId(final Class<Type> clazz) {
        final String monitoringDataClass = SPARQLHelper.getRdfClassQualifier(clazz);
        final String identifierProperty = SPARQLHelper.getRdfPropertyQualifier(clazz, "identifier");
        final String monitoredAtProperty = SPARQLHelper.getRdfPropertyQualifier(clazz, "monitoredAt");

        final String selectQuery = String.format("SELECT ?monitoringId WHERE { ?c rdf:type %s. ?c %s ?monitoringId . ?c %s ?mon } "
            + "ORDER BY DESC (?mon) LIMIT 1", monitoringDataClass, identifierProperty, monitoredAtProperty);
        ResultSet rs = executeSparqlSelectQuery(selectQuery);
        if (rs.hasNext()) {
            QuerySolution qs = rs.next();
            String monitoringId = (qs != null) && (qs.getLiteral("monitoringId") != null) ? qs.getLiteral("monitoringId").getString()
                : null;
            return monitoringId;
        }
        return null;
    }

    public abstract String getNewestMonitoringId();

    /**
     * Prepares (appending prefixes) and executes the given SparQL SELECT query
     * and retuns the appropriate ResultSet (if any).
     *
     * @param query the SparQL SELECT query to execute. Note this query will be
     *              prepared before execution.
     * @return the ResultSet as a result of the exeuction of the given SparQL
     * SELECT query.
     */
    public final ResultSet executeSparqlSelectQuery(final String query) {
        return repos.executeSparqlSelectQuery(businessCase, query);
    }

    public final void executeSparqlUpdateQuery(final String query) {
        repos.executeSparqlUpdateQuery(businessCase, query);
    }

    public final void executeSparqlUpdateQueries(final String... queries) {
        repos.executeSparqlUpdateQueries(businessCase, queries);
    }

}
