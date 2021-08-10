package de.atb.context.persistence.processors;

/*
 * #%L
 * ATB Context Extraction Core Lib
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

/**
 * IPersistanceListener
 *
 * @author scholze
 * @version $LastChangedRevision: 417 $
 * @param <T>
 *            T
 *
 */
public interface IPersistenceProcessor<T> {

	String getId();

	void setId(String id);

	T process(T object);
}
