package de.atb.context.monitoring.models;

/*-
 * #%L
 * ATB Context Monitoring Core Services
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import org.simpleframework.xml.Root;

import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;
import thewebsemantic.Transient;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * A TankRefillingMonitoringData describes
 * 
 * @author scholze
 * @version $LastChangedRevision: 881 $
 * 
 */
@RdfType("TankRefilling")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Root
public class TankRefillingMonitoringData implements IMonitoringDataModel<TankRefillingMonitoringData, FileSystemDataSource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6988279358987251276L;

	protected String documentIndexId;
	protected String documentUri;

	protected String implementingClassName = TankRefillingMonitoringData.class.getName();
	protected Date monitoredAt;
	protected FileSystemDataSource dataSource;
	protected String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
	protected String identifier;

	@Transient
	private Map<Long, Map<String, SensoricalTankInformation>> timeStampMapping = new HashMap<Long, Map<String, SensoricalTankInformation>>();
	@Transient
	private List<Long> sortedTimestamps = new ArrayList<Long>();
	@Transient
	private List<String> tankNames = new ArrayList<String>();

	/**
	 * The List of tanks that were monitored.
	 */
	protected List<Tank> tanks = new ArrayList<Tank>();

	/**
	 * Adds another monitored Tank to the list of monitored tanks.
	 * 
	 * @param tank
	 *            The monitored Tank to be added to the list of monitored tanks.
	 */
	public void addTank(Tank tank) {
		this.tanks.add(tank);
	}

	@Override
	public FileSystemDataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public void setDataSource(FileSystemDataSource ds) {
		this.dataSource = ds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.IMonitoringData#fromRDFModel(com.hp.hpl
	 * .jena.rdf.model.Model)
	 */
	@Override
	public TankRefillingMonitoringData fromRdfModel(Model model) {
		return RdfHelper.createMonitoringData(model, TankRefillingMonitoringData.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.IMonitoringData#fromXMPString(java.lang
	 * .String)
	 */
	@Override
	public TankRefillingMonitoringData fromRdfModel(String rdfString) {
		return RdfHelper.createMonitoringData(rdfString, TankRefillingMonitoringData.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.models.IMonitoringDataModel#getBusinessCase
	 * ()
	 */
	@Override
	public BusinessCase getBusinessCase() {
		return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.models.IMonitoringDataModel#getDocumentIndexId
	 * ()
	 */
	@Override
	public String getDocumentIndexId() {
		return this.documentIndexId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.models.IMonitoringDataModel#getDocumentUri
	 * ()
	 */
	@Override
	public String getDocumentUri() {
		return this.documentUri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.IMonitoringData#getImplementedClass()
	 */
	@Override
	public String getImplementingClassName() {
		return this.implementingClassName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.models.IMonitoringDataModel#getMonitoredAt
	 * ()
	 */
	@Override
	public Date getMonitoredAt() {
		if (this.monitoredAt != null) {
			return (Date) this.monitoredAt.clone();
		} else {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.models.IMonitoringDataModel#
	 * getMonitoringDataVersion()
	 */
	@Override
	public String getMonitoringDataVersion() {
		return this.monitoringDataVersion;
	}

	/**
	 * Gets the List of Tanks that were monitored. Each tank has
	 * {@linkplain SensoricalTankInformation} attached, describing the different
	 * sensorical information monitored.
	 * 
	 * @return a List of Tanks monitored.
	 */
	public List<Tank> getTanks() {
		return this.tanks;
	}

	public void setDocumentIndexId(String documentIndexId) {
		this.documentIndexId = documentIndexId;
	}

	public void setDocumentUri(String documentUri) {
		this.documentUri = documentUri;
	}

	public void setImplementingClass(String clazz) {
		this.implementingClassName = clazz;
	}

	public void setMonitoredAt(Date monitoredAt) {
		if (monitoredAt != null) {
			this.monitoredAt = (Date) monitoredAt.clone();
		} else {
			this.monitoredAt = null;
		}
	}

	public void setMonitoringDataVersion(String version) {
		this.monitoringDataVersion = version;
	}

	public void setTanks(List<Tank> tanks) {
		this.tanks = tanks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.IMonitoringData#toRDFModel()
	 */
	@Override
	public Model toRdfModel() {
		return RdfHelper.createRdfModel(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.IMonitoringData#toXMPString()
	 */
	@Override
	public String toRdfString() {
		return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(String.format(" (%d tanks)", Integer.valueOf(this.tanks.size())));
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.models.IMonitoringDataModel#
	 * triggersContextChange()
	 */
	@Override
	public boolean triggersContextChange() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.models.IMonitoringDataModel#getIdentifier
	 * ()
	 */
	@Id
	@Override
	public String getIdentifier() {
		if (this.identifier == null) {
			this.identifier = UUID.randomUUID().toString();
		}
		return this.identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.models.IMonitoringDataModel#
	 * getApplicationScenario()
	 */
	@Override
	public ApplicationScenario getApplicationScenario() {
		return ApplicationScenario.getInstance(getBusinessCase());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.atb.proseco.monitoring.models.IMonitoringDataModel#
	 * getContextIdentifierClassName()
	 */
	@Override
	public String getContextIdentifierClassName() {
		return null /*TankRefillingContextIdentifier.class.getName()*/;
	}

	public List<Long> getTimestamps() {
		return sortedTimestamps;
	}

	public List<String> getTankNames() {
		return tankNames;
	}

	public SensoricalTankInformation getSensoricalTankInformation(Long timeStamp, String tankName) {
		if (timeStampMapping != null) {
			Map<String, SensoricalTankInformation> subMap = timeStampMapping.get(timeStamp);
			return subMap.get(tankName);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.atb.proseco.monitoring.models.IMonitoringDataModel#initialize()
	 */
	@Override
	public void initialize() {
		timeStampMapping = new HashMap<Long, Map<String, SensoricalTankInformation>>();
		tankNames = new ArrayList<String>();
		sortedTimestamps = new ArrayList<Long>();
		if ((tanks != null) && (tanks.size() > 0)) {
			for (Tank tank : tanks) {
				tankNames.add(tank.getName());
				for (SensoricalTankInformation info : tank.getSensoricalTankInformation()) {
					Long timestamp = info.getTimestamp();
					Map<String, SensoricalTankInformation> subMap = timeStampMapping.get(timestamp);
					if (!sortedTimestamps.contains(timestamp)) {
						sortedTimestamps.add(timestamp);
					}
					if (subMap == null) {
						subMap = new HashMap<String, SensoricalTankInformation>();
					}
					subMap.put(tank.getName(), info);
					timeStampMapping.put(timestamp, subMap);
				}
			}
		}

		Collections.sort(sortedTimestamps);
	}
}
