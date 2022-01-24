package de.atb.context.extraction;

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

import org.apache.jena.ontology.OntModel;

/**
 * IContextReasoner
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
public interface IContextReasoner<Context extends OntModel> {

	Context performCompleteReasoning(Context contextModel);

	Context performRuleBasedReasoning(Context contextModel);

	Context performRuleStatisticalReasoning(Context contextModel);
}
