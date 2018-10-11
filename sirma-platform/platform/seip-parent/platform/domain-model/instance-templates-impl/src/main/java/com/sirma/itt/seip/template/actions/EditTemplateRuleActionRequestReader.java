package com.sirma.itt.seip.template.actions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Message body reader for {@link EditTemplateRuleActionRequest}
 *
 * @author Viliar Tsonev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class EditTemplateRuleActionRequestReader implements MessageBodyReader<EditTemplateRuleActionRequest> {

	@BeanParam
	private RequestInfo info;

	private static final String TEMPLATE_RULE_KEY = "rule";

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EditTemplateRuleActionRequest.class.equals(type);
	}

	@Override
	public EditTemplateRuleActionRequest readFrom(Class<EditTemplateRuleActionRequest> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, buildReqest());
	}

	private Function<JsonObject, EditTemplateRuleActionRequest> buildReqest() {
		return json -> {
			EditTemplateRuleActionRequest request = new EditTemplateRuleActionRequest();
			String instanceId = RequestParams.PATH_ID.get(info);
			if (StringUtils.isBlank(instanceId)) {
				throw new BadRequestException("Target instance ID is required when editing a template rule");
			}

			request.setTargetId(instanceId);
			request.setUserOperation(
					json.getString(JsonKeys.USER_OPERATION, EditTemplateRuleActionRequest.OPERATION_NAME));
			if (json.isNull(TEMPLATE_RULE_KEY)) {
				request.setRule(null);
			} else {
				request.setRule(json.getString(TEMPLATE_RULE_KEY));
			}
			return request;
		};
	}

}
