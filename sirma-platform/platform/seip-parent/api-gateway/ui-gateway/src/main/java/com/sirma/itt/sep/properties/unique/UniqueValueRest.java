package com.sirma.itt.sep.properties.unique;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;

/**
 * Rest service used to handle the definition field that should have unique value.
 *
 * @author Boyan Tonchev.
 */
@Transactional
@Path("/properties")
@ApplicationScoped
public class UniqueValueRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String JSON_KEY_UNIQUE = "unique";

    @Inject
    private UniqueValueValidationService uniqueValueValidationService;

    @Inject
    private DefinitionService definitionService;

    @Inject
    private TypeConverter typeConverter;

    /**
     * Checks if a value is unique for a certain tenant.
     *
     * @param uniqueValueRequest
     *         - {@link UniqueValueRequest} hold information propertyName, definitionId, instanceId and value.
     * @return map with key -> "unique" and value result of uniqueness check.
     */
    @POST
    @Path("/unique")
    public Map<String, Boolean> checksValueForUniqueness(UniqueValueRequest uniqueValueRequest) {
        String propertyName = uniqueValueRequest.getPropertyName();
        String instanceId = uniqueValueRequest.getInstanceId();
        String definitionId = uniqueValueRequest.getDefinitionId();
        Object value = uniqueValueRequest.getValue();
        if (value == null) {
            return buildResponseUnique();
        }
        DefinitionModel instanceDefinition = definitionService.find(definitionId);
        Optional<PropertyDefinition> propertyDefinition = instanceDefinition.getField(propertyName)
				.filter(PropertyDefinition.isUniqueProperty());
        if (propertyDefinition.isPresent()) {
            return buildResponse(checksValueForUniqueness(instanceId, propertyDefinition.get(), definitionId, value));
        }
        return buildResponseUnique();
    }

    /**
     * Checks if a value is unique for a certain tenant.
     *
     * Result will be true if value is not already registered or is registered for requested <code>instanceId</code>.
     * Result will be false if value is registered for other instance id than requested <code>instanceId</code>.
     *
     * @param instanceId
     *         - the requested instance id.
     * @param propertyDefinition
     *         - the property definition where unique property is defined.
     * @param definitionId
     *         - the definition id where <code>propertyDefinition</code> is defined.
     * @param value
     *         - the value which will be check for uniqueness.
     * @return true if value is not already registered or is registered for requested instance id.
     */
    private boolean checksValueForUniqueness(String instanceId, PropertyDefinition propertyDefinition,
            String definitionId, Object value) {
        Object convertedValue = convertToInternalTypeOrNull(propertyDefinition, value);
        return !uniqueValueValidationService.hasRegisteredValueForAnotherInstance(instanceId, definitionId,
                                                                                  propertyDefinition, convertedValue);
    }

    /**
     * Fetches java class from <code>propertyDefinition</code> and try to convert requested <code>value</code>.
     *
     * @param propertyDefinition
     *         - the property definition where unique property is defined.
     * @param value
     *         - the requested value.
     * @return converted value or null if can not find converter or exception occurred during conversion.
     */
    private Object convertToInternalTypeOrNull(PropertyDefinition propertyDefinition, Object value) {
        DataTypeDefinition dataType = propertyDefinition.getDataType();
        Class<?> javaClass = dataType.getJavaClass();
        try {
            return typeConverter.convert(javaClass, value);
        } catch (Exception e) {
            LOGGER.debug("Value: '" + value + "' can't be checked for uniqueness!", e);
        }
        return null;
    }

    private static Map<String, Boolean> buildResponseUnique() {
        return buildResponse(true);
    }

    private static Map<String, Boolean> buildResponse(boolean unique) {
        Map<String, Boolean> response = new HashMap<>();
        response.put(JSON_KEY_UNIQUE, unique);
        return response;
    }
}
