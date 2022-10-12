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
import java.util.UUID;

import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;
import thewebsemantic.Transient;

/**
 * TankSensoricalInformation
 *
 * @author scholze
 * @version $LastChangedRevision: 881 $
 *
 */
@RdfType("SensoricalTankInformation")
@Namespace(BusinessCase.NS_DUMMY_URL)
public class SensoricalTankInformation implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5851024469044552342L;

	@Transient
	public static final int CSV_COLUMNS_COUNT = 6;

	protected float maximumMaterialRecirculationPressure;

	protected float actualMaterialRecirculationPressure;

	protected float materialTemperature;

	protected float nominalPumpSpeed;

	protected float actualPumpSpeed;

	protected float fillingLevel;

	protected Long timestamp;

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

	public float getMaximumMaterialRecirculationPressure() {
		return this.maximumMaterialRecirculationPressure;
	}

	public void setMaximumMaterialRecirculationPressure(float maximumMaterialRecirculationPressure) {
		this.maximumMaterialRecirculationPressure = maximumMaterialRecirculationPressure;
	}

	public float getActualMaterialRecirculationPressure() {
		return this.actualMaterialRecirculationPressure;
	}

	public void setActualMaterialRecirculationPressure(float actualMaterialRecirculationPressure) {
		this.actualMaterialRecirculationPressure = actualMaterialRecirculationPressure;
	}

	public float getMaterialTemperature() {
		return this.materialTemperature;
	}

	public void setMaterialTemperature(float materialTemperature) {
		this.materialTemperature = materialTemperature;
	}

	public float getNominalPumpSpeed() {
		return this.nominalPumpSpeed;
	}

	public void setNominalPumpSpeed(float nominalPumpSpeed) {
		this.nominalPumpSpeed = nominalPumpSpeed;
	}

	public float getActualPumpSpeed() {
		return this.actualPumpSpeed;
	}

	public void setActualPumpSpeed(float actualPumpSpeed) {
		this.actualPumpSpeed = actualPumpSpeed;
	}

	public float getFillingLevel() {
		return this.fillingLevel;
	}

	public void setFillingLevel(float fillingLevel) {
		this.fillingLevel = fillingLevel;
	}

	public Long getTimestamp() {
		// if (this.timestamp != null) {
		// return (Date) this.timestamp.clone();
		// } else {
		// return null;
		// }
		return this.timestamp;
	}

	public void setTimestamp(Long timestamp) {
		// if (timestamp != null) {
		// this.timestamp = (Date) timestamp.clone();
		// } else {
		// this.timestamp = null;
		// }
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WpMax: ");
		builder.append(this.getMaximumMaterialRecirculationPressure());
		builder.append(", Xp: ");
		builder.append(this.getActualMaterialRecirculationPressure());
		builder.append(", XT: ");
		builder.append(this.getMaterialTemperature());
		builder.append(", Wn: ");
		builder.append(this.getNominalPumpSpeed());
		builder.append(", Xn: ");
		builder.append(this.getActualPumpSpeed());
		builder.append(", Xq: ");
		builder.append(this.getFillingLevel());
		return builder.toString();
	}
}
