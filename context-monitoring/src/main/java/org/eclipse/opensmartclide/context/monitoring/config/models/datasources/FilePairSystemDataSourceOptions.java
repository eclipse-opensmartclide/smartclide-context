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
 * FilePairSystemDataSourceOptions
 *
 * @author scholze
 * @version $LastChangedRevision: 143 $
 */
public enum FilePairSystemDataSourceOptions implements IDataSourceOptionValue {

    ExtensionOne("extensionOne", String.class),

    ExtensionTwo("extensionTwo", String.class),

    MachineId("machineId", String.class),

    ;

    private final String key;
    private final Class<? extends Serializable> valueType;

    FilePairSystemDataSourceOptions(final String optionKey, final Class<? extends Serializable> valueType) {
        this.key = optionKey;
        this.valueType = valueType;
    }

    /**
     * (non-Javadoc)
     *
     * @see IDataSourceOptionValue#getKeyName()
     */
    @Override
    public String getKeyName() {
        return this.key;
    }

    /**
     * (non-Javadoc)
     *
     * @see IDataSourceOptionValue#getValueType()
     */
    @Override
    public Class<? extends Serializable> getValueType() {
        return this.valueType;
    }
}
