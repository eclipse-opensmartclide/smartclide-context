package org.eclipse.opensmartclide.context.modules.broker.status.ontology;

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


import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni
 */
public class Alerts {
    private List<Alert> lAlert = new ArrayList<>();

    public Alerts() {
    }

    public Alerts(final List<Alert> lAlert) {
        this.lAlert = lAlert;
    }

    public final List<Alert> getLAlert() {
        return lAlert;
    }

    public final void setLAlert(final List<Alert> lAlert) {
        this.lAlert = lAlert;
    }

    public void addAlert(Alert alert) {
        this.lAlert.add(alert);
    }

    public Alert removeAlert(Alert alert) {
        return this.lAlert.remove(this.lAlert.indexOf(alert));
    }

}
