package de.atb.context.extraction.util;

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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * IOntPropertyProvider
 *
 * @param <T> the type of property this Provider provides.
 * @author scholze
 * @version $LastChangedRevision: 647 $
 */
public interface IOntPropertyProvider<T extends OntProperty> {

	/**
	 * Gets the Property from the given Ontology.
	 *
	 * @param model
	 *            The Ontology to retrieve the property from.
	 * @return the Property from the given Ontology.
	 */
	T getProperty(OntModel model);

	Class<?>[] getRanges();

	Class<?>[] getDomains();
}
