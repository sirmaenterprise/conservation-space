package com.sirma.itt.seip.instance.actions.relations;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Message body reader that produces {@link AddRelationRequest} instances
 *
 * @author BBonev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class RemoveRelationBodyReader implements MessageBodyReader<RemoveRelationRequest> {
	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return RemoveRelationRequest.class.isAssignableFrom(type);
	}

	@Override
	public RemoveRelationRequest readFrom(Class<RemoveRelationRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {
		return JSON.readObject(entityStream, toRemoveRelationRequest());
	}

	private Function<JsonObject, RemoveRelationRequest> toRemoveRelationRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			return buildActionRequest(json, request);
		};
	}

	private static RemoveRelationRequest buildActionRequest(JsonObject json, RequestInfo request) {
		RemoveRelationRequest actionRequest = new RemoveRelationRequest();
		String userOperation = json.getString(JsonKeys.USER_OPERATION, ActionTypeConstants.DELETE);
		String targetId = PATH_ID.get(request);

		actionRequest.setUserOperation(userOperation);
		actionRequest.setTargetId(targetId);
		actionRequest.setContextPath(JSON.getStringArray(json, JsonKeys.CONTEXT_PATH));

		JsonObject relations = json.getJsonObject(JsonKeys.INSTANCE_RELATIONS);
		Map<String, Set<String>> relationsMapping = createHashMap(relations.size());
		for (String relationId : relations.keySet()) {
			List<String> ids = JSON.getStringArray(relations, relationId);
			relationsMapping.put(relationId, new HashSet<>(ids));
		}
		actionRequest.setRelations(relationsMapping);
		return actionRequest;
	}
}
