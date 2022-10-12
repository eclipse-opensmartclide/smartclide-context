package org.eclipse.opensmartclide.context.monitoring.parser.file;

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

import org.eclipse.opensmartclide.context.monitoring.config.models.DataSource;
import org.eclipse.opensmartclide.context.monitoring.config.models.InterpreterConfiguration;
import org.eclipse.opensmartclide.context.monitoring.index.Indexer;
import org.eclipse.opensmartclide.context.tools.ontology.AmIMonitoringConfiguration;

import java.io.File;

/**
 * TrfParser
 *
 * @author scholze
 * @version $LastChangedRevision: 77 $
 *
 */
public class TrfParser extends FileParser {

	public TrfParser(DataSource dataSource,
                     InterpreterConfiguration fileSetting, Indexer indexer,
                     final AmIMonitoringConfiguration amiConfiguration) {
		super(dataSource, fileSetting, indexer, amiConfiguration);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see FileParser#parseObject(java.io.File)
	 */
	@Override
	protected boolean parseObject(File file) {
		return true;
	}

}
