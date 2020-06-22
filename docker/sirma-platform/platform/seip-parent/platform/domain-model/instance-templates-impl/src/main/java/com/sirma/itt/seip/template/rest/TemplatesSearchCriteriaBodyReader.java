package com.sirma.itt.seip.template.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.collections4.map.HashedMap;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.template.TemplateSearchCriteria;

/**
 * JSON Body reader for {@link TemplateSearchCriteria}.
 *
 * @author Vilizar Tsonev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class TemplatesSearchCriteriaBodyReader implements MessageBodyReader<TemplateSearchCriteria> {

	private static final String GROUP = "group";
	private static final String PURPOSE = "purpose";
	private static final String FILTER = "filter";


	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return TemplateSearchCriteria.class.isAssignableFrom(type);
	}

	@Override
	public TemplateSearchCriteria readFrom(Class<TemplateSearchCriteria> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, json -> readCriteria(json));
	}

	private static TemplateSearchCriteria readCriteria(JsonObject json) {
		if (json.isEmpty()) {
			throw new BadRequestException("The JSON request payload is empty");
		}
		if (!json.containsKey(GROUP) || json.isNull(GROUP)) {
			throw new BadRequestException(GROUP + " is a required field when searching for a template");
		}
		if (!json.containsKey(PURPOSE) || json.isNull(PURPOSE)) {
			throw new BadRequestException(PURPOSE + " is a required field when searching for a template");
		}

		Map<String, Serializable> filterMap = new HashedMap<>();
		if (json.containsKey(FILTER) && !json.isNull(FILTER)) {
			JsonObject filtersJson = json.getJsonObject("filter");
			filterMap = JSON.jsonToMap(filtersJson);
		}

		return new TemplateSearchCriteria(json.getString(GROUP), json.getString(PURPOSE), filterMap);
	}
}