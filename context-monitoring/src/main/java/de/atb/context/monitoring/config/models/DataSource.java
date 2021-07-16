package de.atb.context.monitoring.config.models;

/*
 * #%L
 * ATB Context Monitoring Core Services
 * %%
 * Copyright (C) 2015 - 2020 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.atb.context.monitoring.config.models.datasources.DatabaseDataSource;
import de.atb.context.monitoring.config.models.datasources.FilePairSystemDataSource;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.config.models.datasources.FileTripletSystemDataSource;
import de.atb.context.monitoring.config.models.datasources.IDataSourceOptionValue;
import de.atb.context.monitoring.config.models.datasources.MessageBrokerDataSource;
import de.atb.context.monitoring.config.models.datasources.WebServiceDataSource;
import org.apache.commons.lang3.tuple.Pair;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

/**
 * DataSource
 *
 * @author scholze
 * @version $LastChangedRevision: 156 $
 */
@Element
@RdfType("DataSource")
@Namespace("http://atb-bremen.de/")
public abstract class DataSource implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6260730665728114554L;

    private final Logger logger = LoggerFactory.getLogger(DataSource.class);

    /**
     * Represents the Id of a DataSource. This is used for mapping keys, to
     * lookup DataSource for a certain Monitoring and Interpreter component.
     */
    @Attribute
    protected String id;

    /**
     * Represents the type of DataSource. This is internaly a String, but getter
     * and setter provide DataSourceType enum.
     */
    @Attribute
    protected String type;

    /**
     * Represents the name of the Class that shall monitor this DataSource.
     */
    @Attribute
    protected String monitor;

    /**
     * Represents the URI of this DataSource (may be file- or web-based or
     * whatever).
     */
    @Attribute
    protected String uri;

    /**
     * Represents several options that are validated by subclasses of a
     * DataSource.
     */
    @Attribute(required = false)
    protected String options;

    /**
     * The options string as a map, mapping option name to option value.
     * <p>
     * Do not access directly, always use the corresponding getOptionsMap() method!
     */
    @Transient
    private Map<String, String> optionsMap;

    /**
     * Gets the Id of the DataSource.
     *
     * @return the Id of the DataSource.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * Sets the Id of the DataSource.
     *
     * @param id the Id of the DataSource.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the type of the DataSource. Depending on the type there are several
     * more options available. A FileSystemDataSource for example may for
     * example provide options to include hidden files etc.
     *
     * @return the type of the DataSource.
     */
    public DataSourceType getType() {
        return DataSourceType.valueOf(this.type.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Sets the type of the DataSource.
     *
     * @param type the type of the DataSource.
     */
    public final void setType(final DataSourceType type) {
        this.type = type.toString().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Gets the name of the Class that will be instanciated to monitor the
     * DataSource.
     *
     * @return the name of the Class that will be instanciated to monitor the
     */
    public final String getMonitor() {
        return this.monitor;
    }

    /**
     * Sets the name of the Class that will be instanciated to monitor the
     *
     * @param monitor the name of the Class that will be instanciated to monitor the
     */
    public final void setMonitor(final String monitor) {
        this.monitor = monitor;
    }

    /**
     * Gets the URI of the DataSource that will be monitored.
     *
     * @return the URI of the DataSource that will be monitored.
     */
    public final String getUri() {
        return this.uri;
    }

    /**
     * Sets the URI of the DataSource that will be monitored.
     *
     * @param uri the URI of the DataSource that will be monitored.
     */
    public final void setUri(final String uri) {
        this.uri = uri;
    }

    /**
     * Gets miscellaneous options for the DataSource as a String.
     * <p>
     * The option String will be in the format
     * <code>key=value&amp;key=value&amp;...</code>.
     *
     * @return miscellaneous options for the DataSource as a String.
     */
    public final String getOptions() {
        return this.options;
    }

    /**
     * Returns the options as a map of option name to option value (String).
     *
     * @return Map of option name to option value
     */
    public final Map<String, String> getOptionsMap() {
        if (optionsMap == null) {
            this.optionsMap = optionsToMap();
        }
        return optionsMap;
    }

    /**
     * Sets miscellaneous options for the DataSource as a String.
     * <p>
     * The option String has to be in the format
     * <code>key=value&amp;key=value&amp;...</code>.
     *
     * @param options miscellaneous options for the DataSource as a String.
     */
    public final void setOptions(final String options) {
        this.options = options;
        this.optionsMap = optionsToMap();
    }

    public final void setOptions(final Map<String, String> optionsMap) {
        this.optionsMap = optionsMap;
        this.options = optionsMapToString(optionsMap);
    }

    /**
     * Converts this DataSource to the given class, that has to extend a
     * DataSource class.
     *
     * @param <T>   the type to convert the DataSource to.
     * @param clazz the class of the type to convert the DataSource to.
     * @return a (more specific) DataSource that extends the DataSource and has
     * the given Class.
     */
    public final <T extends DataSource> T convertTo(final Class<? extends T> clazz) {
        return getType().convertTo(clazz, this);
    }

    /**
     * Converts this DataSource to the given class, that has to extend a
     * DataSource class.
     *
     * @param <T>  the type to convert the DataSource to.
     * @param type the DataSourceType containing the class to convert the
     *             DataSource to.
     * @return a (more specific) DataSource that extends the DataSource and has
     * the given Class.
     */
    @SuppressWarnings("unchecked")
    public final <T extends DataSource> T convertTo(final DataSourceType type) {
        if (type == DataSourceType.FileSystem) {
            return (T) convertTo(FileSystemDataSource.class);
        } else if (type == DataSourceType.FilePairSystem) {
            return (T) convertTo(FilePairSystemDataSource.class);
        } else if (type == DataSourceType.FileTripletSystem) {
            return (T) convertTo(FileTripletSystemDataSource.class);
        } else if (type == DataSourceType.WebService) {
            return (T) convertTo(WebServiceDataSource.class);
        } else if (type == DataSourceType.Database) {
            return (T) convertTo(DatabaseDataSource.class);
        } else if (type == DataSourceType.MessageBroker) {
            return (T) convertTo(MessageBrokerDataSource.class);
        } else {
            return null;
        }
    }

    @Override
    public final String toString() {
        return String.format("%s [%s] -> %s (%s)", this.id, getType(), this.uri, this.monitor);
    }

    /**
     * Gets a value from the DataSource options with the given information.
     * These include the key to find in the option String and the class of the
     * value to be returned.
     *
     * @param <T>   the Class of the value to be returned from the DataSource
     *              options.
     * @param value information about the value to be retrieved, containing it's
     *              key and the Class of the value to be returned.
     * @return a value from the DataSource options or <code>null</code> if no
     * value was found for the given key/type information.
     */
    public final <T extends Serializable> T getOptionValue(final IDataSourceOptionValue value) {
        return parseOptionValue(getOptionValue(value.getKeyName(), false), value.getValueType());
    }

    /**
     * Gets a value from the DataSource options with the given information.
     * These include the key to find in the option String and the class of the
     * value to be returned.
     *
     * @param <T>       the Class of the value to be returned from the DataSource
     *                  options.
     * @param value     information about the value to be retrieved, containing it's
     *                  key and the Class of the value to be returned.
     * @param urlDecode whether to try to tecode the given value via an URLDecoder.
     * @return a value from the DataSource options or <code>null</code> if no
     * value was found for the given key/type information.
     */
    public final <T extends Serializable> T getOptionValue(final IDataSourceOptionValue value, final boolean urlDecode) {
        return parseOptionValue(getOptionValue(value.getKeyName(), urlDecode), value.getValueType());
    }

    /**
     * Extracts the corresponding value for the given key from the DataSources
     * option String.
     * <p>
     * This will perform a lookup for the given key in the option String. For
     * this purpose the option string will be tokenized by the
     * <code>&amp;</code> delimiter. Each token then will be seperated by
     * <code>=</code> into key and value. If the given key is found, it's
     * corresponding value will be returned as a String.
     *
     * @param key The key to look for in the option string.
     * @return the value for the given key or <code>null</code> if no such key
     * or a correspondig value exists.
     */
    private String getOptionValue(final String key, final boolean decode) {
        final String value = getOptionsMap().get(key);
        return value == null ? null : decode ? URLDecoder.decode(value, StandardCharsets.UTF_8) : value;
    }

    /**
     * Converts the given String value into the given Class value.
     *
     * @param <T>   the type of Class to convert the value into.
     * @param value the value to be converted.
     * @param clazz the Class to convert the value into.
     * @return the String given converted into the specified Class.
     */
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T parseOptionValue(final String value, final Class<? extends Serializable> clazz) {
        if (value == null) {
            return null;
        }
        if (clazz == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (clazz == Float.class) {
            return (T) Float.valueOf(value);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(value);
        } else if (clazz == String.class) {
            return (T) value;
        } else if ((clazz == Character.class) && (value.length() > 0)) {
            return (T) Character.valueOf(value.charAt(0));
        }
        logger.warn("Could not cast '" + value + "' to class " + clazz);
        return null;
    }

    private Map<String, String> optionsToMap() {
        return Arrays.stream(options.split("&"))
            .map(option -> {
                final String[] parts = option.split("=");
                return parts.length == 2 ? Pair.of(parts[0], parts[1]) : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private String optionsMapToString(final Map<String, String> optionsMap) {
        return optionsMap.entrySet().stream()
            .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("&"));
    }
}
