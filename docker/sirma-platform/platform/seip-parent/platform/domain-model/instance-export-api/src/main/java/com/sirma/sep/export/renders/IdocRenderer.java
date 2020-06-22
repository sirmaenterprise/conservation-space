package com.sirma.sep.export.renders;

import java.io.Serializable;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueElementBuilder;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Renderers Plugin interface.
 *
 * @author Hristo Lungov
 */
// TODO move in export-api
public interface IdocRenderer extends Plugin {

	/**
	 * Constant passed in widgets configuration. It manage if current object have to be included to selected instances.
	 */
	String SELECT_CURRENT_OBJECT = "selectCurrentObject";

	/** IdocRenderer Plugin extensions name */
	String PLUGIN_NAME = "IdocRenderer";

	/** Constant passed in widget config {@link #SELECTED_PROPERTIES} which must be skipped */
	String COMMON_PROPERTIES = "COMMON_PROPERTIES";

	/** Constant passed in widget config that specifies exact header type to be resolved */
	String INSTANCE_HEADER_TYPE = "instanceHeaderType";

	/** Constant passed in object data widget config to specify exact header type to be resolved */
	String INSTANCE_LINK_TYPE = "instanceLinkType";

	/** Constant passed in widget config that specifies selected properties to be resolved */
	String SELECTED_PROPERTIES = "selectedProperties";

	/** Constant passed in widget config for getting search criteria */
	String SEARCH_CRITERIA = "criteria";

	/** Constant passed in object data widget config for getting selected instance identifier */
	String SELECTED_OBJECT = "selectedObject";

	/** Constant passed in data table widget config for getting selected instances identifiers */
	String SELECTED_OBJECTS = "selectedObjects";

	/** Constant passed in widget config for getting selection object mode */
	String SELECT_OBJECT_MODE = "selectObjectMode";

	/** Constant passed in widget config for getting is widget header visible */
	String SHOW_WIDGET_HEADER = "showWidgetHeader";

	/** Constant passed in widget config for getting is widget borders visible */
	String SHOW_WIDGET_BORDERS = "showWidgetBorders";

	/** Constant passed in widget config to verify that selection object mode is manually */
	String MANUALLY = "manually";

	String AUTOMATICALLY = "automatically";

	/** Constant passed in widget config to verify that selection object mode is current */
	String CURRENT = "current";

	/** Constant used for storing hyperlinks */
	String HYPERLINKS = "hyperlinks";

	/** Widget configuration that determines if header icons will be shown */
	String HIDE_ICONS = "hideIcons";

	/** Constant used for storing value in render property */
	String VALUE = "value";

	/** Constant used in data table widget first heading label */
	String ENTITY_LABEL = "Entity";

	/** Constant used in aggregate table widget used to be replaced in search query */
	String CURRENT_OBJECT = "current_object";

	/** Constant passed in widget config for getting group by */
	String GROUP_BY = "groupBy";
	/** Constant passed in widget config for getting group by label */
	String GROUP_BY_LABEL = "label";
	/** Constant passed in widget config for getting group by name */
	String GROUP_BY_NAME = "name";

	/** Constant passed in widget config for getting show footer */
	String SHOW_FOOTER = "showFooter";

	/** keys for label */
	String KEY_LABEL_SELECT_OBJECT_RESULTS_NONE = "select.object.results.none";
	String KEY_LABEL_WIDGET_AGGREGATED_TABLE_TOTAL = "widget.aggregated.table.total";
	String KEY_LABEL_SEARCH_RESULTS = "widget.datatable.search.results";
	String KEY_LABEL_SELECT_OBJECT_NONE = "select.object.none";
	String KEY_LABEL_MORE_THAN_ONE = "select.object.automatically.more.than.one.message";
	String KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA = "select.object.undefined.criteria";
	String KEY_LABEL_NO_PREVIEW_AVAILABLE = "labels.preview.none";
	String KEY_LABEL_NO_IMAGES_SELECTED = "imagewidget.no.images.selected";
	String KEY_LABEL_NO_COMMENTS = "comments.widget.error";
	String KEY_LABEL_MULTIPLE_OBJECTS = "widget.export.multiple.objects";

	String COLUMN_ORDER = "columnsOrder";
	String DISPLAY_TABLE_HEADER_ROW = "displayTableHeaderRow";

	String MARGIN_LEFT_15 = "margin-left: 15px;";

	String MARGIN_LEFT_12 = "margin-left: 12px;";

	String NIGHTY_NINE_PRECENT = "99%";

	String SEPARATOR = ":";
	
	String COLUMN_HEADER = "columnHeaders";
	
	String RICHTEXT = "RICHTEXT";

	/**
	 * Verify that corresponding content node is for current render.
	 *
	 * @param node
	 *            the node
	 * @return true, if successful
	 */
	boolean accept(ContentNode node);

	/**
	 * Render the content node to word document
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param node
	 *            the node
	 * @return
	 */
	Element render(String currentInstanceId, ContentNode node);

	/**
	 * Gets the instance header type or instance link type(if ODW for renderer compatibility reasons) or finally return
	 * default one DefaultProperties.HEADER_COMPACT.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the instance header type
	 */
	static String getInstanceHeaderType(JsonObject jsonConfiguration) {
		return jsonConfiguration.getString(INSTANCE_HEADER_TYPE, IdocRenderer.getInstanceLinkType(jsonConfiguration));
	}

	/**
	 * Gets the widget selection type - current,automatically or manually.
	 *
	 * @param jsonConfiguration
	 *            the widget json configuration
	 * @return search selection mode
	 */
	static String getSelectionMode(JsonObject jsonConfiguration) {
		return jsonConfiguration.getString(SELECT_OBJECT_MODE, "");
	}

	/**
	 * Gets the widget configuration for borders. If true borders must be visible, else must be no borders.
	 *
	 * @param jsonConfiguration
	 *            the widget json configuration
	 * @return true or false
	 */
	static boolean areWidgetBordersVisible(JsonObject jsonConfiguration) {
		return jsonConfiguration.getBoolean(SHOW_WIDGET_BORDERS, true);
	}

	/**
	 * Gets the title or return empty string.
	 *
	 * @see DefaultProperties#TITLE
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the widget title
	 */
	static String getWidgetTitle(JsonObject jsonConfiguration) {
		return jsonConfiguration.getString(DefaultProperties.TITLE, "");
	}

	/**
	 * Gets the showWidgetHeader widget configuration or true.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return boolean variable indicating if widget header should be displayed. True by default.
	 */
	static Boolean getShowWidgetHeader(JsonObject jsonConfiguration) {
		return jsonConfiguration.getBoolean(SHOW_WIDGET_HEADER, true);
	}

	/**
	 * Convert WidgetConfiguration to JsonObject.
	 *
	 * @param configuration
	 *            the configuration
	 * @return the json object
	 */
	static JsonObject toJson(WidgetConfiguration configuration) {
		try (JsonReader jsonReader = Json.createReader(new StringReader(configuration.getConfiguration().toString()))) {
			return jsonReader.readObject();
		}
	}

	/**
	 * Gets the {@value #SELECTED_PROPERTIES} from json configuration.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected properties
	 */
	static Map<String, List<String>> getSelectedProperties(JsonObject jsonConfiguration) {
		Map<String, List<String>> result = new HashMap<>();

		Optional.of(jsonConfiguration.getJsonObject(IdocRenderer.SELECTED_PROPERTIES))
				.ifPresent(jsonObject -> jsonObject.forEach((key, value) -> {
					// selectedProperties format was changed from array to map.
					// The check for array is added for backward compatibilities with old instances
					if (JsonValue.ValueType.ARRAY.equals(value.getValueType())) {
						result.put(key, JSON.getStringArray((JsonArray) value));
					} else {
						result.put(key, new ArrayList<>(JSON.readObject(value.toString(), JsonObject::keySet)));
					}
				}));
		return result;
	}
	
	static Set<String> getSelectedPropertiesSet(Map<String, List<String>> selectedProperties) {
		Set<String> propertiesSet = new HashSet<>();
		selectedProperties.values().forEach(propertiesSet::addAll);
		return propertiesSet;
	}
	
	/**
	 * Retrieves the selected sub properties for each object property
	 * 
	 * @param requestJson
	 * @return selected sub properties
	 */
	static Map<String, Set<String>> getSelectedSubProperties(JsonObject requestJson) {
		Map<String, Set<String>> result = new HashMap<>();
		JsonObject jsonObject = requestJson.getJsonObject(IdocRenderer.SELECTED_PROPERTIES);

		jsonObject.forEach((key, value) -> {
			// If value type is not an object then the widgets use old model and don't support sub properties
			if (JsonValue.ValueType.OBJECT.equals(value.getValueType())) {
				Map<String, Serializable> jsonAsMap = JSON.toMap((JsonObject) value, false);

				jsonAsMap.values().forEach(propertyNameValues -> {
					String propertyName = ((Map<String, String>) propertyNameValues).get(DefaultProperties.NAME);
					List<Map<String, String>> selectedProperties = (List<Map<String, String>>) ((Map<String, HashMap>) propertyNameValues)
							.get(IdocRenderer.SELECTED_PROPERTIES);

					if (selectedProperties != null) {
						selectedProperties.forEach(subProperty -> CollectionUtils.addValueToSetMap(result, propertyName,
								subProperty.get(DefaultProperties.NAME)));
					}
				});
			}
		});
		return result;
	}

	/**
	 * Gets the instances identifiers.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the instances identifiers
	 */
	static Collection<String> getInstancesIdentifiers(JsonObject jsonConfiguration) {
		JsonObject jsonObject = jsonConfiguration.getJsonObject(SELECTED_PROPERTIES);
		List<String> identifiers = new LinkedList<>();
		for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
			String type = entry.getKey();
			if (!COMMON_PROPERTIES.equalsIgnoreCase(type)) {
				identifiers.add(type);
			}
		}
		return identifiers;
	}

	/**
	 * Check Values for null and return string value or empty string
	 *
	 * @param obj
	 *            the object
	 * @return return string value of object or empty string
	 */
	static String extractValidValue(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}

	/**
	 * Gets the hyperlink from instance header.
	 *
	 * @param instance
	 *            the instance
	 * @param headerType
	 *            the header type
	 * @param ui2url
	 *            the ui2url
	 * @return the hyperlink
	 */
	static String getHyperlink(Instance instance, String headerType, String ui2url) {
		return IdocRenderer.getHyperlink(instance, IdocRenderer.createHeaderTypeConfig(headerType), ui2url);
	}

	/**
	 * Gets the hyperlink from instance header. Used mainly by Datatable and ObjectData widgets.
	 *
	 * @param instance
	 *            the instance
	 * @param headersConfig
	 *            widget configuration for the headers
	 * @param ui2url
	 *            the ui2url
	 * @return the hyperlink
	 */
	static String getHyperlink(Instance instance, Map<String, Object> headersConfig, String ui2url) {
		String headerType = (String) headersConfig.get(INSTANCE_HEADER_TYPE);
		// fallback if header type is set to "none" or any other unsupported header type
		if (!DefaultProperties.HEADERS.contains(headerType)) {
			headerType = DefaultProperties.HEADER_COMPACT;
		}
		Boolean hideIcons = (Boolean) headersConfig.getOrDefault(HIDE_ICONS, false);
		String header = instance.getString(headerType);
		Document fixedHeaderUrls = JsoupUtil.fixHeaderUrls(header, ui2url);
		Elements spans = fixedHeaderUrls.select("body > span");
		if (!spans.isEmpty()) {
			return createHeaderAsTable(spans, headerType, hideIcons);
		}
		fixedHeaderUrls.select("img").addClass(headerType);
		return fixedHeaderUrls.html();
	}

	/**
	 * Proxy method for createHeaderAsTable(Elements spans, String className, boolean shouldHideIcon), where icon is not
	 * hidden. Spans with images will be separated in own table column. For example: Create header as table, where:
	 *
	 * <pre>
	 *  &lt;span&gt;&lt;img src="some image src" /&gt;&lt;/span&gt;
	 *	&lt;span&gt;&lt;a href="some instance href"&gt;&lt;span&gt;Instance Text&lt;/span&gt;&lt;/a&gt;&lt;/span&gt;
	 * </pre>
	 *
	 * is converted to:
	 *
	 * <pre>
	 *  &lt;table&gt;
	 *  	&lt;tr&gt;
	 *  		&lt;td&gt;
	 *  			&lt;span&gt;&lt;img src="some image src" /&gt;&lt;/span&gt;
	 *  		&lt;/td&gt;
	 *  		&lt;td&gt;
	 *				&lt;span&gt;&lt;a href="some instance href"&gt;&lt;span&gt;Instance Text&lt;/span&gt;&lt;/a&gt;&lt;/span&gt;
	 *			&lt;/td&gt;
	 *		&lt;/tr&gt;
	 *	&lt;/table&gt;
	 * </pre>
	 *
	 * Reason to use this method is that docx4j have problem with images align ("left or right"). So we cannot have
	 * image and text right of it but we want headers to look like that. This method build table (without border), add
	 * every span in separated cell and mark td and table with specific CSS classes. This classes will be used to
	 * formatting table cells to look like we want.
	 *
	 * @param spans
	 *            the two spans of header, where in first is image in second is some data or url
	 * @param className
	 *            class which will be added to td tag if it contains image. CSS style will be added before conversion to
	 *            docx.
	 * @return the created table with header
	 */
	static String createHeaderAsTable(Elements spans, String className) {
		return createHeaderAsTable(spans, className, false);
	}

	static String createHeaderAsTable(Elements spans, String className, boolean shouldHideIcon) {
		HtmlTableBuilder table = new HtmlTableBuilder("", true);
		int columnIndex = 0;

		if (shouldHideIcon) {
			spans.select("img").forEach(Element::remove);
		}

		for (int elementsIndex = 0; elementsIndex < spans.size(); elementsIndex++) {
			Element span = spans.get(elementsIndex);

			table.addTdValue(0, columnIndex, new HtmlValueElementBuilder(span));
			table.addTdClass(0, columnIndex, "header-column");
			// Check if span have image on first level (this mean this image is icon of instance)
			// see bugs CMF-23954, CMF-23962
			if (!span.select(" > img").isEmpty()) {
				table.addTdClass(0, columnIndex, className);
				columnIndex++;
			}
		}
		table.addAttribute(JsoupUtil.ATTRIBUTE_BORDER, "0");
		table.addAttribute(JsoupUtil.ATTRIBUTE_WIDTH, "100%");
		return table.build().outerHtml();
	}

	/**
	 * Check configuration for configuration "displayTableHeaderRow".
	 *
	 * @param jsonConfiguration
	 *            the widget configuration.
	 * @return true if header column have t obe shown.
	 */
	static boolean showHeaderColumn(JsonObject jsonConfiguration) {
		return jsonConfiguration.containsKey(DISPLAY_TABLE_HEADER_ROW)
				&& jsonConfiguration.getBoolean(DISPLAY_TABLE_HEADER_ROW);
	}

	/**
	 * Check configuration for "hideIcons" property.
	 *
	 * @param jsonConfiguration
	 *            the widget configuration
	 * @return defaults to false if not specified.
	 */
	static boolean shouldHideIcon(JsonObject jsonConfiguration) {
		return jsonConfiguration.getBoolean(HIDE_ICONS, false);
	}

	/**
	 * Add component after rendering the current.
	 *
	 * @param newElement
	 *            new element that will be render
	 * @param node
	 *            content node
	 */
	void afterRender(Element newElement, ContentNode node);

	/**
	 * Extracts widget configurations specific to its instance headers properties styling.
	 *
	 * @param jsonConfiguration
	 * @return
	 */
	static Map<String, Object> getInstanceHeaderConfig(JsonObject jsonConfiguration) {
		return Collections.checkedMap(
				Stream.of(
						new AbstractMap.SimpleEntry<>(INSTANCE_HEADER_TYPE,
								IdocRenderer.getInstanceHeaderType(jsonConfiguration)),
						new AbstractMap.SimpleEntry<>(HIDE_ICONS, IdocRenderer.shouldHideIcon(jsonConfiguration)),
						new AbstractMap.SimpleEntry<>(INSTANCE_LINK_TYPE,
								IdocRenderer.getInstanceLinkType(jsonConfiguration)))
						.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)),
				String.class, Object.class);
	}

	static String getInstanceLinkType(JsonObject jsonConfiguration) {
		return jsonConfiguration.getString(INSTANCE_LINK_TYPE, DefaultProperties.HEADER_COMPACT);
	}

	/**
	 * Creates a default configuration map populated only with the "instanceHeaderType", as its needed by most widgets.
	 *
	 * @param headerType
	 *            type of header to be initialized.
	 * @return singleton map of specified header type
	 */
	static Map<String, Object> createHeaderTypeConfig(String headerType) {
		return Collections.singletonMap(INSTANCE_HEADER_TYPE, headerType);
	}
}