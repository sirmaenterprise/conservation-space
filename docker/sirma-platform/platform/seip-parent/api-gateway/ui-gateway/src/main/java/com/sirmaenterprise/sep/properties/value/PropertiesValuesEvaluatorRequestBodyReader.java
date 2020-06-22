package com.sirmaenterprise.sep.properties.value;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Producer for ExpressionTemplateRequest.
 * Consumes json and convert it to {@link PropertiesValuesEvaluatorRequest}
 * Consumed json is for example:
 * <pre>
 *     {
 *         "definitionId": "id",
 *         "bindings": [{
 *             "id": "selected-instance-id",
 *             "target": "emf:createdBy.emf:email",
 *             "source": "sourceFieldName"
 *         }, {
 *             ...
 *         }]
 *     }
 * </pre>
 *
 * definitionId - definition id which will be applied to new instance.
 * id - is id of instance from where property will be evaluated.
 * target - expression for definition. It is comma separated value. First part is field name of definition of new created instance from where
 *          id have to be extract. Second part is real property name which have to be evaluated.
 * source - is field name of definition of new created instance where evaluated property have to be used.
 *
 * @author Boyan Tonchev.
 */
@Provider
@Consumes(Versions.V2_JSON)
public class PropertiesValuesEvaluatorRequestBodyReader implements MessageBodyReader<PropertiesValuesEvaluatorRequest> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return PropertiesValuesEvaluatorRequest.class.isAssignableFrom(type);
    }

    @Override
    public PropertiesValuesEvaluatorRequest readFrom(Class<PropertiesValuesEvaluatorRequest> type, Type genericType,
            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException {
        return JSON.readObject(entityStream, PropertiesValuesEvaluatorRequestBodyReader::readRequest);
    }

    private static PropertiesValuesEvaluatorRequest readRequest(JsonObject json) {
        if (json.isEmpty()) {
            throw new BadRequestException("An error occurred. Please contact your system administrator!");
        }

        PropertiesValuesEvaluatorRequest propertiesValuesEvaluatorRequest = new PropertiesValuesEvaluatorRequest();
        propertiesValuesEvaluatorRequest.setNewInstanceDefinitionId(json.getString(JsonKeys.DEFINITION_ID));

        json.getJsonArray(JsonKeys.BINDINGS).forEach(binding -> {
            JsonObject bindingObject = (JsonObject) binding;
            propertiesValuesEvaluatorRequest.addObjectProperty(bindingObject.getString(JsonKeys.ID), bindingObject.getString(
                    JsonKeys.TARGET), bindingObject.getString(JsonKeys.SOURCE));
        });
        return propertiesValuesEvaluatorRequest;
    }
}
