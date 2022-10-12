package org.eclipse.opensmartclide.context.services.interfaces;

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


import org.eclipse.opensmartclide.context.services.faults.ContextFault;
import org.eclipse.opensmartclide.context.infrastructure.ServiceInfo;
import org.eclipse.opensmartclide.context.modules.broker.process.services.PESFlowSpecs;
import org.eclipse.opensmartclide.context.tools.datalayer.models.OutputDataModel;
import org.eclipse.opensmartclide.context.tools.ontology.Configuration;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Giovanni
 */
@WebService(name = "ContextService", targetNamespace = "http://atb-bremen.de/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface IService extends IPrimitiveService {

    @WebMethod(operationName = "configureService")
    public <T extends Configuration> boolean configureService(@WebParam(name = "Configuration") T configuration) throws ContextFault;

    @WebMethod(operationName = "getReposInfo")
    public ServiceInfo getReposInfo() throws ContextFault;

    @WebMethod(operationName = "setupRuntimeSpecs")
    public boolean setupRuntimeSpecs(@WebParam(name = "host") String host, @WebParam(name = "port") int port, @WebParam(name = "classname") String className,
                                     @WebParam(name = "dataOutputIds") ArrayList<String> dataOutputIds, @WebParam(name = "pesId") String pesId,
                                     @WebParam(name = "flowSpecs") HashMap<String, PESFlowSpecs> flowSpecs, @WebParam(name = "serviceId") String serviceId, @WebParam(name = "outModel") OutputDataModel outModel) throws ContextFault;

    @WebMethod(operationName = "setNotifierClient")
    public boolean setNotifierClient(@WebParam(name = "host") String host,
                                     @WebParam(name = "port") int port, @WebParam(name = "classname") String className) throws ContextFault;
    @WebMethod(operationName = "runtimeInvoke")
    public boolean runtimeInvoke(@WebParam(name = "flowId") String flowId) throws ContextFault;

}
