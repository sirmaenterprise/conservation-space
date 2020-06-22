package com.sirma.itt.seip.instance.actions.relations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Message body reader that produces {@link UpdateRelationsRequest} instances.
 * Example of json:
 * <pre>
 *         <code>
 *             {
 *               "id": "emf:0001",
 *               "add": [{
 *                        "linkId": "emf:hasAttachment",
 *                        "ids": ["emf:0002", "emf:0003"]
 *                       }, {
 *                         "linkId": "emf:hasWatchers",
 *                         "ids": ["emf:0002", "emf:0003"]
 *                      }],
 *                "remove": [{
 *                            "linkId": "emf:hasAttachment",
 *                            "ids": ["emf:0004", "emf:0005"]
 *                           }, {
 *                            "linkId": "emf:hasWatchers",
 *                            "ids": ["emf:0004", "emf:0005"]
 *                          }]
 *              }
 *         </code>
 *     </pre>
 *
 * @author Boyan Tonchev.
 */
@Provider
@Consumes(Versions.V2_JSON)
public class UpdateRelationsBodyReader implements MessageBodyReader<UpdateRelationsRequest> {

    private static final String JSON_KEY_ID = "id";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return UpdateRelationsRequest.class.isAssignableFrom(type);
    }

    @Override
    public UpdateRelationsRequest readFrom(Class<UpdateRelationsRequest> type, Type genericType,
            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {
        return JSON.readObject(entityStream, UpdateRelationsBodyReader::readRequest);
    }

    private static UpdateRelationsRequest readRequest(JsonObject json) {
        if (json.isEmpty() || !json.containsKey(JSON_KEY_ID)) {
            throw new BadRequestException("The JSON object is empty or id of instance is missing!");
        }

        UpdateRelationsRequest updateRelationsRequest = new UpdateRelationsRequest(json.getString(JSON_KEY_ID));
        updateRelationsRequest.setLinksToBeAdded(fetchUpdateRelationData(json, "add"));
        updateRelationsRequest.setLinksToBeRemoved(fetchUpdateRelationData(json, "remove"));
        updateRelationsRequest.setUserOperation(json.getString(JsonKeys.USER_OPERATION));
        return updateRelationsRequest;
    }

    private static Set<UpdateRelationData> fetchUpdateRelationData(JsonObject json, String relationOperation) {
        if (!json.containsKey(relationOperation)) {
            return Collections.emptySet();
        }

        JsonArray relations = json.getJsonArray(relationOperation);
        Set<UpdateRelationData> result = new HashSet<>(relations.size());
        for (int index = 0; index < relations.size(); index++) {
            JsonObject relation = relations.getJsonObject(index);
            String linkId = relation.getString("linkId", null);
            if (linkId != null && json.containsKey(relationOperation)) {
                result.add(new UpdateRelationData(linkId, convert(relation.getJsonArray("ids"))));
            }
        }
        return result;
    }

    private static Set<String> convert(JsonArray json) {
        Set<String> result = new HashSet<>(json.size());
        for (int index = 0; index < json.size(); index++) {
            result.add(json.getString(index));
        }
        return result;
    }
}
