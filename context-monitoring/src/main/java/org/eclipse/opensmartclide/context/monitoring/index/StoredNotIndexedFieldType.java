package org.eclipse.opensmartclide.context.monitoring.index;

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

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;

public class StoredNotIndexedFieldType extends FieldType {

    public StoredNotIndexedFieldType() {
        super(StringField.TYPE_STORED);
        this.setIndexOptions(IndexOptions.NONE);
    }
}
