package de.atb.context.modules.broker.status.ontology;

/*-
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
 * @author Giovanni
 */
public enum StatusVocabulary {
	RUNNING("Running"),

	STOPPED("Stopped"),

	FINISHED("Finished"),

	FREE("Free"),

	BUSY("Busy");

	private final String statusName;

	StatusVocabulary(final String statusName) {
		this.statusName = statusName;
	}

	public String getStatusName() {
		return statusName;
	}

}
