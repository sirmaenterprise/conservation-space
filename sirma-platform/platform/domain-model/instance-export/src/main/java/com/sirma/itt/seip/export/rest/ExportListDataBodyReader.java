package com.sirma.itt.seip.export.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Converts {@link JsonObject} to {@link ExportListDataXlsxRequest}.
 *
 * @author gshefkedov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class ExportListDataBodyReader implements MessageBodyReader<ExportListDataXlsxRequest> {

	private static final String COMMON_PROPERTIES = "COMMON_PROPERTIES";
	private static final String MANAULLY_SELECTED = "manually";
	@BeanParam
	private RequestInfo request;

	@Inject
	private SearchService searchService;

	@Inject
	private JsonToConditionConverter convertor;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ExportListDataXlsxRequest.class.isAssignableFrom(type);
	}

	@Override
	public ExportListDataXlsxRequest readFrom(Class<ExportListDataXlsxRequest> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, toExportWidgetRequest());
	}

	/**
	 * Process data and converts it to {@link ExportListDataXlsxRequest}.
	 *
	 * @return executable function on json creation.
	 */
	private Function<JsonObject, ExportListDataXlsxRequest> toExportWidgetRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("Request payload is empty.");
			}
			ExportListDataXlsxRequest exportRequest = new ExportListDataXlsxRequest();
			exportRequest.setFileName(FileUtil.convertToValidFileName(json.getString("filename")));
			exportRequest.setHeaderType(json.getString("instanceHeaderType"));
			List<String> selectedInstances = JSON.getStringArray(json.getJsonArray("selectedObjects"));
			if (selectedInstances.isEmpty() && !MANAULLY_SELECTED.equals(json.getString("selectObjectMode"))) {
				selectedInstances = getSelectedInstancesFromCriteria(json);
			}
			exportRequest.setSelectedInstances(selectedInstances);
			Set<String> selectedProperties = iterateJsonObject(json.getJsonObject("selectedProperties"));
			exportRequest.setSelectedProperties(selectedProperties);
			exportRequest.setTargetId(PATH_ID.get(request));
			return exportRequest;
		};
	}

	private List<String> getSelectedInstancesFromCriteria(JsonObject json) {
		JsonObject criteria = json.getJsonObject("criteria");
		Condition tree = convertor.parseCondition(criteria);
		MultivaluedMap<String, String> qparams = request.getUriInfo().getQueryParameters();
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(qparams.size()));
		searchRequest.setSearchTree(tree);
		SearchArguments<Instance> searchArgs = searchService.parseRequest(searchRequest);
		searchArgs.setPageSize(searchArgs.getMaxSize());
		searchArgs.setPageNumber(1);
		String orderBy = json.getString("orderBy");
		String orderDirection = json.getString("orderDirection");
		if (!orderBy.isEmpty() && !orderDirection.isEmpty()) {
			// clear default modifiedOn sorter
			searchArgs.getSorters().clear();
			searchArgs.addSorter(new Sorter(orderBy, orderDirection));
		}
		searchService.search(Instance.class, searchArgs);
		return getInstanceIds(searchArgs.getResult());
	}

	private static List<String> getInstanceIds(List<Instance> instances) {
		List<String> selectedInstances = new ArrayList<String>(instances.size());
		for (Instance instance : instances) {
			selectedInstances.add(instance.getId().toString());
		}
		return selectedInstances;
	}

	private static Set<String> iterateJsonObject(JsonObject jsonObject) {
		Set<String> keys = jsonObject.keySet();
		Set<String> selectedProperties = new LinkedHashSet<String>();
		for (String key : keys) {
			if (!COMMON_PROPERTIES.equalsIgnoreCase(key)) {
				selectedProperties.addAll(JSON.getStringArray(jsonObject.getJsonArray(key)));
			}
		}
		return selectedProperties;
	}
}
