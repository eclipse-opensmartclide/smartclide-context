/*
 * @(#)IWebService.java
 *
 * $Id: IWebService.java 665 2016-11-03 23:44:11Z scholze $
 * 
 * $Rev:: 143                  $ 	last change revision
 * $Date:: 2015-09-11 15:09:26#$	last change date
 * $Author:: scholze             $	last change author
 * 
 * Copyright 2011-15 Sebastian Scholze (ATB). All rights reserved.
 *
 */
package de.atb.context.monitoring.models;

/*
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2021 ATB
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import de.atb.context.common.authentication.Credentials;

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
