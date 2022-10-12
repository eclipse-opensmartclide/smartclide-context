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


import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.extraction.ContextContainer;
import org.eclipse.opensmartclide.context.persistence.common.IPersistenceUnit;

import java.util.List;

/**
 * IContextRepository
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
public interface IContextRepository extends IPersistenceUnit<ContextContainer> {

	Model getDefaultModel(BusinessCase businessCase);

	ContextContainer getContext(ApplicationScenario applicationScenario, String contextId);

	ContextContainer getContext(BusinessCase businessCase, String contextId);

	ContextContainer getRawContext(BusinessCase businessCase, String contextId);

	ContextContainer getRawContext(ApplicationScenario applicationScenario, String contextId);

	Boolean executeSparqlAskQuery(BusinessCase businessCase, String query);

	ResultSet executeSparqlSelectQuery(BusinessCase businessCase, String query);

	Model executeSparqlDescribeQuery(BusinessCase businessCase, String query);

	Model executeSparqlConstructQuery(BusinessCase businessCase, String query);

	Boolean executeSparqlAskQuery(BusinessCase businessCase, String query, boolean useReasoner);

	ResultSet executeSparqlSelectQuery(BusinessCase businessCase, String query, boolean useReasoner);

	Model executeSparqlDescribeQuery(BusinessCase businessCase, String query, boolean useReasoner);

	Model executeSparqlConstructQuery(BusinessCase businessCase, String query, boolean useReasoner);

	List<String> getLastContextsIds(BusinessCase businessCase, int count);

	List<String> getLastContextsIds(BusinessCase businessCase, TimeFrame timeFrame);

	List<String> getLastContextsIds(ApplicationScenario applicationScenario, int count);

	List<String> getLastContextsIds(ApplicationScenario applicationScenario, TimeFrame timeFrame);

	void initializeRepository(BusinessCase businessCase, String modelUri);

}
