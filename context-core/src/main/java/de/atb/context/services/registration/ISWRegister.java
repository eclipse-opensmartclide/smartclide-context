package de.atb.context.services.registration;

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
 *
 * @author Guilherme
 */
interface ISWRegister {

	public String getDate();

	public String getPort();

	public String getURL();

	public String getAddress();

	public void setDate(String date);

	public void setURL(String url);

	public void setPort(String port);

	public void setAddress(String address);
}
