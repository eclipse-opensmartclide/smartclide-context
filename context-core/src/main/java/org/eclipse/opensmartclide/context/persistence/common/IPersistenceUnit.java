package org.eclipse.opensmartclide.context.persistence.common;

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

import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.IApplicationScenarioProvider;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistencePostProcessor;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistencePreProcessor;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;

/**
 * IPersistenceUnit
 *
 * @param <T> Param
 * @author scholze
 * @version $LastChangedRevision: 417 $
 */
public interface IPersistenceUnit<T extends IApplicationScenarioProvider> {

	void persist(T object) throws ContextFault;

	boolean addPersistencePreProcessor(ApplicationScenario scenario,
                                       IPersistencePreProcessor<T> preProcessor) throws ContextFault;

	boolean addPersistencePostProcessor(ApplicationScenario scenario,
                                        IPersistencePostProcessor<T> postProcessor) throws ContextFault;

	boolean removePersistencePreProcessor(ApplicationScenario scenario,
                                          IPersistencePreProcessor<T> preProcessor) throws ContextFault;

	boolean removePersistencePostProcessor(ApplicationScenario scenario,
                                           IPersistencePostProcessor<T> postProcessor) throws ContextFault;

	boolean removePersistencePreProcessor(ApplicationScenario scenario,
                                          String id) throws ContextFault;

	boolean removePersistencePostProcessor(ApplicationScenario scenario,
                                           String id) throws ContextFault;

	void triggerPreProcessors(ApplicationScenario scenario, T object)
			throws ContextFault;

	void triggerPostProcessors(ApplicationScenario scenario, T object)
			throws ContextFault;

}
