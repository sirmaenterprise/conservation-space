package com.sirma.sep.export.renders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueHtmlBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueTextBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Render for aggregate table widget.
 * 
 * @author Boyan Tonchev
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 31)
public class AggregateTableWidgetRenderer extends BaseRenderer {
	
	private static final String COLUMN_ORDER_VALUE_FIRST = "values-first";
	private static final String COLUMN_ORDER_NUMBER_FIRST = "numbers-first";

	public static final String KEY_LABEL_NUMBER = "widget.aggregated.table.number";
	/**
	 * Name of the aggregated-table widget.
	 */
	protected static final String AGGREGATE_TABLE_WIDGET_NAME = "aggregated-table";

	@Inject
	private UserPreferences userPreferences;

	@Override
	public boolean accept(ContentNode node) {
		return node != null && node.isWidget() && AGGREGATE_TABLE_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder htmlTableBuilder = renderWidgetFrame(jsonConfiguration);

		JsonObject groupBy = jsonConfiguration.getJsonObject(GROUP_BY);
		if (groupBy != null) {
			String groupByName = groupBy.getString(GROUP_BY_NAME);
			String groupByColumnTitle = groupBy.getString(GROUP_BY_LABEL);
			Map<String, String> codelistLabels = fetchCodelistLabels(groupBy);
			Map<String, Map<String, Serializable>> result = searchAggregated(currentInstanceId, jsonConfiguration);
			processResults(jsonConfiguration, htmlTableBuilder, groupByName, groupByColumnTitle, codelistLabels, result);
		} else {
			HtmlTableBuilder.addNoResultRow(htmlTableBuilder, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_RESULTS_NONE));
		}
		setWidth(htmlTableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		return htmlTableBuilder.build();
	}

	/**
	 * Process results and populate <code>htmlTableBuilder</code>.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @param htmlTableBuilder
	 *            the html table builder
	 * @param groupByName
	 *            the group by name
	 * @param groupByColumnTitle
	 *            the group by column title
	 * @param codelistLabels
	 *            the codelist labels
	 * @param result
	 *            the result
	 */
	private void processResults(JsonObject jsonConfiguration, HtmlTableBuilder htmlTableBuilder, String groupByName, String groupByColumnTitle, Map<String, String> codelistLabels,
			Map<String, Map<String, Serializable>> result) {
		Map<String, Serializable> groupByResult = result.get(groupByName);
		if (groupByResult == null) {
			return;
		}
		if (groupByResult.isEmpty()) {
			HtmlTableBuilder.addNoResultRow(htmlTableBuilder, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_RESULTS_NONE));
		} else {
			populateTable(groupByColumnTitle, htmlTableBuilder, groupByResult, codelistLabels, getColumnsOrder(jsonConfiguration));
			if (showFooter(jsonConfiguration)) {
				HtmlTableBuilder.addTotalResultRow(htmlTableBuilder, labelProvider.getLabel(KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL) + calculateTotalResult(groupByResult));
			}
		}
	}
	
	/**
	 * Fetch column order from configuration if order is not present default will be "numbers-first" this will happen for old instances before order implementation.
	 * @param jsonConfiguration configuration of widget.
	 * @return column order constant.
	 */
	private static String getColumnsOrder(JsonObject jsonConfiguration) {
		return jsonConfiguration.containsKey(COLUMN_ORDER) ? jsonConfiguration.getString(COLUMN_ORDER) : COLUMN_ORDER_NUMBER_FIRST;
	}

	/**
	 * Create rows with results for table <code>htmlTableBuilder</code>.
	 * 
	 * @param groupByColumnTitle
	 *            - title of broupBy column.
	 * @param htmlTableBuilder
	 *            the table builder where rows have to be populated.
	 * @param groupByResult
	 *            - results to be populated into <code>htmlTableBuilder</code>
	 * @param codelistLabels
	 *            - map with key -> code and value -> description of code.
	 */
	private void populateTable(String groupByColumnTitle, HtmlTableBuilder htmlTableBuilder, Map<String, Serializable> groupByResult, Map<String, String> codelistLabels, String columnOrder) {
		int numberColumnIndex = COLUMN_ORDER_NUMBER_FIRST.equals(columnOrder) ? 0 : 1;
		int valueColumnIndex = COLUMN_ORDER_VALUE_FIRST.equals(columnOrder) ? 0 : 1;
		
		HtmlValueTextBuilder numberTitle = new HtmlValueTextBuilder(labelProvider.getLabel(KEY_LABEL_NUMBER), true);
		numberTitle.addAttribute(JsoupUtil.ATTRIBUTE_STYLE, MARGIN_LEFT_12);
		htmlTableBuilder.addTdValue(0, numberColumnIndex, numberTitle);
		
		HtmlValueTextBuilder groupByBuilder = new HtmlValueTextBuilder(groupByColumnTitle == null ? "" : groupByColumnTitle, true);
		groupByBuilder.addAttribute(JsoupUtil.ATTRIBUTE_STYLE, MARGIN_LEFT_12);

		htmlTableBuilder.addTdValue(0, valueColumnIndex, groupByBuilder);
		int rowIndex = 1;
		List<Serializable> allInstances = new ArrayList<>(groupByResult.keySet());
		Map<String, String> compactInstanceHeaders = new HashMap<>();
		if (codelistLabels.isEmpty()) {
			compactInstanceHeaders = getCompactInstanceHeaders(allInstances);
		}
		for (Entry<String, Serializable> group : groupByResult.entrySet()) {
			HtmlValueTextBuilder number = new HtmlValueTextBuilder(group.getValue().toString());
			number.addAttribute(JsoupUtil.ATTRIBUTE_STYLE, MARGIN_LEFT_12);
			htmlTableBuilder.addTdValue(rowIndex, numberColumnIndex, number);
			htmlTableBuilder.addTdValue(rowIndex++, valueColumnIndex, createValue(compactInstanceHeaders, group.getKey(), codelistLabels));
		}

	}

	/**
	 * Builds html table value.
	 *
	 * @param compactInstanceHeaders
	 *            the compact instance headers
	 * @param value
	 *            the value
	 * @param codelistLabels
	 *            the codelist labels
	 * @return the html value builder
	 */
	private static HtmlValueBuilder createValue(Map<String, String> compactInstanceHeaders, String value, Map<String, String> codelistLabels) {
		HtmlValueBuilder valueBuilder;
		String instanceHeader = compactInstanceHeaders.get(value);
		if (instanceHeader != null) {
			valueBuilder = new HtmlValueHtmlBuilder(instanceHeader);
		} else {
			valueBuilder = new HtmlValueTextBuilder(codelistLabels.getOrDefault(value, value));
		}
		valueBuilder.addAttribute(JsoupUtil.ATTRIBUTE_STYLE, MARGIN_LEFT_12);
		return valueBuilder;
	}

	/**
	 * Fetch all descriptions of codelits presented in groupBy jsno. Json can be:
	 * 
	 * <pre>
	 * 	 "groupBy": {
	 * 		"name": "emf:status",
	 * 		"label": "State",
	 * 		"codelist": [106, 1]
	 * 	}
	 * </pre>
	 * 
	 * @param groupBy
	 *            - the json "groupBy".
	 * @return map populated with key -> code from codelist and value -> descriptions. If key is present more than one
	 *         codelist then descriptions will be concatenated by ",".
	 */
	private Map<String, String> fetchCodelistLabels(JsonObject groupBy) {
		Map<String, String> codelistLabels = new HashMap<>();
		if (groupBy.containsKey("codelist")) {
			List<Integer> clCodes = getCLCodes(groupBy);
			clCodes.forEach(clCode -> populateFromCodelist(clCode, codelistLabels));
		}
		return codelistLabels;
	}

	/**
	 * Gets the CL codes.
	 *
	 * @param groupBy
	 *            - the json "groupBy".
	 * @return integer list of CL codes
	 */
	private static List<Integer> getCLCodes(JsonObject groupBy) {
		JsonValue codelistValue = groupBy.get("codelist");
		if (codelistValue instanceof JsonArray) {
			JsonArray codelists = (JsonArray) codelistValue;
			return codelists.stream().map(JsonNumber.class::cast).map(JsonNumber::intValue).collect(Collectors.toList());
		}
		if (codelistValue instanceof JsonNumber) {
			JsonNumber codelist = (JsonNumber) codelistValue;
			return Collections.singletonList(codelist.intValue());
		}
		if (codelistValue instanceof JsonString) {
			JsonString codelist = (JsonString) codelistValue;
			return Collections.singletonList(Integer.valueOf(codelist.getString()));
		}
		return Collections.emptyList();
	}

	/**
	 * Populate description of <code>codelist</code> into <code>codelistLabels</code>.
	 *
	 * @param codelist
	 *            the codelist
	 * @param codelistLabels
	 *            the codelist labels
	 */
	private void populateFromCodelist(int codelist, Map<String, String> codelistLabels) {
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(codelist);
		if (codeValues != null) {
			Locale locale = new Locale(userPreferences.getLanguage());
			for (Entry<String, CodeValue> codeValue : codeValues.entrySet()) {
				CodeValue value = codeValue.getValue();
				addValue(codelistLabels, codeValue.getKey(), value.getDescription(locale));
			}
		}
	}

	/**
	 * Add <code>valuetoBeAdded</code> into <code>codelistLabels</code>. If key is present more than one codelist then
	 * descriptions will be concatenated by ",".
	 *
	 * @param codelistLabels
	 *            the codelist labels
	 * @param key
	 *            the key
	 * @param valuetoBeAdded
	 *            the valueto be added
	 */
	private static void addValue(Map<String, String> codelistLabels, String key, String valuetoBeAdded) {
		if (StringUtils.isNotBlank(valuetoBeAdded)) {
			if (codelistLabels.containsKey(key)) {
				String existingValue = codelistLabels.get(key);
				codelistLabels.put(key, existingValue + ", " + valuetoBeAdded);
			} else {
				codelistLabels.put(key, valuetoBeAdded);
			}
		}
	}

	/**
	 * Calculate total result.
	 * 
	 * @param groupByResult
	 *            - aggregated results.
	 * @return total results.
	 */
	private static int calculateTotalResult(Map<String, Serializable> groupByResult) {
		return groupByResult.values().stream().mapToInt(Integer.class::cast).sum();
	}

	/**
	 * Prepare search arguments and execute search.
	 * 
	 * @param currentInstanceId
	 *            - the id of current instance.
	 * @param jsonConfiguration
	 *            - configuration of widget.
	 * @return aggregated result.
	 */
	private Map<String, Map<String, Serializable>> searchAggregated(String currentInstanceId, JsonObject jsonConfiguration) {
		SearchArguments<Instance> searchArguments = getSearchArguments(currentInstanceId, jsonConfiguration);
		searchService.search(Instance.class, searchArguments);
		return searchArguments.getAggregatedData();
	}

	/**
	 * Prepare search arguments for aggregated search.
	 * 
	 * @param currentInstanceId
	 *            - the id of current instance.
	 * @param jsonConfiguration
	 *            - configuration of widget.
	 * @return the search arguments.
	 */
	private SearchArguments<Instance> getSearchArguments(String currentInstanceId, JsonObject jsonConfiguration) {
		JsonObject criteria = jsonConfiguration.getJsonObject(SEARCH_CRITERIA);
		Condition tree = converter.parseCondition(criteria);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setRequest(new HashMap<>());
		searchRequest.setSearchTree(tree);

		List<String> selectedObjects = new ArrayList<>();
		if (jsonConfiguration.containsKey(SELECT_OBJECT_MODE)) {
			String selectObjectMode = jsonConfiguration.getString(SELECT_OBJECT_MODE);
			if (jsonConfiguration.containsKey(SELECTED_OBJECTS) && MANUALLY.equals(selectObjectMode)) {
				for (Serializable selectdObject : JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_OBJECTS))) {
					selectedObjects.add((String) selectdObject);
				}
			}
		}

		String groupBy = jsonConfiguration.getJsonObject(GROUP_BY).getString(GROUP_BY_NAME);
		searchRequest.getRequest().put(GROUP_BY, Collections.singletonList(groupBy));
		searchRequest.getRequest().put(SELECTED_OBJECTS, selectedObjects);

		SearchArguments<Instance> searchArgs = searchService.parseRequest(searchRequest);
		String newQuery = searchArgs.getStringQuery().replace(CURRENT_OBJECT, currentInstanceId);
		searchArgs.setStringQuery(newQuery);
		return searchArgs;
	}

	/**
	 * Gets the mapping between the IDs of the provided instances and their compact headers.
	 *
	 * @param instanceIds
	 *            the list with the instance ids
	 * @return the map with the instance ids and headers
	 */
	private Map<String, String> getCompactInstanceHeaders(List<Serializable> instanceIds) {
		Collection<Instance> loadedInstances = loadInstances(instanceIds);
		return loadedInstances.stream().collect(
				Collectors.toMap(instance -> instance.getId().toString(), instance -> IdocRenderer.getHyperlink(instance, DefaultProperties.HEADER_COMPACT, systemConfiguration.getUi2Url().get())));
	}

}
