package com.itranswarp.summer.context.io;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static net.liyze.basin.core.Basin.envMap;
/*
Edited
YAML to TOML
Used Basin's settings.
*/

public class PropertyResolver {

    Logger logger = LoggerFactory.getLogger(getClass());

    Map<String, Object> properties = new HashMap<>();

    public PropertyResolver() {
        this.properties.putAll(System.getenv());
        this.properties.putAll(envMap);
        if (logger.isDebugEnabled()) {
            List<String> keys = new ArrayList<>(this.properties.keySet());
            Collections.sort(keys);
            if (logger.isDebugEnabled())
                for (String key : keys) {
                    logger.debug("PropertyResolver: {} = {}", key, this.properties.get(key));
                }
        }
    }

    public boolean containsProperty(String key) {
        return this.properties.containsKey(key);
    }

    @Nullable
    public String getPropertyAsString(String key) {
        PropertyExpr keyExpr = parsePropertyExpr(key);
        if (keyExpr != null) {
            if (keyExpr.defaultValue() != null) {
                return getProperty(keyExpr.key(), keyExpr.defaultValue());
            } else {
                return getRequiredProperty(keyExpr.key());
            }
        }
        Object value = this.properties.get(key);
        if (value != null) {
            return parseValue((String) value);
        }
        return null;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? parseValue(defaultValue) : value;
    }

    @Nullable
    public <T> T getProperty(String key) {
        String value = getPropertyAsString(key);
        if (value == null) {
            return null;
        }
        return convert(value);
    }

    public <T> T getProperty(String key, T defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return convert(value);
    }

    public <T> T getRequiredProperty(String key) {
        T value = getProperty(key);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }

    @SuppressWarnings("unchecked")
    <T> T convert(Object value) {
        return (T) value;
    }

    String parseValue(String value) {
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null) {
            return value;
        }
        if (expr.defaultValue() != null) {
            return getProperty(expr.key(), expr.defaultValue());
        } else {
            return getRequiredProperty(expr.key());
        }
    }

    PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int n = key.indexOf(':');
            if (n == (-1)) {
                // no default value: ${key}
                String k = notEmpty(key.substring(2, key.length() - 1));
                return new PropertyExpr(k, null);
            } else {
                // has default value: ${key:default}
                String k = notEmpty(key.substring(2, n));
                return new PropertyExpr(k, key.substring(n + 1, key.length() - 1));
            }
        }
        return null;
    }

    String notEmpty(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return key;
    }
}

record PropertyExpr(String key, String defaultValue) {
}
