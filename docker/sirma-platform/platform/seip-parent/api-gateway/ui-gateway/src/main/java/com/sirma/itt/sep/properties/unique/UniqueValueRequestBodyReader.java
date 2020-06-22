package com.sirma.itt.sep.properties.unique;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Producer for UniqueFieldRequest.
 * Consumes json and convert it to {@link UniqueValueRequest}
 * Consumed json is for example:
 * <pre>
 *     {
 *         "instanceId": "emf:0001",
 *         "definitionId": "DT0007",
 *         "propertyName": "uniqueDate",
 *         "value": "2017-09-27T21:00:00.000Z"
 *     }
 * </pre>
 * <br>
 * <b>instanceId</b> -  instance id for which check for uniqueness have to be evaluated.<br>
 * <b>definitionId</b> - definition id where checked property is defined.
 * <b>propertyName</b> - property name which value have to be checked for uniqueness.<br>
 * <b>value</b> - value which will be checked for uniqueness.
 *
 * @author Boyan Tonchev.
 */
@Provider
@Consumes(Versions.V2_JSON)
public class UniqueValueRequestBodyReader implements MessageBodyReader<UniqueValueRequest> {

    private static final String JSON_KEY_INSTANCE_ID = "instanceId";
    private static final String JSON_KEY_DEFINITION_ID = "definitionId";
    private static final String JSON_KEY_PROPERTY_NAME = "propertyName";
    private static final String JSON_KEY_VALUE = "value";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return UniqueValueRequest.class.isAssignableFrom(type);
    }

    @Override
    public UniqueValueRequest readFrom(Class<UniqueValueRequest> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        return JSON.readObject(entityStream, UniqueValueRequestBodyReader::readRequest);
    }

    private static UniqueValueRequest readRequest(JsonObject json) {
        if (json.isEmpty()) {
            throw new BadRequestException("The request have empty body!");
        }

        if (!json.containsKey(JSON_KEY_DEFINITION_ID) || !json.containsKey(JSON_KEY_PROPERTY_NAME)) {
            throw new BadRequestException("Missing required parameters!");
        }

        UniqueValueRequest uniqueValueRequest = new UniqueValueRequest();
        uniqueValueRequest.setInstanceId(json.getString(JSON_KEY_INSTANCE_ID, null));
        uniqueValueRequest.setPropertyName(json.getString(JSON_KEY_PROPERTY_NAME));
        uniqueValueRequest.setDefinitionId(json.getString(JSON_KEY_DEFINITION_ID));
        uniqueValueRequest.setValue(getValue(json));
        return uniqueValueRequest;
    }

    /**
     * Fetches value from <code>json</code>. If value missing or is empty null will be returned.
     *
     * @param json
     *         - json from where value have to be extracted.
     * @return the value or null if value missing or is empty.
     */
    private static Object getValue(JsonObject json) {
        String value = json.getString(JSON_KEY_VALUE, null);
        return StringUtils.isNotBlank(value) ? value : null;
    }
}
