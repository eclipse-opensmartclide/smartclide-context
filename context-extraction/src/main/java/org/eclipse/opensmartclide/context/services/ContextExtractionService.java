package org.eclipse.opensmartclide.context.services;

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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.TimeFrame;
import org.eclipse.opensmartclide.context.extraction.ContextContainer;
import org.eclipse.opensmartclide.context.extraction.ContextContainerWrapper;
import org.eclipse.opensmartclide.context.extraction.IContextIdentifier;
import org.eclipse.opensmartclide.context.infrastructure.ServiceInfo;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.models.IMonitoringDataModel;
import org.eclipse.opensmartclide.context.monitoring.rdf.RdfHelper;
import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.eclipse.opensmartclide.context.services.interfaces.DataOutput;
import org.eclipse.opensmartclide.context.services.interfaces.IPrimitiveService;
import org.eclipse.opensmartclide.context.services.interfaces.IService;
import org.eclipse.opensmartclide.context.services.interfaces.Output;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.services.wrapper.ContextRepositoryServiceWrapper;
import org.eclipse.opensmartclide.context.tools.ontology.Configuration;
import org.eclipse.opensmartclide.context.tools.ontology.ContextExtractionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextExtractionService
 * <p>
 * In order to retrieve a registered WebService, please consider using the
 * {@link ServiceManager} by providing the implemented interface
 * {@link IContextExtractionService} like :
 *
 * <pre>
 * ServiceManager.getWebservice(IContextExtractionService.class);
 * </pre>
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
public class ContextExtractionService extends DeployableService implements IContextExtractionService {
    //protected ServiceInfo repositoryConfigData = new ServiceInfo();
	private static final Logger logger = LoggerFactory.getLogger(ContextExtractionService.class);
	protected IContextRepositoryService service;
	protected ContextRepositoryServiceWrapper repos;
	protected ContextExtractionConfiguration config;

	public ContextExtractionService() {
		initializeServices();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IContextExtractionService#extractContext(String, String, ApplicationScenario)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final synchronized <T, D extends DataSource> ContextContainerWrapper extractContext(String rdfString, String clazz,
                                                                                               ApplicationScenario applicationScenario) throws ContextFault {
		Class<? extends IMonitoringDataModel<T, D>> clazzP;
		try {
			clazzP = (Class<? extends IMonitoringDataModel<T, D>>) Class.forName(clazz);
			IMonitoringDataModel<T, D> monitoringData = (IMonitoringDataModel<T, D>) RdfHelper.createMonitoringData(rdfString, clazzP);
			if (monitoringData != null) {
				String className = monitoringData.getContextIdentifierClassName();
				if (className != null) {
					try {
						Class<? extends IContextIdentifier<ContextContainer, IMonitoringDataModel<?, ?>>> identifierClazz = (Class<? extends IContextIdentifier<ContextContainer, IMonitoringDataModel<?, ?>>>) Class
								.forName(className);
						IContextIdentifier<ContextContainer, IMonitoringDataModel<?, ?>> identifier = identifierClazz.newInstance();
						ContextContainer container = new ContextContainer(monitoringData.getApplicationScenario(), false);
						container.addDefaultModel(monitoringData.getBusinessCase());
						container = identifier.identifyContext(container, monitoringData);
						return container.toContextContainerWrapper();
					} catch (Exception e) {
						ContextExtractionService.logger.error(e.getMessage(), e);
						throw new ContextFault(e.getMessage(), e);
					}
				} else {
					ContextExtractionService.logger.warn("MonitoringData of type " + monitoringData.getImplementingClassName()
							+ " provides no context identifier class name!");
					throw new ContextFault("MonitoringData of type " + monitoringData.getImplementingClassName()
							+ " provides no context identifier class name!");
				}
			}

		} catch (ClassNotFoundException e) {
			ContextExtractionService.logger.error(e.getMessage(), e);
			throw new ContextFault(e.getMessage(), e);
		}
		ContextExtractionService.logger.error("MonitoringData to be used for Context extraction is null!");
		throw new ContextFault("MonitoringData to be used for Context extraction is null!");
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IPrimitiveService#start()
	 */
	@Override
	public final void start() throws ContextFault {
		ContextExtractionService.logger.info(String.format("Starting %s ...", getClass().getSimpleName()));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IPrimitiveService#stop()
	 */
	@Override
	public final void stop() throws ContextFault {
		ContextExtractionService.logger.info(String.format("Stopping %s ...", getClass().getSimpleName()));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IPrimitiveService#restart()
	 */
	@Override
	public final void restart() throws ContextFault {
		ContextExtractionService.logger.info(String.format("Restarting %s ...", getClass().getSimpleName()));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IPrimitiveService#ping()
	 */
	@Override
	public final String ping() throws ContextFault {
		ContextExtractionService.logger.info(String.format("%s was pinged", getClass().getSimpleName()));
		return ServiceManager.PING_RESPONSE;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IContextExtractionService#informAboutAdaptation(ApplicationScenario, java.lang.String)
	 */
	@Override
	public final synchronized void informAboutAdaptation(ApplicationScenario applicationScenario, String identifier) {
		ContextExtractionService.logger.info(String.format("Adaptation occured for ApplicationScenario %s with id %s", applicationScenario, identifier));
		// AdaptationRepositoryService service =
		// ServiceManager.getWebservice(Service.AdaptationRepositoryService);
		// if (ServiceManager.isPingable(service)) {
		// // TODO implement
		// }
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IContextExtractionService#getLastContextsIds(ApplicationScenario, int)
	 */
	@Override
	public final synchronized List<String> getLastContextsIds(ApplicationScenario applicationScenario, int count) {
		List<String> ids = new ArrayList<>();
		initializeServices();
		if (repos != null) {
			try {
				ids = repos.getLastContextsIds(applicationScenario, count);
			} catch (ContextFault e) {
				ContextExtractionService.logger.error(e.getMessage(), e);
			}
		}
		return ids;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IContextExtractionService#getLastContextsIds(ApplicationScenario, TimeFrame)
	 */
	@Override
	public final synchronized List<String> getLastContextsIds(ApplicationScenario applicationScenario, TimeFrame timeFrame) {
		List<String> ids = new ArrayList<>();
		initializeServices();
		if (repos != null) {
			try {
				ids = repos.getLastContextsIds(applicationScenario, timeFrame);
			} catch (ContextFault e) {
				ContextExtractionService.logger.error(e.getMessage(), e);
			}
		}
		return ids;
	}

	protected final synchronized void initializeServices() {
        try {
            SWServiceContainer reposContainer = null;
            if (service == null || !ServiceManager.isPingable(service)) {
                for (SWServiceContainer container : ServiceManager.getLSWServiceContainer()) {
                    if (container.getServerClass().toString().contains("ContextRepository")) {
						service = ServiceManager.getWebservice(container);
                        reposContainer = container;
                    }
                }
            }
            if (service != null && ServiceManager.isPingable(service) && reposContainer != null) {
				repos = new ContextRepositoryServiceWrapper(service);
				repositoryConfigData.setName(reposContainer.getName());
				repositoryConfigData.setHost(reposContainer.getHost());
				repositoryConfigData.setLocation(reposContainer.getLocation().toString());
				repositoryConfigData.setProxy(reposContainer.getProxyClass().getName());
				repositoryConfigData.setServer(reposContainer.getServerClass().getName());
				repositoryConfigData.setId(reposContainer.getId());
            }
        } catch (Exception e) {
			ContextExtractionService.logger.error(e.getMessage(), e);
        }
	}

    @Override
	public final ServiceInfo getReposData() {
        return repositoryConfigData;
    }

    @Override
	public final boolean configureService(Configuration configuration) {
		if (configuration != null) {
			config = (ContextExtractionConfiguration) configuration;
		} else {
			return false;
		}
    	return true;
	}

	@Override
	public boolean setNotifierClient(String host, int port, String className) throws ContextFault {
		return false;
	}

    /**
     * (non-Javadoc)
     *
     * @see IService#runtimeInvoke(java.lang.String)
     */
	@Override
	public final boolean runtimeInvoke(String input) throws ContextFault {
		// TODO what to do with "Input"
		Output output = new Output();
		DataOutput dataOutput = new DataOutput();
		dataOutput.setReposData(repositoryConfigData);
		dataOutput.setResultId("use serviceInfo to access the results from ContextRepository");
		output.setDataOutput(dataOutput);
		return true;
	}


}
