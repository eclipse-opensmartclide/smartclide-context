package org.eclipse.opensmartclide.context.monitoring.config.models.datasources;

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


import java.io.Serializable;

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

    IncomingExchange("incoming-exchange", String.class),

    IncomingTopic("incoming-topic", String.class),

    IncomingDurable("incoming-durable", Boolean.class),

    OutgoingExchange("outgoing-exchange", String.class),

	OutgoingTopic("outgoing-topic", String.class),

    OutgoingQueue("outgoing-queue", String.class),

    OutgoingDurable("outgoing-durable", Boolean.class);

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