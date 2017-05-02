package com.sirma.itt.seip.resources.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.resources.security.UserPasswordChangeRequest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Reads {@link UserPasswordChangeRequest} for the {@link UserSettingsRestService}.
 * 
 * @author smustafov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class UserPasswordChangeRequestReader implements MessageBodyReader<UserPasswordChangeRequest> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return UserPasswordChangeRequest.class.isAssignableFrom(type);
	}

	@Override
	public UserPasswordChangeRequest readFrom(Class<UserPasswordChangeRequest> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		return JSON.readObject(entityStream, json -> toPasswordChangeRequest(json));
	}

	private static UserPasswordChangeRequest toPasswordChangeRequest(JsonObject json) {
		if (json.isEmpty()) {
			throw new BadRequestException("The request payload is empty.");
		}

		UserPasswordChangeRequest passwordChangeRequest = new UserPasswordChangeRequest();
		passwordChangeRequest.setUsername(json.getString(JsonKeys.USERNAME));
		passwordChangeRequest.setOldPassword(json.getString(JsonKeys.OLD_PASSWORD));
		passwordChangeRequest.setNewPassword(json.getString(JsonKeys.NEW_PASSWORD));
		return passwordChangeRequest;
	}

}
