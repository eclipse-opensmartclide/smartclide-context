package org.eclipse.opensmartclide.context.monitoring.models;

/*
 * #%L
 * ATB Context Monitoring Core Services
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.eclipse.opensmartclide.context.common.Version;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.monitoring.IMonitoringData;
import org.eclipse.opensmartclide.context.monitoring.config.models.datasources.FileSystemDataSource;
import org.eclipse.opensmartclide.context.monitoring.rdf.RdfHelper;
import org.eclipse.opensmartclide.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Root;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;
import thewebsemantic.Transient;

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
    @Id
    private String identifier;

	@Transient
	private Map<Long, Map<String, SensoricalTankInformation>> timeStampMapping = new HashMap<>();
	@Transient
	private List<Long> sortedTimestamps = new ArrayList<>();
	@Transient
	private List<String> tankNames = new ArrayList<>();

	/**
	 * The List of tanks that were monitored.
	 */
    private List<Tank> tanks = new ArrayList<>();

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

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringData#fromRdfModel(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public TankRefillingMonitoringData fromRdfModel(Model model) {
		return RdfHelper.createMonitoringData(model, TankRefillingMonitoringData.class);
	}

	/**
	 * (non-Javadoc)
	 *
     * @see IMonitoringData#fromRdfModel(java.lang.String)
	 */
	@Override
	public TankRefillingMonitoringData fromRdfModel(String rdfString) {
		return RdfHelper.createMonitoringData(rdfString, TankRefillingMonitoringData.class);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#getBusinessCase()
	 */
	@Override
	public BusinessCase getBusinessCase() {
		return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringData#toRdfModel()
	 */
	@Override
	public Model toRdfModel() {
		return RdfHelper.createRdfModel(this);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringData#toRdfString()
	 */
	@Override
	public String toRdfString() {
		return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
        return String.format("%s (%d tanks)", getClass().getSimpleName(), this.tanks.size());
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#triggersContextChange()
	 */
	@Override
	public boolean triggersContextChange() {
		return true;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#getApplicationScenario()
	 */
	@Override
	public ApplicationScenario getApplicationScenario() {
		return ApplicationScenario.getInstance(getBusinessCase());
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#getContextIdentifierClassName()
	 */
	@Override
	public String getContextIdentifierClassName() {
		return null /*TankRefillingContextIdentifier.class.getName()*/;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#initialize()
	 */
	@Override
	public void initialize() {
		timeStampMapping = new HashMap<>();
		tankNames = new ArrayList<>();
		sortedTimestamps = new ArrayList<>();
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
						subMap = new HashMap<>();
					}
					subMap.put(tank.getName(), info);
					timeStampMapping.put(timestamp, subMap);
				}
			}
		}

		Collections.sort(sortedTimestamps);
	}
}
