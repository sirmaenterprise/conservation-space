package com.sirmaenterprise.sep.activities;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.time.DateRange;

/**
 * Request reader for {@link RecentActivitiesRequest}. At the moment support parameter retrieval as query parameters or as
 * request payload. The first option is done for backward compatibility, until the web client is changed.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class RecentActivitiesRequestReader implements MessageBodyReader<RecentActivitiesRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return RecentActivitiesRequest.class.isAssignableFrom(type);
	}

	@Override
	public RecentActivitiesRequest readFrom(Class<RecentActivitiesRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		
			return JSON.readObject(entityStream, toRecentActivitiesRequest());
	}

	private static Function<JsonObject, RecentActivitiesRequest> toRecentActivitiesRequest() {
		return json -> {
			if (json.isEmpty()) {
				// empty request
				return new RecentActivitiesRequest();
			}

			int limit = json.getInt(JsonKeys.LIMIT);
			int offset = json.getInt(JsonKeys.OFFSET);
			Collection<Serializable> ids = collectInstanceIds(json);
			DateRange range = JSON.getDateRange(json);
			return new RecentActivitiesRequest().setLimit(limit).setOffset(offset).setIds(ids).setDateRange(range);
		};
	}

	private static Collection<Serializable> collectInstanceIds(JsonObject json) {
		List<String> instanceIds = JSON.getStringArray(json, JsonKeys.INSTANCE_IDS);
		String currentInstanceId = json.getString(JsonKeys.CURRENT_INSTANCE_ID, null);
		Set<Serializable> ids = new HashSet<>(instanceIds.size() + 1);
		ids.addAll(instanceIds);
		addNonNullValue(ids, StringUtils.trimToNull(currentInstanceId));
		return ids;
	}

}
