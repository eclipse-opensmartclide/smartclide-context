package de.atb.context.monitoring.models;

/*
 * #%L
 * SmartCLIDE Monitoring
 * %%
 * Copyright (C) 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.WebServiceDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.rdf.model.Model;
import org.simpleframework.xml.Root;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RdfType("GitlabCommitDataModel")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Root
@Getter
@Setter
public class GitlabCommitDataModel implements IMonitoringDataModel<GitlabCommitDataModel, WebServiceDataSource> {
    private Date monitoredAt;
    private String documentIndexId = "index/broker";
    private String documentUri;
    @Id
    private String identifier;
    private WebServiceDataSource dataSource;
    private String implementingClassName = GitlabCommitDataModel.class.getName();
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private List<GitlabCommitMessage> gitlabCommitMessages = new ArrayList<>();

    public GitlabCommitDataModel() {
        this.identifier = UUID.randomUUID().toString();
    }

    public void addGitMessage(GitlabCommitMessage gitlabCommitMessage) {
        if (!this.gitlabCommitMessages.contains(gitlabCommitMessage)) {
            this.gitlabCommitMessages.add(gitlabCommitMessage);
        }
    }

    @Override
    public GitlabCommitDataModel fromRdfModel(String rdfModel) {
        return RdfHelper.createMonitoringData(rdfModel, GitlabCommitDataModel.class);
    }

    @Override
    public GitlabCommitDataModel fromRdfModel(Model model) {
        return RdfHelper.createMonitoringData(model, GitlabCommitDataModel.class);
    }

    @Override
    public String toRdfString() {
        return ModelOutputLanguage.RDFXML.getModelAsString(this.toRdfModel());
    }

    @Override
    public Model toRdfModel() {
        return RdfHelper.createRdfModel(this);
    }

    @Override
    public String getContextIdentifierClassName() {
        return null;
    }

    @Override
    public void setDataSource(WebServiceDataSource dataSource) {
    }

    @Override
    public BusinessCase getBusinessCase() {
        return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
    }

    @Override
    public ApplicationScenario getApplicationScenario() {
        return ApplicationScenario.getInstance(getBusinessCase());
    }

    @Override
    public boolean triggersContextChange() {
        return false;
    }

    @Override
    public void initialize() {

    }
}
