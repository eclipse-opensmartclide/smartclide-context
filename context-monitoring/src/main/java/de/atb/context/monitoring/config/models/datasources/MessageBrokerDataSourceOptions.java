package de.atb.context.monitoring.config.models.datasources;

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


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * WebServiceDataSourceOptions
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 *
 */
public enum MessageBrokerDataSourceOptions implements IDataSourceOptionValue {

	MessageBrokerServer("server", String.class),

    MessageBrokerPort("port", Integer.class),

	UserName("username", String.class),

	Password("password", String.class),

    Exchange("exchange", String.class),

    Topic("topic", String.class);

	private final String key;
	private final Class<? extends Serializable> valueType;

	MessageBrokerDataSourceOptions(final String optionKey, final Class<? extends Serializable> valueType) {
		this.key = optionKey;
		this.valueType = valueType;
	}

	@Override
	public String getKeyName() {
		return this.key;
	}

	@Override
	public Class<? extends Serializable> getValueType() {
		return this.valueType;
	}
}
