package org.eclipse.opensmartclide.context.extraction.util.base;

/*
 * #%L
 * ATB Context Extraction Core Service
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


import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.eclipse.opensmartclide.context.extraction.util.IOntPropertyProvider;
import org.eclipse.opensmartclide.context.context.util.IOntologyResource;
import org.eclipse.opensmartclide.context.context.util.OntologyNamespace;
import lombok.Getter;

/**
 * DataTypeProperties
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
@Getter
public enum BaseDatatypeProperties implements IOntologyResource, IOntPropertyProvider<DatatypeProperty> {

	/**
	 * Represents the BusinessCaseIdentifier DatatypeProperty from the base
	 * namespace.
	 */
	BusinessCaseIdentifier("businessCaseIdentifier"),

	/**
	 * Represents the ApplicationScenarioIdentifier DatatypeProperty from the
	 * base namespace.
	 */
	ApplicationScenarioIdentifier("applicationScenarioIdentifier"),

	/**
	 * Represents the BasedOnMonitoringData DatatypeProperty from the base
	 * namespace.
	 */
	BasedOnMonitoringData("basedOnMonitoringData"),

	/**
	 * Represents the CapturedAt DatatypeProperty from the base namespace.
	 */
	CapturedAt("capturedAt"),

	/**
	 * Represents the Identifier DatatypeProperty from the base namespace.
	 */
	Identifier("identifier"),

	/**
	 * Represents the UnitFloatValue DatatypeProperty from the base namespace.
	 */
	UnitFloatValue("unitFloatValue"),

	/**
	 * Represents the UnitIntegerValue DatatypeProperty from the base namespace.
	 */
	UnitIntegerValue("unitIntegerValue"),

	/**
	 * Represents the ConfigurationType DatatypeProperty from the base
	 * namespace.
	 */
	ConfigurationType("configurationType"),

	;

	/**
	 * Gets the local name that identifies the Resource within the ontology.
	 */
	private String localName;

	private Class<?>[] domains = new Class<?>[] { Object.class };
	private Class<?>[] ranges = new Class<?>[] { Object.class };

	/**
	 * Gets the Namespace this DatatypeProperty belongs to.
	 */
	private final OntologyNamespace namespace = OntologyNamespace.getInstance();

	BaseDatatypeProperties(final String localName) {
		this.localName = localName;
	}

	BaseDatatypeProperties(final String localName, final Class<?>[] ranges) {
		this.localName = localName;
		this.ranges = ranges;
	}

	BaseDatatypeProperties(final String localName, final Class<?> rangeClass) {
		this.localName = localName;
		ranges = new Class<?>[] { rangeClass };
	}

	BaseDatatypeProperties(final String localName, final Class<?> domainClass, final Class<?> rangeClass) {
		this.localName = localName;
		ranges = new Class<?>[] { rangeClass };
		domains = new Class<?>[] { domainClass };
	}

	BaseDatatypeProperties(final String localName, final Class<?> domainClass, final Class<?>[] ranges) {
		this.localName = localName;
		this.ranges = ranges;
		domains = new Class<?>[] { domainClass };
	}

	BaseDatatypeProperties(final String localName, final Class<?>[] domains, final Class<?> rangeClass) {
		this.localName = localName;
		ranges = domains;
		this.domains = new Class<?>[] { rangeClass };
	}

	BaseDatatypeProperties(final String localName, final Class<?>[] domains, final Class<?>[] ranges) {
		this.localName = localName;
		this.ranges = ranges;
		this.domains = domains;
	}

	// @Override
	// public String getURI(OntModel model) {
	// String nsPrefixUri = model.getNsPrefixURI(this.namespace.getLocalName());
	// if (nsPrefixUri != null) {
	// return nsPrefixUri + this.localName;
	// } else {
	// return this.namespace.getAbsoluteUri() + "#" + this.localName;
	// }
	// // return model.getNsPrefixURI(this.namespace.getLocalName()) +
	// // this.localName;
	// }
	@Override
	public String getURI() {
		return namespace.getAbsoluteUri() + "#" + localName;
	}

	@Override
	public String getNameSpacePrefix() {
		return namespace.getLocalName();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IOntologyResource#getNameSpace()
	 */
	@Override
	public String getNameSpace() {
		return namespace.getAbsoluteUri();
	}

	@Override
	public String toString() {
		return localName;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see IOntPropertyProvider#getProperty(org.apache.jena.ontology.OntModel)
	 */
	@Override
	public DatatypeProperty getProperty(final OntModel model) {
		return model.getDatatypeProperty(getURI());
	}
}
