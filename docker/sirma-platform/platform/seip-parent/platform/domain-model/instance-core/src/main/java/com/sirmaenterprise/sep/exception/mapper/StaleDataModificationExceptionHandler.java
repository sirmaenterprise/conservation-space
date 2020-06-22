package com.sirmaenterprise.sep.exception.mapper;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.exceptions.StaleDataModificationException;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Exception mapper for {@link StaleDataModificationException}. This exception occurs when a newer version of the
 * document/object has already been persisted and the user is trying to overwrite with an older one.
 *
 * @author yasko
 * @author BBonev
 */
@Provider
public class StaleDataModificationExceptionHandler implements ExceptionMapper<StaleDataModificationException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private LabelProvider labelProvider;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private ResourceService resourceService;

	@Override
	public Response toResponse(StaleDataModificationException exception) {
		LOGGER.info(exception.getMessage());
		LOGGER.trace("", exception);

		JsonObjectBuilder result = Json.createObjectBuilder();

		result.add("messages", Json.createObjectBuilder().add("staleDataMessage",
				labelProvider.getValue("idoc.exception.staleDataModification.message")));

		if (exception.getLastModifiedOn() != null) {
			result.add("modifiedOn", typeConverter.convert(String.class, exception.getLastModifiedOn()));
		}
		String displayName = resourceService.getDisplayName(exception.getModifiedBy());
		if (displayName != null) {
			result.add("modifiedBy", displayName);
		}

		ExceptionMapperUtil.appendExceptionMessages(result, exception);

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.CONFLICT, result.build());
	}

}
