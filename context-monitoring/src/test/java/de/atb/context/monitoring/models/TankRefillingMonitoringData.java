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

import java.time.LocalDateTime;
import java.util.*;

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
@Getter
@Setter
public class TankRefillingMonitoringData implements IMonitoringDataModel<TankRefillingMonitoringData, FileSystemDataSource> {

	private static final long serialVersionUID = 6988279358987251276L;

    private String documentIndexId;
    private String documentUri;

    private String implementingClassName = TankRefillingMonitoringData.class.getName();
    private Date monitoredAt;
    private FileSystemDataSource dataSource;
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private String identifier;

	@Transient
	private Map<Long, Map<String, SensoricalTankInformation>> timeStampMapping = new HashMap<Long, Map<String, SensoricalTankInformation>>();
	@Transient
	private List<Long> sortedTimestamps = new ArrayList<Long>();
	@Transient
	private List<String> tankNames = new ArrayList<String>();

	/**
	 * The List of tanks that were monitored.
	 */
    private List<Tank> tanks = new ArrayList<Tank>();

    public TankRefillingMonitoringData() {
        this.identifier = UUID.randomUUID().toString();
    }

	/**
	 * Adds another monitored Tank to the list of monitored tanks.
	 *
	 * @param tank
	 *            The monitored Tank to be added to the list of monitored tanks.
	 */
	public void addTank(Tank tank) {
		this.tanks.add(tank);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * IMonitoringData#fromRDFModel(com.hp.hpl
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
	 * IMonitoringData#fromXMPString(java.lang
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
	 * IMonitoringDataModel#getBusinessCase
	 * ()
	 */
	@Override
	public BusinessCase getBusinessCase() {
		return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringData#toRDFModel()
	 */
	@Override
	public Model toRdfModel() {
		return RdfHelper.createRdfModel(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringData#toXMPString()
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
	 * @see IMonitoringDataModel#
	 * triggersContextChange()
	 */
	@Override
	public boolean triggersContextChange() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * getApplicationScenario()
	 */
	@Override
	public ApplicationScenario getApplicationScenario() {
		return ApplicationScenario.getInstance(getBusinessCase());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IMonitoringDataModel#
	 * getContextIdentifierClassName()
	 */
	@Override
	public String getContextIdentifierClassName() {
		return null /*TankRefillingContextIdentifier.class.getName()*/;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * IMonitoringDataModel#initialize()
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
