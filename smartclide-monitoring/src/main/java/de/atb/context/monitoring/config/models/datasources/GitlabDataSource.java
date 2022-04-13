package de.atb.context.monitoring.config.models.datasources;

/*-
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2015 - 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import de.atb.context.monitoring.config.models.DataSource;
import lombok.NoArgsConstructor;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.io.Serializable;

/**
 * GitLabDataSource
 *
 * @author scholze
 * @version $LastChangedRevision: 156 $
 */
@RdfType("GitlabDataSource")
@Namespace("http://atb-bremen.de/")
@NoArgsConstructor
public class GitlabDataSource extends WebServiceDataSource {

    private static final IDataSourceOptionValue ACCESS_TOKEN_OPTION = new IDataSourceOptionValue() {
        @Override
        public String getKeyName() {
            return "token";
        }

        @Override
        public Class<? extends Serializable> getValueType() {
            return String.class;
        }
    };

    public GitlabDataSource(final DataSource base) {
        this.id = base.getId();
        this.monitor = base.getMonitor();
        this.options = base.getOptions();
        this.type = base.getType().toString();
        this.uri = base.getUri();
    }

    public final String getGitLabAccessToken() {
        return this.getOptionValue(ACCESS_TOKEN_OPTION, true);
    }

    public final String getMessageBrokerServer() {
        return this.getOptionValue(MessageBrokerDataSourceOptions.MessageBrokerServer, true);
    }

    public final Integer getMessageBrokerPort() {
        return this.getOptionValue(MessageBrokerDataSourceOptions.MessageBrokerPort, true);
    }

    public final String getExchange() {
        return this.getOptionValue(MessageBrokerDataSourceOptions.Exchange, true);
    }

    public final String getOutTopic() {
        return this.getOptionValue(MessageBrokerDataSourceOptions.OutTopic, true);
    }
}
