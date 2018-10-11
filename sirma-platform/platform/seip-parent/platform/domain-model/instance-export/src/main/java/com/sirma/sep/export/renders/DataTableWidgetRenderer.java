package com.sirma.sep.export.renders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.jsoup.nodes.Element;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueHtmlBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueTextBuilder;
import com.sirma.sep.export.renders.objects.RenderProperty;
import com.sirma.sep.export.renders.objects.RowRenderProperty;

/**
 * The Class DatatableWidget represents the iDoc Widget.
 * 
 * @author Hristo Lungov
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 2)
public class DataTableWidgetRenderer extends BaseRenderer {

	protected static final String DATA_TABLE_WIDGET_NAME = "datatable-widget";
	private static final String NAME = "name";

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		return node.isWidget() && DATA_TABLE_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder table = renderWidgetFrame(jsonConfiguration);
		
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		setWidth(table, node.getElement(), NIGHTY_NINE_PRECENT);
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(selectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(table, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		// get selected instances
		Collection<Instance> selectedInstances = getSelectedInstances(currentInstanceId, selectionMode, jsonConfiguration);
		return buildTable(jsonConfiguration, table, selectedInstances);
	}

	/**
	 * Build table of object data widget.
	 * @param jsonConfiguration widget configuration.
	 * @param tableBuilder the table builder
	 * @param selectedInstances the instance id.
	 * @return html representation of filled object date widget.
	 */
	private Element buildTable(JsonObject jsonConfiguration, HtmlTableBuilder tableBuilder,
			Collection<Instance> selectedInstances) {
		// get header type
		Map<String, Object> instancesHeaderConfig = IdocRenderer.getInstanceHeaderConfig(jsonConfiguration);
		// get all selected properties
		Map<String, List<String>> selectedProperties = IdocRenderer.getSelectedProperties(jsonConfiguration);
		// get all selected sub properties for an object property
		Map<String, Set<String>> selectedSubProperties = IdocRenderer.getSelectedSubProperties(jsonConfiguration);
		// get all data needed to start writing in word document
		Map<String, List<RowRenderProperty>> prepareData = prepareData(selectedInstances, selectedProperties,
				selectedSubProperties, instancesHeaderConfig);

		Map<String, Integer> columnsOrder = getColumnsOrder(jsonConfiguration);

		if (prepareData.isEmpty()) {
			return buildEmptySelectionTable(IdocRenderer.getSelectionMode(jsonConfiguration), tableBuilder);
		}

		populateTable(jsonConfiguration, selectedInstances, IdocRenderer.getSelectedPropertiesSet(selectedProperties),
				selectedSubProperties, prepareData, columnsOrder, tableBuilder);

		addTotalResult(tableBuilder, prepareData.size() + labelProvider.getLabel(KEY_LABEL_SEARCH_RESULTS));

		return tableBuilder.build();
	}

	/**
	 * Add total results inside the title row.
	 *
	 * @param tableBuilder
	 * 		The table builder.
	 * @param value
	 * 		The value to be added.
	 */
	private static void addTotalResult(HtmlTableBuilder tableBuilder, String value){
		if (tableBuilder.isAddHeader()) {
			HtmlValueTextBuilder totalResults = new HtmlValueTextBuilder(value);
			totalResults.addStyle("text-align:right;");
			tableBuilder.getTitleRow().getTd(0).addValue(totalResults);
		}
	}

	/**
	 * Populate <code>tableBuilder</code> with <code>prepareData</code>.
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param selectedSubProperties selected sub properties for an object property
	 * @param prepareData data to be populate into <code>tableBuilder</code>.
	 * @param columnsOrder order of columns it can be empty. If is empty default order will be used.
	 * @param tableBuilder builder where <code>prepareData</code> have to be populated.
	 */
	private void populateTable(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, Set<String>> selectedSubProperties,
			Map<String, List<RowRenderProperty>> prepareData, Map<String, Integer> columnsOrder,
			HtmlTableBuilder tableBuilder) {

		if (columnsOrder.isEmpty()) {
			populateWithOutColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties, selectedSubProperties,
					prepareData, tableBuilder);
		} else {
			populateWithColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties, selectedSubProperties,
					prepareData, columnsOrder, tableBuilder);
		}
	}

	/**
	 * Populate <code>tableBuilder</code> with <code>prepareData</code> and default column order.
	 *
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param selectedSubProperties selected sub properties for an object property
	 * @param prepareData data to be populate into <code>tableBuilder</code>.
	 * @param tableBuilder builder where <code>prepareData</code> have to be populated.
	 */
	private void populateWithOutColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, Set<String>> selectedSubProperties,
			Map<String, List<RowRenderProperty>> prepareData, HtmlTableBuilder tableBuilder) {
		
		Map<String, Integer> headersMap = new HashMap<>();
		if (jsonConfiguration.containsKey(IdocRenderer.COLUMN_HEADER)) {
			JsonArray columnHeaders = jsonConfiguration.getJsonArray(IdocRenderer.COLUMN_HEADER);
			for (int i = 0; i < columnHeaders.size(); i++) {
				headersMap.put(columnHeaders.getJsonObject(i).getString(NAME), i);
			}
		}
		int row = populateHeaderWithOutColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties,
				selectedSubProperties, tableBuilder, headersMap) ? 1 : 0;
		
		// for each instance with properties create row and set data
		for (Entry<String, List<RowRenderProperty>> key : prepareData.entrySet()) {
			List<RowRenderProperty> data = key.getValue();
			for (int cell = 0; cell < data.size(); cell++) {
				RowRenderProperty renderProperty = data.get(cell);
				String name = renderProperty.getName();
				if (headersMap.get(name) != null) {
					fillTable(tableBuilder, row, headersMap.get(name), renderProperty);
				} else {
					fillTable(tableBuilder, row, cell, renderProperty);
				}
			}
			row++;
		}
	}

	/**
	 * Check configuration if header row have to be populated. If yes will populate header with <code>selectedProperties</code> and default column order.
	 *
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param selectedSubProperties selected sub properties for an object property
	 * @param tableBuilder builder where <code>selectedProperties</code> have to be populated.
	 * @param headersMap map with headers order if available
	 * @return true if header row is populated.
	 */
	private boolean populateHeaderWithOutColumnsOrder(JsonObject jsonConfiguration,
			Collection<Instance> selectedInstances, Set<String> selectedProperties,
			Map<String, Set<String>> selectedSubProperties, HtmlTableBuilder tableBuilder, Map<String, Integer> headersMap) {
		if (IdocRenderer.showHeaderColumn(jsonConfiguration)) {
			// get widget headings
			List<RenderProperty> widgetHeadings = getWidgetHeadingLabels(jsonConfiguration, selectedInstances,
					selectedProperties, selectedSubProperties);
			for (int i = 0; i < widgetHeadings.size(); i++) {
				RenderProperty labelProperty = widgetHeadings.get(i);
				String value = IdocRenderer.extractValidValue(labelProperty.getLabel());
				if (headersMap.get(labelProperty.getName()) != null) {
					tableBuilder.addTdValue(0, headersMap.get(labelProperty.getName()),
						new HtmlValueTextBuilder(value, true));
				} else {
					tableBuilder.addTdValue(0, i, new HtmlValueTextBuilder(value, true));
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 *  Populate <code>tableBuilder</code> with <code>prepareData</code> and column order (<code>columnsOrder</code>).
	 *
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param selectedSubProperties selected sub properties for an object property
	 * @param prepareData data to be populate into <code>tableBuilder</code>.
	 * @param columnsOrder map represent columns order. Key of map is name of property which will be shown in table column and value index of column.
	 * @param tableBuilder tableBuilder builder where <code>prepareData</code> have to be populated.
	 */
	private void populateWithColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, Set<String>> selectedSubProperties,
			Map<String, List<RowRenderProperty>> prepareData, Map<String, Integer> columnsOrder,
			HtmlTableBuilder tableBuilder) {
		int row = populateHeaderWithColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties,
				selectedSubProperties, columnsOrder, tableBuilder) ? 1 : 0;
		// for each instance with properties create row and set data
		for (Entry<String, List<RowRenderProperty>> key : prepareData.entrySet()) {
			List<RowRenderProperty> data = key.getValue();
			for (int cell = 0; cell < data.size(); cell++) {
				RowRenderProperty renderProperty = data.get(cell);
				String name = renderProperty.getName();
				fillTable(tableBuilder, row, columnsOrder.get(name), renderProperty);
			}
			row++;
		}
	}

	/**
	 *  Check configuration if header row have to be populated. If yes will populate header with <code>selectedProperties</code> and column order (<code>columnsOrder</code>).
	 *
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param selectedSubProperties selected sub properties for an object property
	 * @param columnsOrder map represent columns order key of map is name of property which will be shown in table column and value index of column.
	 * @param tableBuilder tableBuilder builder where <code>selectedProperties</code> have to be populated.
	 * @return true if header row is populated.
	 */
	private boolean populateHeaderWithColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, Set<String>> selectedSubProperties,
			Map<String, Integer> columnsOrder, HtmlTableBuilder tableBuilder) {
		if (IdocRenderer.showHeaderColumn(jsonConfiguration)) {
			// get widget headings
			List<RenderProperty> widgetHeadings = getWidgetHeadingLabels(jsonConfiguration, selectedInstances,
					selectedProperties, selectedSubProperties);
			for (int i = 0; i < widgetHeadings.size(); i++) {
				RenderProperty labelProperty = widgetHeadings.get(i);
				String value = IdocRenderer.extractValidValue(labelProperty.getLabel());
				tableBuilder.addTdValue(0, columnsOrder.get(labelProperty.getName()),
						new HtmlValueTextBuilder(value, true));
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Add table builder rows/cells with values from RenderProperty.
	 * 
	 * @see RowRenderProperty
	 * @param tableBuilder
	 *            the table builder
	 * @param row
	 *            current row of table
	 * @param cell
	 *            current cell of table
	 * @param renderProperty
	 *            property with cell data
	 */
	private static void fillTable(HtmlTableBuilder tableBuilder, int row, int cell, RowRenderProperty renderProperty) {
		String value = IdocRenderer.extractValidValue(renderProperty.getValues().get(VALUE));
		if (renderProperty.isHyperLink()) {
			List<String> hyperLinks = (List<String>) renderProperty.getValues().get(HYPERLINKS);
			for (int j = 0; j < hyperLinks.size(); j++) {
				tableBuilder.addTdValue(row, cell, new HtmlValueHtmlBuilder(hyperLinks.get(j)));
			}
		} else if (renderProperty.isHtml()) {
			tableBuilder.addTdValue(row, cell, new HtmlValueHtmlBuilder(value));
		} else {
			tableBuilder.addTdValue(row, cell, new HtmlValueTextBuilder(value));
		}
	}
	
	/**
	 * Extract columns order from configuration.
	 *
	 * @param jsonConfiguration the widget configuration.
	 * @return  map represent columns order. Key of map is name of property which will be shown in table column and value index of column.
	 */
	private static Map<String, Integer> getColumnsOrder(JsonObject jsonConfiguration) {
		Map<String, Integer> result = new HashMap<>();
		if (jsonConfiguration.containsKey(COLUMN_ORDER)) {
			JsonObject columnOrder = jsonConfiguration.getJsonObject(COLUMN_ORDER);
			JsonObject columns = columnOrder.getJsonObject("columns");
			for (Entry<String, JsonValue> column: columns.entrySet()) {
				JsonObject indexValue = (JsonObject) column.getValue();
				result.put(column.getKey(), indexValue.getInt("index"));
			}
		}
		return result;
	}

	/**
	 * Gets the selected instances from widget json configuration.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances
	 */
	private Collection<Instance> getSelectedInstances(String currentInstanceId, String selectionMode, JsonObject jsonConfiguration) {
		if (CURRENT.equals(selectionMode)) {
			return loadInstances(Arrays.asList(currentInstanceId));
		}
		if (MANUALLY.equals(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_OBJECTS)) {
				List<Serializable> selectedObjects = JSON.jsonToList(jsonConfiguration.getJsonArray(SELECTED_OBJECTS));
				return loadInstances(selectedObjects);
			}
			return Collections.emptyList();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration);
	}

	/**
	 * Gets the widget properties headings labels.
	 *
	 * @param jsonConfiguration
	 *            widget configuration
	 * @param instances
	 *            the instances
	 * @param props
	 *            the props
	 * @param selectedSubProperties
	 *            selected sub properties for an object property
	 * @return the widget headings
	 */
	private List<RenderProperty> getWidgetHeadingLabels(JsonObject jsonConfiguration, Collection<Instance> instances,
			Set<String> props, Map<String, Set<String>> selectedSubProperties) {
		Collection<String> identifiers = IdocRenderer.getInstancesIdentifiers(jsonConfiguration);
		Set<RenderProperty> pro = getInstancesPropertiesLabels(jsonConfiguration, identifiers, instances, props,
				selectedSubProperties);
		List<RenderProperty> headings = new LinkedList<>();
		String instanceHeaderType = IdocRenderer.getInstanceHeaderType(jsonConfiguration);
		if (jsonConfiguration.containsKey("instanceHeaderType")
				&& DefaultProperties.DEFAULT_HEADERS.contains(instanceHeaderType)) {
			headings.add(new RenderProperty(IdocRenderer.getInstanceHeaderType(jsonConfiguration), ENTITY_LABEL));
		}
		headings.addAll(pro);
		return headings;
	}

	/**
	 * Prepares data which will be exported.
	 *
	 * @param instances
	 *            list of instances
	 * @param selectedProperties
	 *            the properties
	 * @param selectedSubProperties
	 *            selected sub properties for an object property
	 * @param headersConfig
	 *            the header configuration of the widget
	 * @return data for generating docx
	 */
	private Map<String, List<RowRenderProperty>> prepareData(Collection<Instance> instances, Map<String, List<String>> selectedProperties,
			Map<String, Set<String>> selectedSubProperties, Map<String, Object> headersConfig) {
		Map<String, List<RowRenderProperty>> data = new LinkedHashMap<>(instances.size());
		for (Instance instance : instances) {
			List<RowRenderProperty> extractPropertiesData = extractPropertiesData(instance, selectedProperties,
					selectedSubProperties, headersConfig);
			data.put(instance.getId().toString(), extractPropertiesData);
		}
		return data;
	}

}
