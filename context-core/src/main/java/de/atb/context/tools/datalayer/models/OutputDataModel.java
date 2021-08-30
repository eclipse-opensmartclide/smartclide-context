package de.atb.context.tools.datalayer.models;

/*
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


import de.atb.context.tools.datalayer.models.structure.RootModelField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Guilherme
 */
@Getter
@Setter
public class OutputDataModel {

    String associatedPesId;
    String serviceType;
    String repositoryType;
    String associatedConfigId;
    RootModelField root;

    public OutputDataModel(String pesId, String serviceType, String repositoryType, String id) {
        this.associatedPesId = pesId;
        this.serviceType = serviceType;
        this.repositoryType = repositoryType;
        this.associatedConfigId = id;
    }

    public OutputDataModel() {
    }

    public String getImplementingClass() {
        if (this.getRoot() == null) {
            return null;
        }
        return this.getRoot().getImplementingClass();
    }
}
