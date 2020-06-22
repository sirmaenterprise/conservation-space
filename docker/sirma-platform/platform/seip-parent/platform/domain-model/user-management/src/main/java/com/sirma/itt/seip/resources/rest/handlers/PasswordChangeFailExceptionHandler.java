package com.sirma.itt.seip.resources.rest.handlers;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException.PasswordFailType;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Exception mapper for {@link PasswordChangeFailException}. Thrown when password does not pass validation.
 *
 * @author smustafov
 */
@Provider
public class PasswordChangeFailExceptionHandler implements ExceptionMapper<PasswordChangeFailException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String VALIDATION_MESSAGE_KEY = "passwordValidationMessage";
	private static final String WRONG_PASSWORD_MESSAGE_KEY = "passwordWrongMessage"; //NOSONAR
	private static final String RESPONSE_MESSAGES_KEY = "messages";

	@Inject
	private LabelProvider labelProvider;

	@Override
	public Response toResponse(PasswordChangeFailException exception) {
		LOGGER.error(exception.getMessage(), exception);

		JsonObjectBuilder result = Json.createObjectBuilder();
		JsonObjectBuilder builder = Json.createObjectBuilder();

		PasswordFailType type = exception.getType();
		String labelValue = labelProvider.getValue(type.getLabelKey());
		String policyValue = exception.getPolicyValue();
		if (StringUtils.isNotBlank(policyValue)) {
			labelValue = MessageFormat.format(labelValue, exception.getPolicyValue());
		}

		if (type.equals(PasswordFailType.WRONG_OLD_PASSWORD)) {
			result.add(RESPONSE_MESSAGES_KEY, builder.add(WRONG_PASSWORD_MESSAGE_KEY, labelValue));
		} else {
			result.add(RESPONSE_MESSAGES_KEY, builder.add(VALIDATION_MESSAGE_KEY, labelValue));
		}

		ExceptionMapperUtil.appendExceptionMessages(result, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.BAD_REQUEST, result.build());
	}

}
