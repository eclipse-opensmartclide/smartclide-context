package de.atb.context.monitoring.analyser.file;

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

import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.SensoricalTankInformation;
import de.atb.context.monitoring.models.Tank;
import de.atb.context.monitoring.models.TankRefillingMonitoringData;
import de.atb.context.monitoring.parser.IndexedFields;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TrfAnalyser
 * 
 * @author scholze
 * @version $LastChangedRevision: 881 $
 * 
 */
public class TrfAnalyser extends FileAnalyser<TankRefillingMonitoringData> {

	private final Logger logger = LoggerFactory.getLogger(TrfAnalyser.class);

	public static final NumberFormat numberFormat = NumberFormat
			.getInstance(Locale.GERMAN);
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm:ss");

	public TrfAnalyser(DataSource dataSource,
                       InterpreterConfiguration interpreterConfiguration, Indexer indexer,
                       Document document, final AmIMonitoringConfiguration amiConfiguration) {
		super(dataSource, interpreterConfiguration, indexer, document,
				amiConfiguration);
	}

	@Override
	public List<TankRefillingMonitoringData> analyseObject(File file) {
		logger.debug("Analysing '" + file.getAbsolutePath() + "'");

		List<TankRefillingMonitoringData> models = new ArrayList<TankRefillingMonitoringData>();
		FileReader fileReader = null;
		TankRefillingMonitoringData run = null;
		try {
			fileReader = new FileReader(file);
			CsvListReader reader = new CsvListReader(fileReader,
					CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			List<String> line = null;
			run = new TankRefillingMonitoringData();
			int noOfTanks = 0;
			while ((line = reader.read()) != null) {
				if (reader.getLineNumber() == 1) {
					noOfTanks = addTanks(run, line);
				} else if (reader.getLineNumber() > 1) {
					addSensoricalInformation(run, line, noOfTanks);
				}
			}
			reader.close();
			fileReader.close();
			run.setDocumentUri(IndexedFields.Uri.getString(this.document));
			run.setDocumentIndexId(IndexedFields.IndexId
					.getString(this.document));
			run.setMonitoredAt(DateTools.stringToDate(IndexedFields.MonitoredAt
					.getString(this.document)));
			run.setDataSource(this.dataSource
					.convertTo(FileSystemDataSource.class));
			models.add(run);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
		} catch (DateTimeParseException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return models;
	}

	protected int addTanks(TankRefillingMonitoringData run, List<String> line) {
		int noOfTanks = ((line.size() - 1) / SensoricalTankInformation.CSV_COLUMNS_COUNT);

		for (int i = 1; i <= noOfTanks; i++) {
			String name = line.get(i).substring(5);
			Tank tank = new Tank(name);
			run.addTank(tank);
		}

		return noOfTanks;
	}

	@SuppressWarnings("boxing")
	protected void addSensoricalInformation(TankRefillingMonitoringData run,
			List<String> csvLine, int noOfTanks) {
		Date timestamp = new Date();
		try {
			timestamp = dateFormat.parse(csvLine.get(0));
		} catch (ParseException e) {
			logger.warn(e.getMessage(), e);
		}
		for (int i = 0; i < noOfTanks; i++) {
			Tank tank = run.getTanks().get(i);
			float maximumMaterialRecirculationPressure = 0.0f;
			float actualMaterialRecirculationPressure = 0.0f;
			float materialTemperature = 0.0f;
			float nominalPumpSpeed = 0.0f;
			float actualPumpSpeed = 0.0f;
			float fillingLevel = 0.0f;

			SensoricalTankInformation sensoricalInformation = new SensoricalTankInformation();
			sensoricalInformation.setTimestamp(timestamp.getTime());

			try {
				maximumMaterialRecirculationPressure = numberFormat.parse(
						csvLine.get(i + 1)).floatValue();
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			sensoricalInformation
					.setMaximumMaterialRecirculationPressure(maximumMaterialRecirculationPressure);

			try {
				actualMaterialRecirculationPressure = numberFormat.parse(
						csvLine.get((i + 1) + noOfTanks * 1)).floatValue();
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			sensoricalInformation
					.setActualMaterialRecirculationPressure(actualMaterialRecirculationPressure);

			try {
				materialTemperature = numberFormat.parse(
						csvLine.get((i + 1) + noOfTanks * 2)).floatValue();
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			sensoricalInformation.setMaterialTemperature(materialTemperature);

			try {
				nominalPumpSpeed = numberFormat.parse(
						csvLine.get((i + 1) + noOfTanks * 3)).floatValue();
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			sensoricalInformation.setNominalPumpSpeed(nominalPumpSpeed);

			try {
				actualPumpSpeed = numberFormat.parse(
						csvLine.get((i + 1) + noOfTanks * 4)).floatValue();
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			sensoricalInformation.setActualPumpSpeed(actualPumpSpeed);

			try {
				fillingLevel = numberFormat.parse(
						csvLine.get((i + 1) + noOfTanks * 5)).floatValue();
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			sensoricalInformation.setFillingLevel(fillingLevel);

			tank.addSensoricalTankInformation(sensoricalInformation);
		}
	}
}
