package org.eclipse.opensmartclide.context.persistence.common;

/*
 * #%L
 * ATB Context Extraction Core Lib
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

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.eclipse.opensmartclide.context.common.util.ApplicationScenario;
import org.eclipse.opensmartclide.context.common.util.BusinessCase;
import org.eclipse.opensmartclide.context.common.util.IApplicationScenarioProvider;
import org.eclipse.opensmartclide.context.context.util.OntologyNamespace;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistencePostProcessor;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistencePreProcessor;
import org.eclipse.opensmartclide.context.persistence.processors.IPersistenceProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Repository
 *
 * @param <T> Param
 * @author scholze
 * @version $LastChangedRevision: 703 $
 */
public abstract class Repository<T extends IApplicationScenarioProvider> implements IPersistenceUnit<T> {

    private static final Logger logger = LoggerFactory.getLogger(Repository.class);

    protected final String basicLocation;

    protected Map<ApplicationScenario, List<IPersistenceProcessor<T>>> postProcessors = new HashMap<>(0);
    protected Map<ApplicationScenario, List<IPersistenceProcessor<T>>> preProcessors = new HashMap<>(0);

    // FIXME: useReasoner is never used in any of these createDefaultModel methods
    public final synchronized <M extends Model> M createDefaultModel(final Class<M> clazz,
                                                                     final BusinessCase bc,
                                                                     final String modelUri,
                                                                     final boolean useReasoner) {
        final M model = createDefaultModel(clazz, bc);
        model.read(modelUri);
        return model;
    }

    // FIXME: this method does not seem to be used
    public final synchronized <M extends Model> M createDefaultModel(final Class<M> clazz,
                                                                     final BusinessCase bc,
                                                                     final boolean useReasoner) {
        final String modelUri = OntologyNamespace.getOntologyLocation(bc);
        final M model = createDefaultModel(clazz, bc);
        model.read(modelUri);
        return model;
    }

    // FIXME: this method does not seem to be used
    public final synchronized <M extends Model> M createDefaultModel(final Class<M> clazz,
                                                                     final OntologyNamespace ns,
                                                                     final boolean useReasoner) {
        final BusinessCase bc = ns.getBusinessCase();
        if (bc != null) {
            final M model = createDefaultModel(clazz, bc);
            model.read(ns.getAbsoluteUri());
            return model;
        } else {
            throw new IllegalArgumentException(
                "OntologyNamespace entitiy '" + ns + "' must provide a BusinessCase, not null!"
            );
        }
    }

    // FIXME: return value is never used, so maybe not necessary?
    @SuppressWarnings("unchecked")
    public final synchronized <M extends Model> M createDefaultModel(final BusinessCase bc,
                                                                     final Model model,
                                                                     final boolean useReasoner) {
        return (M) createDefaultModel((Class<M>) model.getClass(), bc).add(model);
    }

    public final synchronized <M extends Model> M createDefaultModel(final Class<M> clazz, final BusinessCase bc) {
        return initializeDefaultModel(clazz, bc, this.basicLocation, false);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#addPersistencePreProcessor(ApplicationScenario, IPersistencePreProcessor)
     */
    @Override
    public final synchronized boolean addPersistencePreProcessor(final ApplicationScenario scenario,
                                                                 final IPersistencePreProcessor<T> preProcessor) {
        return addPersistenceProcessor(scenario, preProcessors, preProcessor);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#addPersistencePostProcessor(ApplicationScenario, IPersistencePostProcessor)
     */
    @Override
    public final synchronized boolean addPersistencePostProcessor(final ApplicationScenario scenario,
                                                                  final IPersistencePostProcessor<T> postProcessor) {
        return addPersistenceProcessor(scenario, postProcessors, postProcessor);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#removePersistencePreProcessor(ApplicationScenario, IPersistencePreProcessor)
     */
    @Override
    public final synchronized boolean removePersistencePreProcessor(final ApplicationScenario scenario,
                                                                    final IPersistencePreProcessor<T> preProcessor) {
        return removePersistenceProcessor(scenario, preProcessors, preProcessor.getId());
    }

    @Override
    public final synchronized boolean removePersistencePreProcessor(final ApplicationScenario scenario,
                                                                    final String id) {
        return removePersistenceProcessor(scenario, preProcessors, id);
    }

    /**
     * (non-Javadoc)
     *
     * @see IPersistenceUnit#removePersistencePostProcessor(ApplicationScenario, IPersistencePostProcessor)
     */
    @Override
    public final synchronized boolean removePersistencePostProcessor(final ApplicationScenario scenario,
                                                                     final IPersistencePostProcessor<T> postProcessor) {
        return removePersistenceProcessor(scenario, postProcessors, postProcessor.getId());
    }

    @Override
    public final synchronized boolean removePersistencePostProcessor(
        final ApplicationScenario scenario, final String id) {
        return removePersistenceProcessor(scenario, postProcessors, id);
    }

    protected static synchronized String getLocationForBusinessCase(final String baseUri, final BusinessCase bc) {
        final Path dataDirPath = ContextPathUtils.getDataDirPath();
        return String.format("%s%s%s%s%s", dataDirPath, File.separator, baseUri, File.separator, bc);
    }

    protected abstract void shuttingDown();

    protected abstract boolean reset(BusinessCase bc);

    protected abstract Dataset getDataSet(BusinessCase bc);

    protected Repository(final String baseLocation) {
        validateString(baseLocation, "baseLocation");
        this.basicLocation = baseLocation;
        createShutdownHook();
    }

    protected final void validateNotNull(final Object paramValue, final String paramName) {
        Objects.requireNonNull(paramValue, paramName + " may not be null!");
    }

    protected final void validateString(final String paramValue, final String paramName) {
        validateNotNull(paramValue, paramName);
        if (StringUtils.isBlank(paramValue)) {
            throw new IllegalArgumentException(paramName + " may not be empty!");
        }
    }

    protected final void createShutdownHook() {
        final Thread shutdownHook = new Thread(this::shuttingDown, "Shutdown Hook for repository at '" + basicLocation + "'");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    protected final synchronized boolean implementsInterface(final Class<?> clazz, final Class<?> iFace) {
        for (final Class<?> hasToImplement : clazz.getInterfaces()) {
            if (hasToImplement == iFace) {
                return true;
            }
        }
        return false;
    }

    protected final synchronized String getLocationForBusinessCase(final BusinessCase bc) {
        return Repository.getLocationForBusinessCase(this.basicLocation, bc);
    }

    // FIXME:value of useReasoner is always false
    @SuppressWarnings("unchecked")
    protected <M extends Model> M initializeDefaultModel(final Class<M> clazz,
                                                         final BusinessCase bc,
                                                         final String modelLocation,
                                                         final boolean useReasoner) { // TODO this should maybe replace by a call to DRM API
        final String modelDirStr = Repository.getLocationForBusinessCase(modelLocation, bc);
        final File modelDir = new File(modelDirStr);
        if (!modelDir.exists() && !modelDir.mkdirs()) {
            throw new IllegalArgumentException("Directory '" + modelDir + "' does not exist and cannot be created.");
        }
        M toReturn;
        if (clazz == OntModel.class || implementsInterface(clazz, OntModel.class)) {
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            if (useReasoner) {
                ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                ontModel.prepare();
            }
            toReturn = (M) ontModel;
        } else if (clazz == Model.class || implementsInterface(clazz, Model.class)) {
            toReturn = (M) ModelFactory.createDefaultModel();
        } else {
            throw new IllegalArgumentException(
                "Clazz must be one of org.apache.jena.ontology.OntModel or org.apache.jena.rdf.Model!"
            );
        }
        return toReturn;
    }

    @Override
    public final void triggerPreProcessors(final ApplicationScenario appScenario, final T object) {
        triggerProcessors(appScenario, object, this.preProcessors.get(appScenario), "Pre");
    }

    @Override
    public final void triggerPostProcessors(final ApplicationScenario appScenario, final T object) {
        triggerProcessors(appScenario, object, this.postProcessors.get(appScenario), "Post");
    }

    protected final void triggerProcessors(final ApplicationScenario appScenario,
                                           final T object,
                                           final List<IPersistenceProcessor<T>> processors,
                                           final String processorType) {
        if (processors != null) {
            logger.info("Triggering Persistence {}-Processors for '{}'", processorType, appScenario);
            logger.debug(
                "Triggering {} Persistence {}-Processor(s) for '{}'",
                processors.size(),
                processorType,
                appScenario
            );
            if (!processors.isEmpty()) {
                for (final IPersistenceProcessor<T> processor : processors) {
                    logger.debug(
                        "Triggering Persistence {}-Processor '{}' for '{}'",
                        processorType,
                        processor.getId(),
                        appScenario
                    );
                    processor.process(object);
                }
            }
        }
    }

    protected final synchronized boolean addPersistenceProcessor(
        final ApplicationScenario scenario,
        final Map<ApplicationScenario, List<IPersistenceProcessor<T>>> map,
        final IPersistenceProcessor<T> processor
    ) {
        List<IPersistenceProcessor<T>> list = map.get(scenario);
        boolean exists = false;
        if (list == null) {
            list = new ArrayList<>();
            list.add(processor);
        } else {
            for (final IPersistenceProcessor<T> p : list) {
                if ((p.getId() != null) && p.getId().equals(processor.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                list.add(processor);
            }
        }
        map.put(scenario, list);
        return !exists;
    }

    protected final synchronized boolean removePersistenceProcessor(
        final ApplicationScenario scenario,
        final Map<ApplicationScenario, List<IPersistenceProcessor<T>>> map,
        final String id
    ) {
        final List<IPersistenceProcessor<T>> list = map.get(scenario);
        boolean removed = false;
        if (list != null) {
            for (int i = list.size() - 1; i > -1; i--) {
                final IPersistenceProcessor<T> p = list.get(i);
                if ((p.getId() != null) && p.getId().equals(id)) {
                    list.remove(i);
                    removed = true;
                    break;
                }
            }
            map.put(scenario, list);
        }
        return removed;
    }
}
