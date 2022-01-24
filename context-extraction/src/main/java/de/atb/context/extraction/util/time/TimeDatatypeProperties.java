package de.atb.context.extraction.util.time;

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
import de.atb.context.common.util.BusinessCase;
import de.atb.context.extraction.util.IOntPropertyProvider;
import de.atb.context.context.util.IOntologyResource;
import de.atb.context.context.util.OntologyNamespace;
import lombok.Getter;

/**
 * TimeDatatypeProperties
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
@Getter
public enum TimeDatatypeProperties implements IOntologyResource, IOntPropertyProvider<DatatypeProperty> {

	day("day"),

	dayOfYear("dayOfYear"),

	days("days"),

	hour("hour"),

	hours("hours"),

	inXSDDateTime("inXSDDateTime"),

	minute("minute"),

	minutes("minutes"),

	month("month"),

	months("months"),

	second("second"),

	seconds("seconds"),

	week("week"),

	weeks("weeks"),

	xsdDateTime("xsdDateTime"),

	year("year"),

	years("years"),

	;

	/**
	 * Gets the local name that identifies the Resource within the ontology.
	 */
	private final String localName;

	private Class<?>[] domains = new Class<?>[] { Object.class };
	private Class<?>[] ranges = new Class<?>[] { Object.class };

	/**
	 * Gets the Namespace this DatatypeProperty belongs to.
	 */
	private final OntologyNamespace namespace = OntologyNamespace.getInstance("time", BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL), "http://www.w3.org/2006/time");

	TimeDatatypeProperties(final String localName) {
		this.localName = localName;
	}

	TimeDatatypeProperties(final String localName, final Class<?>[] ranges) {
		this.localName = localName;
		this.ranges = ranges;
	}

	TimeDatatypeProperties(final String localName, final Class<?> rangeClass) {
		this.localName = localName;
		ranges = new Class<?>[] { rangeClass };
	}

	TimeDatatypeProperties(final String localName, final Class<?> domainClass, final Class<?> rangeClass) {
		this.localName = localName;
		ranges = new Class<?>[] { rangeClass };
		domains = new Class<?>[] { domainClass };
	}

	TimeDatatypeProperties(final String localName, final Class<?> domainClass, final Class<?>[] ranges) {
		this.localName = localName;
		this.ranges = ranges;
		domains = new Class<?>[] { domainClass };
	}

	TimeDatatypeProperties(final String localName, final Class<?>[] domains, final Class<?> rangeClass) {
		this.localName = localName;
		ranges = domains;
		this.domains = new Class<?>[] { rangeClass };
	}

	TimeDatatypeProperties(final String localName, final Class<?>[] domains, final Class<?>[] ranges) {
		this.localName = localName;
		this.ranges = ranges;
		this.domains = domains;
	}

	// @Override
	// public String getNameSpace(OntModel model) {
	// return getNameSpace();
	// // return model.getNsPrefixURI(this.namespace.getLocalName());
	// }

	/**
	 * (non-Javadoc)
	 *
	 * @see de.atb.context.context.util.IOntologyResource#getNameSpace()
	 */
	@Override
	public String getNameSpace() {
		return namespace.getAbsoluteUri();
	}

	@Override
	public String getNameSpacePrefix() {
		return namespace.getLocalName();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see de.atb.context.extraction.util.IOntPropertyProvider#getProperty(org.apache.jena.ontology.OntModel)
	 */
	@Override
	public DatatypeProperty getProperty(final OntModel model) {
		return model.getDatatypeProperty(getURI());
	}

	// @Override
	// public String getURI(OntModel model) {
	// String nsPrefixUri = model.getNsPrefixURI(this.namespace.getLocalName());
	// if (nsPrefixUri != null) {
	// return nsPrefixUri + this.localName;
	// } else {
	// return this.namespace.getAbsoluteUri() + "#" + this.localName;
	// }
	//
	// // return model.getNsPrefixURI(this.namespace.getLocalName()) +
	// // this.localName;
	// }

	@Override
	public String getURI() {
		return namespace.getAbsoluteUri() + "#" + localName;
	}

	@Override
	public String toString() {
		return localName;
	}
}
