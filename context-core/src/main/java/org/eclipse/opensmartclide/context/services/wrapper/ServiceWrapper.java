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


import org.eclipse.opensmartclide.context.infrastructure.ServiceInfo;
import org.eclipse.opensmartclide.context.modules.broker.process.services.PESFlowSpecs;
import org.eclipse.opensmartclide.context.services.interfaces.IService;
import org.eclipse.opensmartclide.context.services.manager.ServiceManager;
import org.eclipse.opensmartclide.context.tools.datalayer.models.OutputDataModel;
import org.eclipse.opensmartclide.context.tools.ontology.Configuration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Giovanni
 */
public class ServiceWrapper<Service extends IService> {

	protected Service service;

	public ServiceWrapper(final Service service) {
		this.service = service;
		if (!ServiceManager.isPingable(service)) {
			throw new IllegalArgumentException("Service is not pingable!");
		}
	}

	public final void start() {
		service.start();
	}

	public final void stop() {
		service.stop();
	}

	public final void restart() {
		service.restart();
	}

	public final String ping() {
		return service.ping();
	}

	public final <T extends Configuration> boolean configureService(final T Configuration) {
		return service.configureService(Configuration);
	}

        public boolean setNotifierClientAndDataOutputId(String host, int port, String className,
                ArrayList<String> dataOutputIds, String pesId, HashMap<String,PESFlowSpecs> flowSpecs, String serviceId, OutputDataModel outModel){
             return service.setupRuntimeSpecs(host, port, className, dataOutputIds, pesId, flowSpecs, serviceId, outModel);
        };

        public boolean setNotifierClient(String host, int port, String className){
             return service.setNotifierClient(host, port, className);
        };

        public ServiceInfo getReposInfo(){
             return service.getReposInfo();
        };

	public final boolean runtimeInvoke(String flowId) {
		return service.runtimeInvoke(flowId);
	}

}
