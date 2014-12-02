package com.sirma.itt.emf.cls.rest;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.map.util.ISO8601DateFormat;

/**
 * Jackson configuration that overrides the default serialization of dates.
 * 
 * @author Mihail Radkov
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonConfig implements ContextResolver<ObjectMapper> {

	/** Maps configurations. */
	private ObjectMapper objectMapper;

	/**
	 * Class constructor. Sets the date format and forbids Jackson to serialize dates as time
	 * stamps.
	 */
	public JacksonConfig() {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.setDateFormat(new ISO8601DateFormat());
		this.objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		// Prevent exception to be thrown when no filter is applied
		FilterProvider filters = new SimpleFilterProvider().setFailOnUnknownId(false);
		this.objectMapper.setFilters(filters);
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectMapper getContext(Class<?> objectType) {
		return objectMapper;
	}
}