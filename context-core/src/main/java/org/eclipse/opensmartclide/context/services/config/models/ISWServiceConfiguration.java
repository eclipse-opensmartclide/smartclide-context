package org.eclipse.opensmartclide.context.services.config.models;

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


import java.util.List;

/**
 *
 * @author Giovanni
 */
public interface ISWServiceConfiguration {

	List<SWService> getServices();

	SWService getService(String id);
}
