package org.eclipse.opensmartclide.context.services.interfaces;

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


import org.eclipse.opensmartclide.context.infrastructure.ServiceInfo;

/**
 *
 * @author Giovanni
 */
public class DataInput {

    private String resultId;
    private String type;
    private ServiceInfo repositoryDetails;

    public DataInput() {
    }

    public DataInput(final String ResultId, final String type) {
        this.resultId = ResultId;
        this.type = type;
    }

    public final String getResultId() {
        return resultId;
    }

    public final void setResultId(final String bean) {
        this.resultId = bean;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ServiceInfo getRepositoryDetails() {
        return repositoryDetails;
    }

    public void setRepositoryDetails(ServiceInfo repositoryDetails) {
        this.repositoryDetails = repositoryDetails;
    }

}
