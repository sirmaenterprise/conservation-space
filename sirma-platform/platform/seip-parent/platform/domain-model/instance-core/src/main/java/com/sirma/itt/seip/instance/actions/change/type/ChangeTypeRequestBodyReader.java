package com.sirma.itt.seip.instance.actions.change.type;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Rest request reader for {@link ChangeTypeRequest} types.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/02/2019
 */
@Provider
@Consumes(Versions.V2_JSON)
public class ChangeTypeRequestBodyReader implements MessageBodyReader<ChangeTypeRequest> {

	@BeanParam
	private RequestInfo request;

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Inject
	private InstanceTypeMigrationCoordinator migrationCoordinator;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ChangeTypeRequest.class.equals(type);
	}

	@Override
	public ChangeTypeRequest readFrom(Class<ChangeTypeRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
			IOException {
		return JSON.readObject(entityStream, toActionRequest());
	}

	private Function<JsonObject, ChangeTypeRequest> toActionRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}
			if (!json.containsKey(JsonKeys.DEFINITION_ID)) {
				throw new BadRequestException(
						"The request should contain the target type under property '" + JsonKeys.DEFINITION_ID + "'");
			}

			String userOperation = json.getString(JsonKeys.USER_OPERATION, ActionTypeConstants.CHANGE_TYPE);
			String asType = json.getString(JsonKeys.DEFINITION_ID);
			// In the url is stored the existing instance id, no the cloned one.
			String targetId = PATH_ID.get(request);

			Instance convertedInstance;
			try {
				convertedInstance = migrationCoordinator.getInstanceAs(targetId, asType);
			} catch (IllegalArgumentException e) {
				throw new BadRequestException(e.getMessage());
			}
			if (json.containsKey(JsonKeys.PARENT_ID)) {
				if (json.isNull(JsonKeys.PARENT_ID) || json.getString(JsonKeys.PARENT_ID).equals("null")) {
					convertedInstance.remove(InstanceContextService.HAS_PARENT, fieldConverter);
				} else {
					convertedInstance.add(InstanceContextService.HAS_PARENT, json.getString(JsonKeys.PARENT_ID),
							fieldConverter);
				}
			}

			Instance instanceWithChanges = instanceResourceParser.readInstanceData(json, convertedInstance);

			ChangeTypeRequest changeTypeRequest = new ChangeTypeRequest();
			changeTypeRequest.setTargetId(targetId);
			changeTypeRequest.setTargetReference(instanceWithChanges.toReference());
			changeTypeRequest.setInstance(instanceWithChanges);
			changeTypeRequest.setUserOperation(userOperation);

			return changeTypeRequest;
		};
	}
}
