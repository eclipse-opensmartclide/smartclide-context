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

import java.util.Date;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Root;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

@RdfType("CustomFileBasedDataModel")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Root
@Getter
@Setter
public class CustomFileBasedDataModel implements IMonitoringDataModel<CustomFileBasedDataModel, FileSystemDataSource> {

    private Date monitoredAt;
    private String documentIndexId = "index/file";
    private String documentUri;
    @Id
    private String identifier;
    private FileSystemDataSource dataSource;
    private String implementingClassName = CustomFileBasedDataModel.class.getName();
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private String source;
    private String message;
    private String userInfo;

    public CustomFileBasedDataModel() {
        this.identifier = UUID.randomUUID().toString();
    }

    @Override
    public String getContextIdentifierClassName() {
        return null;
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

    @Override
    public CustomFileBasedDataModel fromRdfModel(String rdfModel) {
        return RdfHelper.createMonitoringData(rdfModel, CustomFileBasedDataModel.class);
    }

    @Override
    public CustomFileBasedDataModel fromRdfModel(Model model) {
        return RdfHelper.createMonitoringData(model, CustomFileBasedDataModel.class);
    }

    @Override
    public String toRdfString() {
        return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
    }

    @Override
    public Model toRdfModel() {
        return RdfHelper.createRdfModel(this);
    }
}
