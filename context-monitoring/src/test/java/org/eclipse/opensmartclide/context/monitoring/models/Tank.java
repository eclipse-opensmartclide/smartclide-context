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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

/**
 * Tank
 *
 * @author scholze
 * @version $LastChangedRevision: 881 $
 *
 */
@RdfType("Tank")
@Namespace(BusinessCase.NS_BASE_URL)
public class Tank implements Iterable<SensoricalTankInformation>, Cloneable, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4036681492756962749L;

	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	protected List<SensoricalTankInformation> sensoricalInformationList = new ArrayList<SensoricalTankInformation>();

	protected String name;

	private String id;

	@Id
	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Tank() {

	}

	public Tank(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addSensoricalTankInformation(SensoricalTankInformation information) {
		this.sensoricalInformationList.add(information);
	}

	public List<SensoricalTankInformation> getSensoricalTankInformation() {
		return this.sensoricalInformationList;
	}

	public void setSensoricalTankInformation(List<SensoricalTankInformation> informationList) {
		this.sensoricalInformationList = informationList;
	}

	@SuppressWarnings("boxing")
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name);
		if (this.sensoricalInformationList.size() > 0) {
			builder.append(" {");
			for (SensoricalTankInformation entry : this.sensoricalInformationList) {

				builder.append("\n\t" + dateFormat.format(new Date(entry.getTimestamp())) + " => ");
				builder.append(String.valueOf(entry));
			}
			builder.append("\n}");
		}
		return builder.toString();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<SensoricalTankInformation> iterator() {
		return this.sensoricalInformationList.iterator();
	}
}
