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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Root;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

@RdfType("GitDataModel")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Root
@Getter
@Setter
public class GitDataModel implements IMonitoringDataModel<GitDataModel, MessageBrokerDataSource> {
    private Date monitoredAt;
    private String documentIndexId = "index/broker";
    private String documentUri;
    private String identifier;
    private MessageBrokerDataSource dataSource;
    private String implementingClassName = GitDataModel.class.getName();
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private List<GitMessage> gitMessages = new ArrayList<>();

    public GitDataModel() {
        this.identifier = UUID.randomUUID().toString();
    }

    @Override
    public GitDataModel fromRdfModel(String rdfModel) {
        return RdfHelper.createMonitoringData(rdfModel, GitDataModel.class);
    }

    public void addGitMessage(GitMessage gitMessage) {
        if (!this.gitMessages.contains(gitMessage)) {
            this.gitMessages.add(gitMessage);
        }
    }

    @Override
    public GitDataModel fromRdfModel(Model model) {
        return RdfHelper.createMonitoringData(model, GitDataModel.class);
    }

    @Override
    public String toRdfString() {
        return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
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
    public void setDataSource(MessageBrokerDataSource dataSource) {
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
