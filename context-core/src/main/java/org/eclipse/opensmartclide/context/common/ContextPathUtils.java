package org.eclipse.opensmartclide.context.common;

/*-
 * #%L
 * ATB Context Extraction Core Lib
 * %%
 * Copyright (C) 2015 - 2022 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ContextPathUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextPathUtils.class);

    private static final Path BASE_DIR_LINUX = Path.of("/var/lib/smartclide");
    private static final Path BASE_DIR_LINUX_ALTERNATIVE = Path.of("/opt/smartclide");
    private static final Path BASE_DIR_WINDOWS = Path.of("C:\\ProgramData\\smartclide");
    private static final Path BASE_DIR_TEST_CLASSES = Path.of("", "target", "test-classes");
    private static final String SMARTCLIDE_HOME_VAR_NAME = "SMARTCLIDE_HOME";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String DATA_DIR_NAME = "data";

    public static Path getConfigDirPath() {
        final Path configDirPath = getBaseDirPath().resolve(CONFIG_DIR_NAME).toAbsolutePath();
        createDirectoryIfNotExists(configDirPath, CONFIG_DIR_NAME);
        return configDirPath;
    }

    public static Path getDataDirPath() {
        final Path dataDirPath = getBaseDirPath().resolve(DATA_DIR_NAME).toAbsolutePath();
        createDirectoryIfNotExists(dataDirPath, DATA_DIR_NAME);
        return dataDirPath;
    }

    private static Path getBaseDirPath() {
        final Path baseDirPath;
        // Environment Variable
        final String smartclideHome = System.getenv(SMARTCLIDE_HOME_VAR_NAME);
        Path smartclideHomePath = null;
        if (smartclideHome != null) {
            smartclideHomePath = Path.of(smartclideHome);
        }
        if (null != smartclideHomePath && Files.isDirectory(smartclideHomePath)) {
            baseDirPath = smartclideHomePath;
        } else if (Files.isDirectory(BASE_DIR_LINUX)) {
            // Linux config directory
            baseDirPath = BASE_DIR_LINUX;
        } else if (Files.isDirectory(BASE_DIR_LINUX_ALTERNATIVE)) {
            // alternative Linux config directory
            baseDirPath = BASE_DIR_LINUX_ALTERNATIVE;
        } else if (Files.isDirectory(BASE_DIR_WINDOWS)) {
            // Windows Config Directories
            baseDirPath = BASE_DIR_WINDOWS;
        } else {
            // not in production, use local target/test-classes folder in current working directory
            baseDirPath = BASE_DIR_TEST_CLASSES;
        }

        checkIfDirectoryExists(baseDirPath.toAbsolutePath(), "base");

        return baseDirPath;
    }

    private static void createDirectoryIfNotExists(final Path path, final String name) {
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                LOGGER.error(String.format("Failed to create %s directory for Context Handling: %s", name, path));
            }
        }
        checkIfDirectoryExists(path, name);
    }

    private static void checkIfDirectoryExists(final Path path, final String name) {
        if (!Files.isDirectory(path)) {
            final String message = String.format("The %s directory %s for Context Handling does not exist!", name, path);
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }
}
