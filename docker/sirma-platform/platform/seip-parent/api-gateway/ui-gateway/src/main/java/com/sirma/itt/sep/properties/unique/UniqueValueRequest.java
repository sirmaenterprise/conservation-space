package com.sirma.itt.sep.properties.unique;

/**
 * Model for {@link UniqueValueRest}.
 *
 * @author Boyan Tonchev.
 */
public class UniqueValueRequest {

    private String instanceId;
    private String definitionId;
    private String propertyName;
    private Object value;

    /**
     * Getter of instance id for which check for uniqueness have to be evaluated.
     *
     * @return the instance id.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Setter Getter of instance id for which check for uniqueness have to be evaluated.
     *
     * @param instanceId
     *         - the instance.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Getter of property name which value have to be checked for uniqueness.
     *
     * @return the property name.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Setter of property name which value have to be checked for uniqueness.
     *
     * @param propertyName
     *         - the property name.
     */
    void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Getter of value which have to be checked for uniqueness.
     *
     * @return the value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Setter of value which have to be checked for uniqueness.
     *
     * @param value
     *         -the value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Getter of definition id where checked property is defined.
     *
     * @return the definition id.
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * Setter of definition id where checked property is defined.
     *
     * @param definitionId
     *         - the definition id.
     */
    void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }
}
