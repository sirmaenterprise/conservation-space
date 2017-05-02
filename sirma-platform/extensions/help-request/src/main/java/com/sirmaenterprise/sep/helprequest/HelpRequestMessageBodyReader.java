package com.sirmaenterprise.sep.helprequest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Convert json data to {@link HelpRequestMessage} object. Json data have to be:
 * <pre>
 * {
 *  params: {
 * 	subject: "subject of mail",
 * 	type: "code of code list. For example: CL600",
 * 	description: "content of mail" 
 * 	} 
 * }
 * </pre>
 * 
 * @author Boyan Tonchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class HelpRequestMessageBodyReader implements MessageBodyReader<HelpRequestMessage> {

	/**
	 * Json keys constants.
	 */
	public static final String JSON_KEY_PARAMS = "params";
	public static final String JSON_KEY_SUBJECT = "subject";
	public static final String JSON_KEY_DESCRIPTION = "description";
	public static final String JSON_KEY_TYPE = "type";

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return HelpRequestMessage.class.isAssignableFrom(type);
	}

	@Override
	public HelpRequestMessage readFrom(Class<HelpRequestMessage> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {

		return JSON.readObject(entityStream, buildMailMessage());
	}

	/**
	 * Build the {@link HelpRequestMessage} object.
	 * 
	 * @return the object.
	 */
	@SuppressWarnings("static-method")
	private Function<JsonObject, HelpRequestMessage> buildMailMessage() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}
			JsonObject jsonParams = json.getJsonObject(JSON_KEY_PARAMS);
			HelpRequestMessage helpRequestMessage = new HelpRequestMessage();
			helpRequestMessage.setSubject(jsonParams.getString(JSON_KEY_SUBJECT));
			helpRequestMessage.setDescription(jsonParams.getString(JSON_KEY_DESCRIPTION));
			helpRequestMessage.setType(jsonParams.getString(JSON_KEY_TYPE));

			return helpRequestMessage;
		};
	}
}
