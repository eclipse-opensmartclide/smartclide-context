package de.atb.context.monitoring.models;

/*-
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2015 - 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import de.atb.context.common.util.BusinessCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

@RdfType("GitMessage")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GitMessage {
    String timestamp;
    String user;
    String repository;
    String branch;
    Integer noOfCommitsInBranch;
    Integer noOfPushesInBranch;
    Integer noOfModifiedFiles;
}
