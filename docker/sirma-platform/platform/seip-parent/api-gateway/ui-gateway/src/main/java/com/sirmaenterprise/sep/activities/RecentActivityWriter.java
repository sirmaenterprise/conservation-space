package com.sirmaenterprise.sep.activities;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.annotations.search.Search;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.models.SearchResponseWrapper;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Message body writer for {@link StoredAuditActivity}
 * {@link SearchResponseWrapper search response}.
 * 
 * @author yasko
 */
@Search
@Provider
@Produces(Versions.V2_JSON)
public class RecentActivityWriter extends AbstractMessageBodyWriter<SearchResponseWrapper<RecentActivity>> {

	@Inject
	private TypeConverter typeConverter;
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (!SearchResponseWrapper.class.isAssignableFrom(type)) {
			return false;
		}
		
		Class<?> actual = Types.getTypeArgument(genericType);
		return actual != null && RecentActivity.class.isAssignableFrom(actual);
	}

	@Override
	public void writeTo(SearchResponseWrapper<RecentActivity> res, Class<?> type, Type generic, Annotation[] annotations,
			MediaType media, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {
		
		try(JsonGenerator generator = Json.createGenerator(out)) {
			generator.writeStartArray();
			res.getResults().forEach(activity -> this.convert(activity, generator));
			generator.writeEnd();
		}
	}

	private void convert(RecentActivity activity, JsonGenerator generator) {
		Instance user = activity.getUser();
		
		generator.writeStartObject();
		generator.writeStartObject(JsonKeys.USER );
		generator.write(JsonKeys.ID, user.getId().toString());
		generator.write(JsonKeys.NAME, user.getLabel());
		generator.writeEnd();

		generator.write(JsonKeys.TIMESTAMP, typeConverter.convert(String.class, activity.getTimestamp()));
		generator.write(JsonKeys.TEXT, activity.getSentence());
		
		generator.writeEnd();
	}
}
