package com.sirmaenterprise.sep.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;

/**
 * JSON Body reader for {@link AnnotationSearchCriteria}.
 *
 * @author Vilizar Tsonev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class AnnotationsSearchCriteriaBodyReader implements MessageBodyReader<AnnotationsSearchCriteria> {

	@Inject
	private DateConverter dateConverter;

	@Inject
	private JsonToConditionConverter jsonToConditionConverter;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return AnnotationsSearchCriteria.class.isAssignableFrom(type);
	}

	@Override
	public AnnotationsSearchCriteria readFrom(Class<AnnotationsSearchCriteria> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, json -> readCriteria(json));
	}

	private AnnotationsSearchCriteria readCriteria(JsonObject json) {
		if (json.isEmpty()) {
			throw new BadRequestException("The JSON request payload is empty");
		}
		AnnotationsSearchCriteria criteria = new AnnotationsSearchCriteria();
		boolean isHistoricalVersion = json.getBoolean("isHistoricalVersion");
		JsonObject filters = json.getJsonObject("filters");
		JsonArray dateRangeJson = filters.getJsonArray("dateRange");
		parseDateRange(dateRangeJson, criteria);

		// This list may contain not only the manually picked instances by the user, but also if he has checked "include
		// comments for current object" - its URI is passed here
		List<String> manuallySelectedObjects = JSON.getStringArray(json, "manuallySelectedObjects");
		criteria.setManuallySelectedObjects(manuallySelectedObjects);

		JsonObject searchCriteriaJson = json.getJsonObject("criteria");
		if (!searchCriteriaJson.isEmpty()) {
			Condition tree = jsonToConditionConverter.parseCondition(searchCriteriaJson);
			criteria.setSearchTree(tree);
		}

		List<String> userIds = JSON.getStringArray(filters, "userIds");
		criteria.setUserIds(userIds);
		criteria.setOffset(Integer.valueOf(filters.getInt("offset")));
		criteria.setLimit(Integer.valueOf(filters.getInt("limit")));
		criteria.setStatus(filters.getString("status", null));
		criteria.setText(filters.getString("text", null));
		criteria.setIsHistoricalVersion(isHistoricalVersion);
		return criteria;
	}

	/**
	 * Retrieves the from/to dates from the passed dateRangeJson and enriches the criteria with them (if provided).
	 *
	 * @param dateRangeJson
	 *            the source json array
	 * @param criteria
	 *            the destination criteria
	 */
	private void parseDateRange(JsonArray dateRangeJson, AnnotationsSearchCriteria criteria) {
		String createdFrom = dateRangeJson.getString(0);
		String createdTo = dateRangeJson.getString(1);
		if (StringUtils.isNotBlank(createdFrom)) {
			Date createdFromDate = dateConverter.parseDate(createdFrom);
			criteria.setCreatedFrom(createdFromDate);
		}
		if (StringUtils.isNotBlank(createdTo)) {
			Date createdToDate = dateConverter.parseDate(createdTo);
			criteria.setCreatedTo(createdToDate);
		}
	}
}