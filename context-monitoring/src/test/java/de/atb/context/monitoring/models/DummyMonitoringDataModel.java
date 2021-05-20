/*
 * @(#)DummyMonitoringDataModel.java
 *
 * $Id: DummyMonitoringDataModel.java 686 2016-12-02 15:53:40Z scholze $
 * 
 * $Rev:: 692                  $ 	last change revision
 * $Date:: 2012-07-09 09:58:45#$	last change date
 * $Author:: scholze             $	last change author
 * 
 * Copyright 2011-15 Sebastian Scholze (ATB). All rights reserved.
 *
 */
package de.atb.context.monitoring.models;

/*
 * #%L
 * ProSEco AmI Monitoring Core Services
 * %%
 * Copyright (C) 2015 ATB
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.hp.hpl.jena.rdf.model.Model;
import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DummyMonitoringDataModel
 * 
 * @author scholze
 * @version $LastChangedRevision: 692 $
 * 
 */
@RdfType("DummyMonitoringDataModel")
@Namespace("http://www.atb-bremen.de/")
@Getter
@Setter
public class DummyMonitoringDataModel implements IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> {

	private static final long serialVersionUID = -8744217754389596169L;

    private String documentIndexId = "index/dummy";
    private String documentUri = "/var/tmp/dummy.doc";
    private String implementingClassName = DummyMonitoringDataModel.class.getName();

    private LocalDateTime monitoredAt = LocalDateTime.now();
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private FileSystemDataSource dataSource;

    private String dummyName = "myDummyName";
    private String dummyValue = "myDummyVaLuE!";
    private UUID identifier;

    public DummyMonitoringDataModel() {
        this.identifier = UUID.randomUUID();
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringData#fromRdfModel(java.lang
	 * .String)
	 */
	@Override
	public final DummyMonitoringDataModel fromRdfModel(final String rdfModel) {
		return RdfHelper.createMonitoringData(rdfModel, DummyMonitoringDataModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringData#fromRdfModel(com.hp.hpl
	 * .jena.rdf.model.Model)
	 */
	@Override
	public final DummyMonitoringDataModel fromRdfModel(final Model model) {
		return RdfHelper.createMonitoringData(model, DummyMonitoringDataModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringData#toRdfString()
	 */
	@Override
	public final String toRdfString() {
		return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringData#toRdfModel()
	 */
	@Override
	public final Model toRdfModel() {
		return RdfHelper.createRdfModel(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringDataModel#getBusinessCase
	 * ()
	 */
	@Override
	public final BusinessCase getBusinessCase() {
		return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * triggersContextChange()
	 */
	@Override
	public final boolean triggersContextChange() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * getApplicationScenario()
	 */
	@Override
	public final ApplicationScenario getApplicationScenario() {
		return ApplicationScenario.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * getContextIdentifierClassName()
	 */
	@Override
	public final String getContextIdentifierClassName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringDataModel#initialize()
	 */
	@Override
	public void initialize() {
	}
}
