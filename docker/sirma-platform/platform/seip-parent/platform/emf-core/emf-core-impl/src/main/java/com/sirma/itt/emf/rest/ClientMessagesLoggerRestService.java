package com.sirma.itt.emf.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Logger for client side messages like debug or errors.
 *
 * @author svelikov
 */
@Path("/logger")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClientMessagesLoggerRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientMessagesLoggerRestService.class);

	/**
	 * Logs the provided messages according to their status.
	 *
	 * @param data
	 *            The message and the type of the message.
	 * @return because the client should not expect any response body, we just return status 204.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void logWebErrors(String data) {
		if (StringUtils.isBlank(data)) {
			throw new BadRequestException("Can not log error message because of missing data!");
		}
		JSONObject jsonObject = JsonUtil.toJsonObject(data);
		String logLevel = JsonUtil.getStringValue(jsonObject, "type");
		String message = JsonUtil.getStringValue(jsonObject, "message");
		String url = JsonUtil.getStringValue(jsonObject, "url");
		if ("debug".equals(logLevel)) {
			LOGGER.debug("{}, {}", message, url);
		} else {
			LOGGER.error("{}, {}", message, url);
		}
	}
}
