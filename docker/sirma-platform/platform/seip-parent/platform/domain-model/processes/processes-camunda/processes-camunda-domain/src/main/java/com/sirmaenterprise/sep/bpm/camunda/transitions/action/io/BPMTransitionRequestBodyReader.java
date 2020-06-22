package com.sirmaenterprise.sep.bpm.camunda.transitions.action.io;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.transitions.action.BPMTransitionRequest;

/**
 * Message body reader that produces {@link BPMTransitionRequest} assignable instances.
 *
 * @author bbanchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class BPMTransitionRequestBodyReader implements MessageBodyReader<BPMTransitionRequest> {

	@BeanParam
	private RequestInfo request;
	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return BPMTransitionRequest.class.isAssignableFrom(type);
	}

	@Override
	public BPMTransitionRequest readFrom(Class<BPMTransitionRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		try (JsonReader reader = Json.createReader(entityStream)) {
			JsonObject transitionRequestJson = reader.readObject();
			if (transitionRequestJson.isEmpty()) {
				throw new CamundaIntegrationRuntimeException("Provided invalid bpmTransition model!");
			}
			return buildTransitionRequest(transitionRequestJson, type);
		}
	}

	protected BPMTransitionRequest buildTransitionRequest(JsonObject sourceData, Class<BPMTransitionRequest> type)
			throws CamundaIntegrationRuntimeException {
		String userOperation = sourceData.getString(JsonKeys.USER_OPERATION, null);
		String targetId = PATH_ID.get(request);

		BPMTransitionRequest actionRequest;
		try {
			actionRequest = type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new CamundaIntegrationRuntimeException(e);
		}
		actionRequest.setUserOperation(userOperation);
		actionRequest.setTargetId(targetId);

		JsonArray targetInstances = sourceData.getJsonArray("targetInstances");

		Map<String, Instance> transitionData = new LinkedHashMap<>(targetInstances.size() + 1, 1f);
		// now iterate the transition data and fill the result map
		for (JsonValue entry : targetInstances) {
			JsonObject activityData = (JsonObject) entry;
			String instanceId = activityData.getString("instanceId");
			if (targetId.equals(instanceId)) {
				// convert with target id set to load the instance
				transitionData.put(instanceId, instanceResourceParser.toInstance(activityData, targetId));
				continue;
			}
			Instance instance = instanceResourceParser.toInstance(activityData, null);
			// set the instance id back
			instance.setId(instanceId);
			transitionData.put(instanceId, instance);
		}
		transitionData.computeIfAbsent(targetId,
				instance -> instanceResourceParser.toInstance(Json.createObjectBuilder().build(), instance));
		actionRequest.setTransitionData(transitionData);
		return actionRequest;
	}
}
