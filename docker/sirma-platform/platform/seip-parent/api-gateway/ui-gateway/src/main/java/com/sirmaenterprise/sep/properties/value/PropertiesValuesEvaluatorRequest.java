package com.sirmaenterprise.sep.properties.value;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Model for expression template request.
 *
 * @author Boyan Tonchev.
 */
public class PropertiesValuesEvaluatorRequest {

    /**
     * Definition id which will be applied to new instance.
     */
    private String newInstanceDefinitionId;

    /**
     * Contains information about all requested properties.
     * Key is instance id from which property values have to be extracted.
     * Value is list with properties which have to be extracted..
     */
    private Map<String, List<PropertiesValuesEvaluatorProperty>> expressionTemplateModel = new LinkedHashMap<>();

    /**
     * Getter method for efinition id which will be applied to new instance.
     *
     * @return definition id which will be applied to new instance.
     */
    public String getNewInstanceDefinitionId() {
        return newInstanceDefinitionId;
    }

    /**
     * Set definition id which will be applied to new instance.
     *
     * @param newInstanceDefinitionId
     *         - the definition id of new instance.
     */
    public void setNewInstanceDefinitionId(String newInstanceDefinitionId) {
        this.newInstanceDefinitionId = newInstanceDefinitionId;
    }

    /**
     * Add information about requested property.
     *
     * @param instanceId-
     *         instance which property have to be processed.
     * @param instancePropertyName-
     *         the property name contains information which property have to be processed.
     * @param newInstancePropertyName
     *         - property name of new instance definition.
     */
    public void addObjectProperty(String instanceId, String instancePropertyName, String newInstancePropertyName) {
        expressionTemplateModel.computeIfAbsent(instanceId, v -> new LinkedList<>())
                .add(new PropertiesValuesEvaluatorProperty(instancePropertyName, newInstancePropertyName));
    }

    /**
     * Fetch all instances ids which have to be processed.
     *
     * @return list with instances ids which have to be processed.
     */
    public Set<String> getInstancesIds() {
        return expressionTemplateModel.keySet();
    }

    /**
     * Return stream with all instances ids and it's properties which have to be extracted.
     *
     * @return the stream.
     */
    public Stream<Map.Entry<String, List<PropertiesValuesEvaluatorProperty>>> getExpressionTemplateModelAsStream() {
        return expressionTemplateModel.entrySet().stream();
    }
}