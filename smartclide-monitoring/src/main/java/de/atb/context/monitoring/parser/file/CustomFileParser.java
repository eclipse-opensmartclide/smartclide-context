package de.atb.context.monitoring.parser.file;

/*
 * #%L
 * SmartCLIDE Monitoring
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

import java.io.File;

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;

public class CustomFileParser extends FileParser {
    public CustomFileParser(DataSource dataSource,
                            InterpreterConfiguration interpreterConfiguration,
                            Indexer indexer,
                            AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, amiConfiguration);
    }

    @Override
    protected boolean parseObject(File file) {
        return true;
    }
}
