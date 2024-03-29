package org.eclipse.opensmartclide.context.common.util;

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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.opensmartclide.context.common.configuration.ApplicationScenarioConfiguration;
import org.eclipse.opensmartclide.context.common.configuration.IConfigurationBean;
import org.eclipse.opensmartclide.context.learning.models.IModelInitializer;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApplicationScenario
 *
 * @author scholze, huesig
 * @version $LastChangedRevision: 701 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@ToString(exclude = {"logger", "initializer"})
public class ApplicationScenario implements IModelInitializer {
    @XmlTransient
    private final Logger logger = LoggerFactory
            .getLogger(ApplicationScenario.class);

    private BusinessCase businessCase;
    @XmlElement
    private String modelInitializerClassName;
    @XmlElement
    private String configurationClassName;
    @XmlElement
    private String configurationDialogClassName;
    private Class<? extends ApplicationScenarioConfiguration<?>> configurationClass;
    @XmlTransient
    private IModelInitializer initializer;
    private static volatile Map<String, ApplicationScenario> settings = new HashMap<String, ApplicationScenario>();

    public ApplicationScenario() {
    }

    public static ApplicationScenario getInstance() {
        if (settings.get("DUMMY_SCENARIO") == null) {
            settings.put("DUMMY_SCENARIO", new ApplicationScenario(BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL)));
        }
        return settings.get("DUMMY_SCENARIO");
    }

    public static ApplicationScenario getInstance(BusinessCase businessCase) {
        if (settings.get(businessCase) == null) {
            settings.put(businessCase.toString(), new ApplicationScenario(businessCase));
        }
        return settings.get(businessCase.toString());
    }

    public static ApplicationScenario getInstance(BusinessCase businessCase, String modelInitializerClassName) {
        if (settings.get(businessCase) == null) {
            settings.put(businessCase.toString(), new ApplicationScenario(businessCase, modelInitializerClassName));
        }
        return settings.get(businessCase.toString());
    }

    ApplicationScenario(final BusinessCase businessCase) {
        this(businessCase, "java.lang.Object");
    }

    ApplicationScenario(final BusinessCase businessCase,
                        final String modelInitializerClassName) {
        this(businessCase, modelInitializerClassName, null, null);
    }

    ApplicationScenario(final BusinessCase businessCase,
                        final String modelInitializerClassName,
                        final String configurationClassName) {
        this(businessCase, modelInitializerClassName, configurationClassName,
                null);
    }

    ApplicationScenario(final BusinessCase businessCase,
                        final String modelInitializerClassName,
                        final String configurationClassName,
                        final String configDialogClassName) {
        this.businessCase = businessCase;
        this.modelInitializerClassName = modelInitializerClassName;
        this.configurationClassName = configurationClassName;
        configurationDialogClassName = configDialogClassName;
    }

    public BusinessCase getBusinessCase() {
        return businessCase;
    }

    public static ApplicationScenario[] values() {
        return settings.values().toArray(new ApplicationScenario[0]);
    }

    public static ApplicationScenario[] values(final BusinessCase businessCase) {
        final ApplicationScenario[] scenarios = ApplicationScenario.values();
        final List<ApplicationScenario> filteredScenarios = new ArrayList<>(
                scenarios.length);
        for (final ApplicationScenario scenario : scenarios) {
            if (scenario.getBusinessCase() == businessCase) {
                filteredScenarios.add(scenario);
            }
        }
        return filteredScenarios
                .toArray(new ApplicationScenario[filteredScenarios.size()]);
    }

    /**
     * (non-Javadoc)
     *
     * @see IModelInitializer#getScenario()
     */
    @Override
    public ApplicationScenario getScenario() {
        if ((initializer != null) || createInitializer()) {
            return initializer.getScenario();
        }
        return null;
    }

    /**
     * (non-Javadoc)
     *
     * @see IModelInitializer#initializeModel(String)
     */
    @Override
    public boolean initializeModel(final String filePath) {
        if ((initializer != null) || createInitializer()) {
            return initializer.initializeModel(filePath);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends IConfigurationBean> Class<? extends ApplicationScenarioConfiguration<T>> getConfigurationClass() {
        if (configurationClass == null) {
            createConfigurationClass();
        }
        return (Class<? extends ApplicationScenarioConfiguration<T>>) configurationClass;
    }

    public IModelInitializer getInitializer() {
        if (initializer == null) {
            createInitializer();
        }
        return initializer;
    }

    @SuppressWarnings("unchecked")
    protected boolean createConfigurationClass() {
        try {
            // FIXME: temporary workaround
            if (configurationClass == null && configurationClassName != null) {
                configurationClass = (Class<? extends ApplicationScenarioConfiguration<?>>) Class.forName(configurationClassName);
                return true;
            }
        } catch (final ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected boolean createInitializer() {
        try {
            if (initializer == null) {
                final Class<? extends IModelInitializer> clazz = (Class<? extends IModelInitializer>) Class.forName(modelInitializerClassName);
                initializer = clazz.getDeclaredConstructor().newInstance();
            }
            return true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}
