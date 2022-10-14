package org.eclipse.opensmartclide.context.monitoring.models;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

@RdfType("GitlabCommitMessage")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GitlabCommitMessage {
    String user;
    String repository;
    String branch;
    Integer noOfModifiedFiles;
    // Has to be Integer because
    // MonitoringDataRepository.persist(java.lang.String, java.lang.Class<Type>)
    // cannot persist long values for some reason.
    Integer timeSinceLastCommit;
}
