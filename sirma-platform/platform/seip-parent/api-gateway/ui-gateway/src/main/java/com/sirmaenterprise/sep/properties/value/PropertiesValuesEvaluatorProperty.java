package com.sirmaenterprise.sep.properties.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of single requested property.
 *
 * @author Boyan Tonchev.
 */
public class PropertiesValuesEvaluatorProperty {

    /**
     * Pattern to separate property name by dot.
     */
    private static final Pattern DOT_SEPARATOR_PATTERN = Pattern.compile(".*\\.(.*)");

    /**
     * Property name of instance which property have to be extracted.
     */
    private String instancePropertyName;

    /**
     * Property name of new instance definition.
     */
    private String newInstancePropertyName;

    /**
     * Name which will be used when extracted property value is returned.
     * This name format can be:
     * 1. emf:createdBy.email
     * emf:createdBy is name from new created instance definition (it is needed in ui)
     * email is real property name which have t obe extract.
     * or
     * 2. email
     */
    private String returnInstancePropertyName;

    /**
     * Create object of ExpressionTemplateProperty.
     *
     * @param instancePropertyName
     *         - the property name contains information which property have to be processed.
     * @param newInstancePropertyName
     *         - Property name of new instance definition.
     */
    public PropertiesValuesEvaluatorProperty(String instancePropertyName, String newInstancePropertyName) {
        returnInstancePropertyName = instancePropertyName;
        this.instancePropertyName = extractSourceName();
        this.newInstancePropertyName = newInstancePropertyName;
    }

    /**
     * Fetch instance property name which have to be processed.
     *
     * @return - the property name.
     */
    public String getInstancePropertyName() {
        return instancePropertyName;
    }

    /**
     * Fetch property name of new instance definition.
     *
     * @return the property name of new instance definition.
     */
    public String getNewInstancePropertyName() {
        return newInstancePropertyName;
    }

    /**
     * Name to be used when extracted property value is returned.
     *
     * @return name of property used in response.
     */
    public String getReturnInstancePropertyName() {
        return returnInstancePropertyName;
    }

    /**
     * Extract real property name which have to be processed.
     *
     * @return - the real instance property name which have to be processed.
     */
    private String extractSourceName() {
        Matcher dotSeparatorMatcher = DOT_SEPARATOR_PATTERN.matcher(returnInstancePropertyName);
        return dotSeparatorMatcher.find() ? dotSeparatorMatcher.group(1) : returnInstancePropertyName;
    }
}