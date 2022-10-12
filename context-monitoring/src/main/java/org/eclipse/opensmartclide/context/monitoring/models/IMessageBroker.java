package org.eclipse.opensmartclide.context.monitoring.models;

/*
 * #%L
 * ATB Context Monitoring Core Services
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


import org.eclipse.opensmartclide.context.common.authentication.Credentials;

import java.net.URI;

/**
 * IWebService
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 *
 */
public interface IMessageBroker {

	URI getURI();

	Credentials getCredentials();

}
