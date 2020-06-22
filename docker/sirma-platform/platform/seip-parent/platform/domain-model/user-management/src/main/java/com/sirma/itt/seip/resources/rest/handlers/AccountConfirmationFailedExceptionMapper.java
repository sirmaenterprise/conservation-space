package com.sirma.itt.seip.resources.rest.handlers;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.resources.security.AccountConfirmationFailedException;
import com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Exception mapper for {@link AccountConfirmationFailedException}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/08/2017
 */
@Provider
public class AccountConfirmationFailedExceptionMapper implements ExceptionMapper<AccountConfirmationFailedException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private LabelProvider labelProvider;

	@Override
	public Response toResponse(AccountConfirmationFailedException exception) {
		LOGGER.warn("Account confirmation process for {} failed with {}", exception.getUserAccount(), exception.getFailType());
		LOGGER.trace("", exception);

		JsonObjectBuilder result = Json.createObjectBuilder();

		if (isExpiredOrInvalid(exception.getFailType())) {
			result.add("expired", true);
		}
		result.add("message", labelProvider.getValue(exception.getFailType().getLabelId()));

		ExceptionMapperUtil.appendExceptionMessages(result, exception);

		Response.Status status = getStatus(exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(status, result.build());
	}

	private static boolean isExpiredOrInvalid(ConfirmationFailType failType) {
		return failType.equals(ConfirmationFailType.INVALID_OR_EXPIRED_CONFIRMATION_CODE)
				|| failType.equals(ConfirmationFailType.INVALID_CONFIRMATION_CODE);
	}

	private static Response.Status getStatus(AccountConfirmationFailedException exception) {
		switch (exception.getFailType()) {
			case USER_DOES_NOT_EXIST:
			case INVALID_USERNAME:
				return Response.Status.NOT_FOUND;
			case UNKNOWN:
				return Response.Status.INTERNAL_SERVER_ERROR;
			case ACCOUNT_LOCKED:
				return Response.Status.UNAUTHORIZED;
			default:
				return Response.Status.BAD_REQUEST;
		}
	}
}
