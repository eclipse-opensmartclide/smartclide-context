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


import java.net.URI;

import de.atb.context.common.authentication.Credentials;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.DataSourceType;
import de.atb.context.monitoring.models.IMessageBroker;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

/**
 * MessageBrokerDataSource
 *
 * @author scholze
 * @version $LastChangedRevision: 156 $
 *
 */
@RdfType("MessageBrokerDataSource")
@Namespace("http://atb-bremen.de/")
public class MessageBrokerDataSource extends DataSource {

	/**
	 *
	 */
	private static final long serialVersionUID = 3490943354053238739L;

	public MessageBrokerDataSource() {

	}

	public MessageBrokerDataSource(final DataSource base) {
		this.id = base.getId();
		this.monitor = base.getMonitor();
		this.options = base.getOptions();
		this.type = base.getType().toString();
		this.uri = base.getUri();
	}

	@Override
	public final DataSourceType getType() {
		return DataSourceType.MessageBroker;
	}

	public final String getMessageBrokerServer() {
		return this.getOptionValue(MessageBrokerDataSourceOptions.MessageBrokerServer, true);
	}

	public final Integer getMessageBrokerPort() {
	    return this.getOptionValue(MessageBrokerDataSourceOptions.MessageBrokerPort, true);
    }

	public final String getUserName() {
		return this.getOptionValue(MessageBrokerDataSourceOptions.UserName, true);
	}

	public final String getPassword() {
		return this.getOptionValue(MessageBrokerDataSourceOptions.Password, true);
	}

    public final String getExchange() {
        return this.getOptionValue(MessageBrokerDataSourceOptions.Exchange, true);
    }

    public final String getTopic() {
        return this.getOptionValue(MessageBrokerDataSourceOptions.Topic, true);
    }

    public final Credentials getCredentials() {
		String userName = this.getUserName();
		String password = this.getPassword();
		return new Credentials(userName, password);
	}

	public final IMessageBroker toMessageBroker() {
		final URI myUri = URI.create(uri);
        final Credentials myCredentials = getCredentials();
		return new IMessageBroker() {

			@Override
			public URI getURI() {
				return myUri;
			}

            @Override
            public Credentials getCredentials() {
                return myCredentials;
            }
		};
	}
}
