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


import org.eclipse.opensmartclide.context.common.authentication.Credentials;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.DataSourceType;
import org.eclipse.opensmartclide.context.monitoring.models.IDatabase;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.net.URI;

/**
 * DatabaseDataSource
 *
 * @author scholze
 * @version $LastChangedRevision: 156 $
 */
@RdfType("DatabaseDataSource")
@Namespace("http://atb-bremen.de/")
public class DatabaseDataSource extends DataSource {

    /**
     *
     */
    private static final long serialVersionUID = 3490943354053238749L;

    public DatabaseDataSource() {

    }

    public DatabaseDataSource(final DataSource base) {
        this.id = base.getId();
        this.monitor = base.getMonitor();
        this.options = base.getOptions();
        this.type = base.getType().toString();
        this.uri = base.getUri();
    }

    @Override
    public final DataSourceType getType() {
        return DataSourceType.Database;
    }

    public final Long getInterval() {
        return this.getOptionValue(DatabaseDataSourceOptions.PollingInterval, true);
    }

    public final String getUserName() {
        return (String) this.getOptionValue(DatabaseDataSourceOptions.UserName, true);
    }

    public final String getPassword() {
        return (String) this.getOptionValue(DatabaseDataSourceOptions.Password, true);
    }

    public final String getMachineId() {
        return this.getOptionValue(DatabaseDataSourceOptions.MachineId, true);
    }

    public final String getDatabaseDriver() {
        return this.getOptionValue(DatabaseDataSourceOptions.DatabaseDriver, true);
    }

    public final String getDatabaseUri() {
        return this.getOptionValue(DatabaseDataSourceOptions.DatabaseUri, true);
    }

    public final String getDatabaseSelect() {
        return this.getOptionValue(DatabaseDataSourceOptions.DatabaseSelect, true);
    }

    public final Credentials getCredentials() {
        String userName = this.getUserName();
        String password = this.getPassword();
        return new Credentials(userName, password);
    }

    public final Long getStartDelay() {
        return this.getOptionValue(DatabaseDataSourceOptions.StartDelay, true);
    }

    public final IDatabase toDatabase() {
        final URI myUri = URI.create(uri);
        final Credentials myCredentials = getCredentials();
        return new IDatabase() {

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
