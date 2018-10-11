package com.sirma.itt.seip.synchronization.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;

/**
 * Producer for {@link SyncRuntimeConfiguration} instances before POST requests
 *
 * @author BBonev
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class SyncRuntimeConfigurationBodyReader implements MessageBodyReader<SyncRuntimeConfiguration> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SyncRuntimeConfiguration.class.isAssignableFrom(type);
	}

	@Override
	public SyncRuntimeConfiguration readFrom(Class<SyncRuntimeConfiguration> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {

		SyncRuntimeConfiguration configuration = new SyncRuntimeConfiguration();

		if (entityStream.available() > 0) {
			try (JsonReader reader = Json.createReader(entityStream)) {
				populateConfiguration(configuration, reader);
			}
		}

		return configuration;
	}

	private static void populateConfiguration(SyncRuntimeConfiguration configuration, JsonReader reader) {
		JsonObject object = reader.readObject();
		if (object.getBoolean("force", false)) {
			configuration.enableForceSynchronization();
		}
		if (object.getBoolean("allowDelete", false)) {
			configuration.allowDelete();
		}
	}

}
