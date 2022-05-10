package de.atb.context.monitoring.models;

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

public enum GitMessageHeader {
    NEW_COMMIT("new commit"),

    NEW_FILE_CHANGED("new file changed");

    private final String header;

    GitMessageHeader(final String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
