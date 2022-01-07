package de.atb.context.common;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContextPathUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextPathUtils.class);

    private static final String BASE_DIR_LINUX = "/var/lib/smartclide";
    private static final String BASE_DIR_LINUX_ALTERNATIVE = "/opt/smartclide";
    private static final String BASE_DIR_WINDOWS = "C:\\ProgramData\\smartclide";
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
        if (smartclideHome != null && Files.isDirectory(Paths.get(smartclideHome))) {
            baseDirPath = Path.of(smartclideHome);
        } else if (Files.isDirectory(Paths.get(BASE_DIR_LINUX))) {
            // Linux config directory
            baseDirPath = Path.of(BASE_DIR_LINUX);
        } else if (Files.isDirectory(Paths.get(BASE_DIR_LINUX_ALTERNATIVE))) {
            // alternative Linux config directory
            baseDirPath = Path.of(BASE_DIR_LINUX_ALTERNATIVE);
        } else if (Files.isDirectory(Paths.get(BASE_DIR_WINDOWS))) {
            // Windows Config Directories
            baseDirPath = Path.of(BASE_DIR_WINDOWS);
        } else {
            // not in production, use local target/test-classes folder in current working directory
            baseDirPath = Path.of("", "target", "test-classes");
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
