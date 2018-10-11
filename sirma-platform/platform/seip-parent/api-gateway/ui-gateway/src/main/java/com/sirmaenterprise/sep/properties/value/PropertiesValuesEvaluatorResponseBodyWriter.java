package com.sirmaenterprise.sep.properties.value;

import com.github.jsonldjava.utils.JsonUtils;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;

/**
 * Producer of json response.
 * Convert {@link PropertiesValuesEvaluatorResponse} to json.
 * Converted json for example is:
 * <pre>
 *     {
 *         "data": [{
 *             "id": "selected-instance-id",
 *             "properties": [{
 *                 "propertyName": "emf:createdBy.emf:email",
 *                 "propertyValue": "admin@sep.com"
 *             }, {
 *                 ...
 *             }]
 *         }]
 *     }
 * </pre>
 *
 * @author Boyan Tonchev.
 */
@Provider
@Produces(Versions.V2_JSON)
public class PropertiesValuesEvaluatorResponseBodyWriter extends AbstractMessageBodyWriter<PropertiesValuesEvaluatorResponse> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return PropertiesValuesEvaluatorResponse.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(PropertiesValuesEvaluatorResponse expressionTemplateResponse, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try (Writer output = new OutputStreamWriter(entityStream)) {
            JsonUtils.write(output, Collections.singletonMap("data", expressionTemplateResponse.getInstancesData()));
        }
    }
}
