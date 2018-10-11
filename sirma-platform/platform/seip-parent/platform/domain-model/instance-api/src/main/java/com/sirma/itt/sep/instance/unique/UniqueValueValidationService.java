package com.sirma.itt.sep.instance.unique;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Generic interface for unique property functionality. Defines base methods for it.
 *
 * @author Boyan Tonchev.
 */
public interface UniqueValueValidationService {

    /**
     * Fetches property value of instance with {@link PropertyDefinition} and check if there is registered such value
     * for other instance.
     *
     * @param instance
     *         - instance which property value have to be checked.
     * @param propertyDefinition
     *         - the propertyDefinition of property value which have to be checked.
     * @return true if value of property is already registered for other instance.
     */
    boolean hasRegisteredValueForAnotherInstance(Instance instance, PropertyDefinition propertyDefinition);

    /**
     * Checks if there is registered property <code>value</code> for other instanceId than <code>instanceId</code>.
     *
     * @param instanceId
     *         - the instance id.
     * @param definitionId
     *         - definition id where property is defined.
     * @param propertyDefinition
     *         - the propertyDefinition of property.
     * @param value
     *         - the value which have to be checked.
     * @return true if <code>value</code> of property is already registered for other instanceId.
     */
    boolean hasRegisteredValueForAnotherInstance(String instanceId, String definitionId,
            PropertyDefinition propertyDefinition, Object value);

    /**
     * Extracts the value of property with <code>propertyDefinition</code> from <code>instance</code> and register it
     * as unique.
     *
     * @param instance
     *         - the instance.
     * @param propertyDefinition
     *         - the {@link PropertyDefinition} of property which value have to be registered.
     */
    void registerUniqueValue(Instance instance, PropertyDefinition propertyDefinition);

    /**
     * Fetches all registered unique values for <code>instance</code> and unregister them.
     *
     * @param instanceId
     *         - the instance id of instance which unique property values have to be unregistered.
     */
    void unRegisterUniqueValues(String instanceId);

    /**
     * Registers all unique values of <code>instance</code>
     *
     * @param instance
     *         - the instance.
     */
    void registerUniqueValues(Instance instance);

    /**
     * Fetches all defined unique fields from all definitions. Register newly added and unregister those who are no longer.
     */
    void updateUniqueFields();

    /**
     * Fetches all {@link PropertyDefinition} defined as unique from <code>definitionModels</code>.
     *
     * @param definitionModels
     *         - the definitionModels from which unique fields to be fetched.
     * @return the collection of fetched {@link PropertyDefinition} defined as unique.
     */
    Collection<PropertyDefinition> extractUniquePropertyDefinitions(Stream<DefinitionModel> definitionModels);

    /**
     * Registers unique values of instances created before some field to be marked as unique.
     *
     * @param definitionId
     *         - the definition id where field is defined.
     * @param fieldUri
     *         - the field uri of filed.
     */
    void registerOldUniqueValues(String definitionId, String fieldUri);
}
