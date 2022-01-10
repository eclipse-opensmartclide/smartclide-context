package de.atb.context.services.wrapper;

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


import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import de.atb.context.extraction.ContextContainer;
import de.atb.context.extraction.ContextContainerWrapper;
import de.atb.context.services.IContextRepositoryService;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.TimeFrame;
import de.atb.context.persistence.ModelOutputLanguage;
import de.atb.context.services.faults.ContextFault;
import de.atb.context.services.wrapper.RepositoryServiceWrapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ContextRepositoryServiceWrapper
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 *
 */
public class ContextRepositoryServiceWrapper extends RepositoryServiceWrapper<IContextRepositoryService> {

	protected static final ModelOutputLanguage serializer = ModelOutputLanguage.TURTLE;

	public ContextRepositoryServiceWrapper(final IContextRepositoryService service) throws IllegalArgumentException {
		super(service);
	}

	public final synchronized Model getDefaultModel(final BusinessCase businessCase) throws ContextFault {
		String modelString = service.getDefaultModel(businessCase);
		return ContextRepositoryServiceWrapper.serializer.getModelFromString(modelString);
	}

	public final synchronized void persist(final ContextContainer context) throws ContextFault {
		if (context != null) {
			ContextContainerWrapper wrapper = context.toContextContainerWrapper();
			service.persistContext(wrapper);
		}
	}

	public final synchronized ContextContainer getContext(final ApplicationScenario applicationScenario, final String contextId) throws ContextFault {
		ContextContainerWrapper wrapper = service.getContext(applicationScenario, contextId);
		if (wrapper != null) {
			return wrapper.toContextContainer();
		}
		return null;
	}

	public final synchronized ContextContainer getContext(final BusinessCase businessCase, final String contextId) throws ContextFault {
		ContextContainerWrapper wrapper = service.getContext(businessCase, contextId);
		if (wrapper != null) {
			return wrapper.toContextContainer();
		}
		return null;
	}

	public final synchronized ContextContainer getRawContext(final ApplicationScenario applicationScenario, final String contextId) throws ContextFault {
		ContextContainerWrapper wrapper = service.getRawContext(applicationScenario, contextId);
		if (wrapper != null) {
			return wrapper.toContextContainer();
		}
		return null;
	}

	public final synchronized ContextContainer getRawContext(final BusinessCase businessCase, final String contextId) throws ContextFault {
		ContextContainerWrapper wrapper = service.getRawContext(businessCase, contextId);
		if (wrapper != null) {
			return wrapper.toContextContainer();
		}
		return null;
	}

	public final synchronized Boolean executeSparqlAskQuery(final BusinessCase businessCase, final String query) throws ContextFault {
		return service.executeSparqlAskQuery(businessCase, query);
	}

	public final synchronized ResultSet executeSparqlSelectQuery(final BusinessCase businessCase, final String query) throws ContextFault {
		String resultSetXmlString = service.executeSparqlSelectQuery(businessCase, query);
		return ResultSetFactory.fromXML(IOUtils.toInputStream(resultSetXmlString, StandardCharsets.UTF_8));
	}

	public final synchronized Model executeSparqlDescribeQuery(final BusinessCase businessCase, final String query) throws ContextFault {
		String modelString = service.executeSparqlDescribeQuery(businessCase, query);
		return ContextRepositoryServiceWrapper.serializer.getModelFromString(modelString);
	}

	public final synchronized Model executeSparqlConstructQuery(final BusinessCase businessCase, final String query) throws ContextFault {
		String modelString = service.executeSparqlConstructQuery(businessCase, query);
		return ContextRepositoryServiceWrapper.serializer.getModelFromString(modelString);
	}

	public final synchronized Boolean executeSparqlAskQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
			throws ContextFault {
		return service.executeSparqlAskQuery(businessCase, query, useReasoner);
	}

	public final synchronized ResultSet executeSparqlSelectQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
			throws ContextFault {
		String resultSetXmlString = service.executeSparqlSelectQuery(businessCase, query, useReasoner);
		return ResultSetFactory.fromXML(IOUtils.toInputStream(resultSetXmlString, StandardCharsets.UTF_8));
	}

	public final synchronized Model executeSparqlDescribeQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
			throws ContextFault {
		String modelString = service.executeSparqlDescribeQuery(businessCase, query, useReasoner);
		return ContextRepositoryServiceWrapper.serializer.getModelFromString(modelString);
	}

	public final synchronized Model executeSparqlConstructQuery(final BusinessCase businessCase, final String query, final boolean useReasoner)
			throws ContextFault {
		String modelString = service.executeSparqlConstructQuery(businessCase, query, useReasoner);
		return ContextRepositoryServiceWrapper.serializer.getModelFromString(modelString);
	}

	public final synchronized List<String> getLastContextsIds(final BusinessCase bc, final int count) throws ContextFault {
		return service.getLastContextsIds(bc, count);
	}

	public final synchronized List<String> getLastContextsIds(final BusinessCase bc, final TimeFrame timeFrame) throws ContextFault {
		return service.getLastContextsIds(bc, timeFrame);
	}

	public final synchronized List<String> getLastContextsIds(final ApplicationScenario applicationScenario, final int count) throws ContextFault {
		return service.getLastContextsIds(applicationScenario, count);
	}

	public final synchronized List<String> getLastContextsIds(final ApplicationScenario applicationScenario, final TimeFrame timeFrame)
			throws ContextFault {
		return service.getLastContextsIds(applicationScenario, timeFrame);
	}

	public final synchronized void initializeRepository(final BusinessCase bc, final String modelUri) throws ContextFault {
		service.initializeRepository(bc, modelUri);
	}
}
