package com.sirma.sep.export.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.sep.export.ExportRequest;
import com.sirma.sep.export.SupportedExportFormats;
import com.sirma.sep.export.renders.IdocRenderer;

/**
 * Request object used to execute export, which will generate file in MS Excel format. It holds the required information
 * for successful execution of the export process.
 *
 * @author A. Kunchev
 */
public class XlsxExportRequest extends ExportRequest {

	/** Used to hold information about the objects that should be exported. */
	private final ObjectsData objectsData;

	/** Used to hold information needed to execute search and retrieve the objects that should be exported. */
	private final SearchData searchData;

	/** Used to hold information about the configuration of the table and how the information should be exported. */
	private final TableConfiguration tableConfiguration;

	/**
	 * Instantiates new xlsx export request.
	 *
	 * @param objectsData
	 *            holds the information for the object that should be exported
	 * @param searchData
	 *            holds the information about the search, which is used to find the objects that should be exported
	 * @param tableConfiguration
	 *            holds the information about the table configuration and how the data for the object should be expored
	 */
	protected XlsxExportRequest(final ObjectsData objectsData, final SearchData searchData,
			final TableConfiguration tableConfiguration) {
		this.objectsData = objectsData;
		this.searchData = searchData;
		this.tableConfiguration = tableConfiguration;
	}

	@Override
	public String getName() {
		return SupportedExportFormats.XLS.getFormat();
	}

	public ObjectsData getObjectsData() {
		return objectsData;
	}

	public SearchData getSearchData() {
		return searchData;
	}

	public TableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}

	/**
	 * Builder for the {@link XlsxExportRequest}.
	 *
	 * @author A. Kunchev
	 */
	public static class XlsxExportRequestBuilder {

		private boolean manuallySelected;
		private List<String> manuallySelectedObjects;
		private String instanceHeaderType;
		private Map<String, List<String>> selectedProperties;
		private Map<String, Set<String>> selectedSubProperties;
		private Map<String, String> headersInfo;
		private JsonObject searchCriteria;
		private String orderBy;
		private String orderDirection;
		private boolean showInstanceId;

		/**
		 * Instantiates new xlsx export request builder.
		 */
		public XlsxExportRequestBuilder() {
			// empty
		}

		/**
		 * Sets configuration that shows if the displayed objects are selected manually or not.
		 *
		 * @param manuallySelected
		 *            shows if the displayed object are selected manually
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setManuallySelected(boolean manuallySelected) {
			this.manuallySelected = manuallySelected;
			return this;
		}

		/**
		 * Sets the ids of the manually selected objects that should be displayed.
		 *
		 * @param manuallySelectedObjects
		 *            collection of instance ids of the manually selected objects
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setManuallySelectedObjects(List<String> manuallySelectedObjects) {
			this.manuallySelectedObjects = manuallySelectedObjects;
			return this;
		}

		/**
		 * Sets the type of the instance header that should be shown in to the table (compact, default, breadcrumb,
		 * etc.)
		 *
		 * @param instanceHeaderType
		 *            the instanceHeaderType to set
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setInstanceHeaderType(String instanceHeaderType) {
			this.instanceHeaderType = instanceHeaderType;
			return this;
		}

		/**
		 * Sets the map with selected properties that should be shown for the objects.
		 *
		 * @param selectedProperties
		 *            map with properties that should be displayed for the objects
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setSelectedProperties(Map<String, List<String>> selectedProperties) {
			this.selectedProperties = selectedProperties;
			return this;
		}

		/**
		 * Sets the map with selected sub properties that should be shown for the object property.
		 *
		 * @param selectedSubProperties
		 *            map with properties that should be displayed for the object property
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setSelectedSubProperties(Map<String, Set<String>> selectedSubProperties) {
			this.selectedSubProperties = selectedSubProperties;
			return this;
		}

		/**
		 * Sets the labels of the headers that should be added in the first row of the result excel file.
		 *
		 * @param headersInfo
		 *            the headersInfo to set
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setHeadersInfo(Map<String, String> headersInfo) {
			this.headersInfo = headersInfo;
			return this;
		}

		/**
		 * Sets the criteria used in the search to find the objects that should be displayed.
		 *
		 * @param searchCriteria
		 *            search criteria as Json object, used to find the objects
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setSearchCriteria(JsonObject searchCriteria) {
			this.searchCriteria = searchCriteria;
			return this;
		}

		/**
		 * Sets the order by clause used in the search.
		 *
		 * @param orderBy
		 *            property used for ordering
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setOrderBy(String orderBy) {
			this.orderBy = orderBy;
			return this;
		}

		/**
		 * Sets the ordering directions - ascending or descending.
		 *
		 * @param orderDirection
		 *            direction of ordering, "asc" or "desc"
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setOrderDirection(String orderDirection) {
			this.orderDirection = orderDirection;
			return this;
		}

		/**
		 * Sets flag if id of instance have to be populated in generated excel.
		 *
		 * @param showInstanceId
		 *            - true if instance id have to be populated in generated excel.
		 * @return current builder to allow methods chaining
		 */
		public XlsxExportRequestBuilder setShowInstanceId(boolean showInstanceId) {
			this.showInstanceId = showInstanceId;
			return this;
		}

		/**
		 * Builds new xlsx export request with the provided data.
		 *
		 * @return new {@link XlsxExportRequest}
		 */
		public XlsxExportRequest buildRequest() {
			return new XlsxExportRequest(
					new ObjectsData(manuallySelectedObjects, instanceHeaderType, selectedProperties,
							selectedSubProperties),
					new SearchData(searchCriteria, orderBy, orderDirection),
					new TableConfiguration(manuallySelected, headersInfo, showInstanceId));
		}
	}

	/**
	 * Builder for the {@link XlsxExportRequest} from provided {@link JsonObject}.
	 *
	 * @author A. Kunchev
	 */
	public static class JsonXlsxExportRequestBuilder {

		private static final String INSTANCE_HEADER_TYPE_KEY = "instanceHeaderType";
		private static final String NONE_INSTANCE_HEADER = "none";
		private static final String SHOW_INSTANCE_ID = "showInstanceId";

		private final JsonObject json;

		/**
		 * Instantiates new xlsx export request builder that retrieves the information from {@link JsonObject}.
		 *
		 * @param json
		 *            the object from which will be retrieved the data for the xlsx export request
		 */
		public JsonXlsxExportRequestBuilder(final JsonObject json) {
			this.json = json;
		}

		/**
		 * Parses the provided {@link JsonObject} and builds new xlsx export request from it.
		 *
		 * @return new {@link XlsxExportRequest}
		 */
		public XlsxExportRequest buildRequest() {
			Objects.requireNonNull(json, "The json object is required argument!");
			return new XlsxExportRequestBuilder()
					.setManuallySelected(IdocRenderer.MANUALLY.equalsIgnoreCase(json.getString("selectObjectMode")))
					.setManuallySelectedObjects(JSON.getStringArray(json.getJsonArray("selectedObjects")))
					.setInstanceHeaderType(getInstanceHeaderTypeOrDefault(json))
					.setSelectedProperties(IdocRenderer.getSelectedProperties(json))
					.setSelectedSubProperties(IdocRenderer.getSelectedSubProperties(json))
					.setHeadersInfo(getHeadersInfo(json)).setOrderBy(json.getString("orderBy"))
					.setOrderDirection(json.getString("orderDirection")).setShowInstanceId(showSystemId(json))
					.setSearchCriteria(json.getJsonObject("criteria")).buildRequest();
		}

		/**
		 * Retrieves the type of the headers that should be exported for the instances. If such type is not passed or
		 * the passed value is 'none', {@link DefaultProperties#HEADER_COMPACT} will be set by default.
		 */
		private static String getInstanceHeaderTypeOrDefault(JsonObject requestJson) {
			String instanceHeaderType = extractInstanceHeaderType(requestJson);
			return NONE_INSTANCE_HEADER.equals(instanceHeaderType) ? DefaultProperties.HEADER_COMPACT
					: instanceHeaderType;
		}

		private static String extractInstanceHeaderType(JsonObject json) {
			JsonValue instanceHeaderType = json.get(INSTANCE_HEADER_TYPE_KEY);
			if (instanceHeaderType != null && !ValueType.NULL.equals(instanceHeaderType.getValueType())) {
				return json.getString(INSTANCE_HEADER_TYPE_KEY);
			}

			return NONE_INSTANCE_HEADER;
		}

		/**
		 * Retrieves labels for the first row of the excel. This labels represents the headers for the displayed
		 * information about the objects.
		 */
		private static Map<String, String> getHeadersInfo(JsonObject requestJson) {
			Map<String, String> headersInfo = new LinkedHashMap<>();
			Optional.ofNullable(requestJson.getJsonArray("selectedHeaders"))
					.ifPresent(labels -> labels.forEach(label -> {
						JsonObject object = (JsonObject) label;
						String name = object.getString(DefaultProperties.NAME);
						// Concatenate labels with ",". We have scenario when for one property name we have different
						// label.
						// This happen when different instance types have one property name but different label for it.
						headersInfo.put(name, extractLabel(object));
					}));
			return headersInfo;
		}

		private static boolean showSystemId(JsonObject requestJson) {
			return requestJson.getBoolean(SHOW_INSTANCE_ID, true);
		}

		private static String extractLabel(JsonObject object) {
			return object.getJsonArray("labels").stream().filter(label -> !ValueType.NULL.equals(label.getValueType()))
					.map(label -> ((JsonString) label).getString()).filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(", "));
		}
	}

}
