package org.eclipse.opensmartclide.context.monitoring.models;

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

import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.monitoring.IMonitoringData;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;

import java.util.Date;

/**
 * IMonitoringDataModel
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public interface IMonitoringDataModel<T, D extends DataSource> extends IMonitoringData<T> {

    String getIdentifier();

    void setIdentifier(String identifier);

    String getMonitoringDataVersion();

    Date getMonitoredAt();

    String getDocumentIndexId();

    String getDocumentUri();

    String getImplementingClassName();

    String getContextIdentifierClassName();

    D getDataSource();

    void setDataSource(D dataSource);

    BusinessCase getBusinessCase();

    @Override
    ApplicationScenario getApplicationScenario();

    boolean triggersContextChange();

    void initialize();


}
