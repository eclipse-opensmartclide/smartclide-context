package org.eclipse.opensmartclide.context.monitoring;

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


import java.io.Serializable;

import org.apache.jena.rdf.model.Model;

import org.eclipse.opensmartclide.context.common.util.IApplicationScenarioProvider;

/**
 * IMonitoringData
 * $Id
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public interface IMonitoringData<T> extends Serializable, IApplicationScenarioProvider {

    T fromRdfModel(String rdfModel);

    T fromRdfModel(Model model);

    String toRdfString();

    Model toRdfModel();

}
