package eu.smartclide.contexthandling.dle.model;

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

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommitMessage {
    @Builder.Default
    final String header = "new commit";
    @Builder.Default
    final String state = "info";
    @SerializedName("repo_id")
    String repoId;
    String user;
    String branch;
    @SerializedName("time_since_last_commit")
    Integer timeSinceLastCommit;
    @SerializedName("number_of_files_modified")
    Integer numberOfFilesModified;
}
