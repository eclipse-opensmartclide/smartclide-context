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
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.util.Date;
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
public class DummyMonitoringDataModel implements IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> {

	private static final long serialVersionUID = -8744217754389596169L;

	protected String documentIndexId = "index/dummy";
	protected String documentUri = "/var/tmp/dummy.doc";
	protected String implementingClassName = DummyMonitoringDataModel.class.getName();

	protected Date monitoredAt = new Date();
	protected String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
	protected FileSystemDataSource dataSource;

	protected String dummyName = "myDummyName";
	protected String dummyValue = "myDummyVaLuE!";
	protected String identifier;

	public final String getDummyName() {
		return this.dummyName;
	}

	public final void setDummyName(final String dummyName) {
		this.dummyName = dummyName;
	}

	public final String getDummyValue() {
		return this.dummyValue;
	}

	public final void setDummyValue(final String dummyValue) {
		this.dummyValue = dummyValue;
	}

	@Override
	public final FileSystemDataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public final void setDataSource(final FileSystemDataSource ds) {
		this.dataSource = ds;
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
	 * @see
	 * IMonitoringDataModel#getDocumentIndexId
	 * ()
	 */
	@Override
	public final String getDocumentIndexId() {
		return this.documentIndexId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringDataModel#getDocumentUri
	 * ()
	 */
	@Override
	public final String getDocumentUri() {
		return this.documentUri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * getImplementingClass()
	 */
	@Override
	public final String getImplementingClassName() {
		return this.getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringDataModel#getMonitoredAt
	 * ()
	 */
	@Override
	public final Date getMonitoredAt() {
		if (this.monitoredAt != null) {
			return (Date) this.monitoredAt.clone();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * getMonitoringDataVersion()
	 */
	@Override
	public final String getMonitoringDataVersion() {
		return this.monitoringDataVersion;
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

	public final void setDocumentIndexId(final String documentIndexId) {
		this.documentIndexId = documentIndexId;
	}

	public final void setDocumentUri(final String documentUri) {
		this.documentUri = documentUri;
	}

	public final void setImplementingClass(final String clazz) {
		this.implementingClassName = clazz;
	}

	public final void setMonitoredAt(final Date monitoredAt) {
		if (monitoredAt != null) {
			this.monitoredAt = (Date) monitoredAt.clone();
		} else {
			this.monitoredAt = null;
		}
	}

	public final void setMonitoringDataVersion(final String version) {
		this.monitoringDataVersion = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * IMonitoringDataModel#getIdentifier
	 * ()
	 */
	@Override
	@Id
	public final String getIdentifier() {
		if (this.identifier == null) {
			this.identifier = UUID.randomUUID().toString();
		}
		return this.identifier;
	}

	@Override
	public final void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	public final String generateIdentifier() {
		return UUID.randomUUID().toString();
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
