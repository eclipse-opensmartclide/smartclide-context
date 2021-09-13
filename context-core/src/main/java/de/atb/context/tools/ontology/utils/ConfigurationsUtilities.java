package de.atb.context.tools.ontology.utils;

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


import de.atb.context.services.faults.ContextFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 *
 * @author Guilherme
 */
public class ConfigurationsUtilities {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfigurationsUtilities.class);
    static String deployableConfigStringId = "Task_";
    static String configNamespace = ("ContextHandling_Ontological_Model:");

    private ConfigurationsUtilities() {}

    public static boolean writeDeployableConfiguration(String pesId, String configId, String configJsonString, KMBConfigsVocabulary voc, String classSimpleName) {
        throw new ContextFault("Not implemented.");
    }



    public static String unifyStringForKMBInformation(String ontName, String configId) {
        String res = new StringBuilder("{\"").append(ontName).append("\":\"").append(configId).append("\"}").toString();
        res = res.replace("\"","\"\"");
        return res;
    }

    public static String createConfigurationIdForDeployableService() {
        return new StringBuilder(UUID.randomUUID().toString()).toString();
    }

    public static String placeNameSpaceBeforeName(String classSimpleName) {
        return configNamespace + classSimpleName;
    }
}
