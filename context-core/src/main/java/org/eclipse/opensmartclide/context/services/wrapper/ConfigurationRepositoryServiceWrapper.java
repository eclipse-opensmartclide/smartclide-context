package org.eclipse.opensmartclide.context.services.wrapper;

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


import org.eclipse.opensmartclide.context.common.configuration.ApplicationScenarioConfiguration;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.opensmartclide.context.services.IConfigurationRepositoryService;

/**
 * ConfigurationRepositoryWrapper
 *
 * @author scholze
 * @version $LastChangedRevision: 144 $
 *
 */
public class ConfigurationRepositoryServiceWrapper extends
        RepositoryServiceWrapper<IConfigurationRepositoryService> {

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigurationRepositoryServiceWrapper.class);

	public ConfigurationRepositoryServiceWrapper(
			final IConfigurationRepositoryService service) {
		super(service);
	}

	public final synchronized <T extends ApplicationScenarioConfiguration<?>> T getConfiguration(
			final ApplicationScenario appScenario, final Class<T> clazz)
			throws ContextFault {
		String configString = this.service.getConfiguration(appScenario,
				clazz.getName());
		T config = ApplicationScenarioConfiguration.deserialize(configString,
				clazz);
		return config;
	}

	public final synchronized void persistConfiguration(
			final ApplicationScenarioConfiguration<?> config) throws ContextFault {
		service.persistConfiguration(config.serialize(), config.getClass()
				.getName());
	}

	@SuppressWarnings("unchecked")
	public final synchronized <T extends ApplicationScenarioConfiguration<?>> T getConfiguration(
			final ApplicationScenario appScenario, final String clazzName)
			throws ContextFault {
		String configString = this.service.getConfiguration(appScenario,
				clazzName);
		Class<T> clazz;
		try {
			clazz = (Class<T>) Class.forName(clazzName);
			T config = ApplicationScenarioConfiguration.deserialize(
					configString, clazz);
			return config;
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public final synchronized boolean clearBaseDirectory() throws ContextFault {
		return service.clearBaseDirectory();
	}

	public final synchronized boolean clearBusinessCaseDirectory(
			final BusinessCase businessCase) throws ContextFault {
		return service.clearBusinessCaseDirectory(businessCase);
	}

	public final synchronized boolean deleteApplicationScenarioConfiguration(
			final ApplicationScenario scenario) throws ContextFault {
		return service.deleteApplicationScenarioConfiguration(scenario);
	}
}
