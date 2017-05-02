package com.sirma.itt.seip.export.renders;

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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.jsoup.nodes.Element;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.export.renders.objects.RenderProperty;
import com.sirma.itt.seip.export.renders.objects.RowRenderProperty;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueHyperlinkBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueTextBuilder;

/**
 * The Class DatatableWidget represents the iDoc Widget.
 * 
 * @author Hristo Lungov
 */
@ApplicationScoped
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 2)
public class DataTableWidget extends BaseRenderer {

	protected static final String DATA_TABLE_WIDGET_NAME = "datatable-widget";

	@Inject
	private LabelProvider labelProvider;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		if (node.isWidget() && DATA_TABLE_WIDGET_NAME.equals(((Widget) node).getName())) {
			return true;
		}
		return false;
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		Widget widget = (Widget) node;
		WidgetConfiguration configuration = widget.getConfiguration();
		// convert widget configuration to json
		JsonObject jsonConfiguration = IdocRenderer.toJson(configuration);
		// get search selection mode
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);
		// get header type
		String instanceHeaderType = IdocRenderer.getInstanceHeaderType(jsonConfiguration);
		// get selected instances
		Collection<Instance> selectedInstances = getSelectedInstances(currentInstanceId, selectionMode, jsonConfiguration);
		// get all selected properties
		Set<String> selectedProperties = IdocRenderer.getSelectedProperties(jsonConfiguration);
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);
		// get all data needed to start writing in word document
		Map<String, List<RowRenderProperty>> prepareData = prepareData(selectedInstances, selectedProperties, instanceHeaderType);

		Map<String, Integer> columnsOrder = getColumnsOrder(jsonConfiguration);
		
		// create table
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(widgetTitle);
		if (prepareData.isEmpty()) {
			if (MANUALLY.equals(selectionMode)) {
				HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_NONE));
			} else if (AUTOMATICALLY.equals(selectionMode)) {
				HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_RESULTS_NONE));
			}
		} else {
			populateTable(jsonConfiguration, selectedInstances, selectedProperties, prepareData, columnsOrder, tableBuilder);
			HtmlTableBuilder.addTotalResultRow(tableBuilder, prepareData.size() + labelProvider.getLabel(KEY_LABEL_SEARCH_RESULTS));
		}
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		return tableBuilder.build();
	}
	
	/**
	 * Populate <code>tableBuilder</code> with <code>prepareData</code>.
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param prepareData data to be populate into <code>tableBuilder</code>.
	 * @param columnsOrder order of columns it can be empty. If is empty default order will be used.
	 * @param tableBuilder builder where <code>prepareData</code> have to be populated.
	 */
	private void populateTable(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, List<RowRenderProperty>> prepareData,
			Map<String, Integer> columnsOrder, HtmlTableBuilder tableBuilder) {
		
		if (columnsOrder.isEmpty()) {
			populateWithOutColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties, prepareData, tableBuilder);
		} else {
			populateWithColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties, prepareData, columnsOrder, tableBuilder);
		}
	}

	/**
	 * Populate <code>tableBuilder</code> with <code>prepareData</code> and default column order.
	 *
	 * @param jsonConfiguration the configuration of widget.
	 * @param selectedInstances the selected instances.
	 * @param selectedProperties the selected properties.
	 * @param prepareData data to be populate into <code>tableBuilder</code>.
	 * @param tableBuilder builder where <code>prepareData</code> have to be populated.
	 */
	private void populateWithOutColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, List<RowRenderProperty>> prepareData, HtmlTableBuilder tableBuilder) {
		int row = populateHeaderWithOutColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties, tableBuilder) ? 1 : 0;
		// for each instance with properties create row and set data
		for (Entry<String, List<RowRenderProperty>> key : prepareData.entrySet()) {
			List<RowRenderProperty> data = key.getValue();
			for (int cell = 0; cell < data.size(); cell++) {
				fillTable(tableBuilder, row, cell, data.get(cell));
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
	 * @param tableBuilder builder where <code>selectedProperties</code> have to be populated.
	 * @return true if header row is populated.
	 */
	private boolean populateHeaderWithOutColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, HtmlTableBuilder tableBuilder) {
		if (IdocRenderer.showHeaderColumn(jsonConfiguration)) {
			// get widget headings
			List<RenderProperty> widgetHeadings = getWidgetHeadingLabels(jsonConfiguration, selectedInstances, selectedProperties);
			for (int i = 0; i < widgetHeadings.size(); i++) {
				RenderProperty labelProperty = widgetHeadings.get(i);
				String value = IdocRenderer.extractValidValue(labelProperty.getLabel());
				tableBuilder.addTdValue(0, i, new HtmlValueTextBuilder(value, true));
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
	 * @param prepareData data to be populate into <code>tableBuilder</code>.
	 * @param columnsOrder map represent columns order. Key of map is name of property which will be shown in table column and value index of column.
	 * @param tableBuilder tableBuilder builder where <code>prepareData</code> have to be populated.
	 */
	private void populateWithColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, List<RowRenderProperty>> prepareData,
			Map<String, Integer> columnsOrder, HtmlTableBuilder tableBuilder) {
		int row = populateHeaderWithColumnsOrder(jsonConfiguration, selectedInstances, selectedProperties, columnsOrder, tableBuilder) ? 1 : 0;
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
	 * @param columnsOrder map represent columns order key of map is name of property which will be shown in table column and value index of column.
	 * @param tableBuilder tableBuilder builder where <code>selectedProperties</code> have to be populated.
	 * @return true if header row is populated.
	 */
	private boolean populateHeaderWithColumnsOrder(JsonObject jsonConfiguration, Collection<Instance> selectedInstances,
			Set<String> selectedProperties, Map<String, Integer> columnsOrder, HtmlTableBuilder tableBuilder) {
		if (IdocRenderer.showHeaderColumn(jsonConfiguration)) {
			// get widget headings
			List<RenderProperty> widgetHeadings = getWidgetHeadingLabels(jsonConfiguration, selectedInstances, selectedProperties);
			for (int i = 0; i < widgetHeadings.size(); i++) {
				RenderProperty labelProperty = widgetHeadings.get(i);
				String value = IdocRenderer.extractValidValue(labelProperty.getLabel());
				tableBuilder.addTdValue(0, columnsOrder.get(labelProperty.getName()), new HtmlValueTextBuilder(value, true));
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
		if (renderProperty.isHyperLink()) {
			List<String> hyperLinks = (List<String>) renderProperty.getValues().get(HYPERLINKS);
			for (int j = 0; j < hyperLinks.size(); j++) {
				tableBuilder.addTdValue(row, cell, new HtmlValueHyperlinkBuilder(hyperLinks.get(j)));
			}
		} else {
			String value = IdocRenderer.extractValidValue(renderProperty.getValues().get(VALUE));
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
	 * @param instances
	 *            the instances
	 * @param props
	 *            the props
	 * @return the widget headings
	 */
	private List<RenderProperty> getWidgetHeadingLabels(JsonObject jsonConfiguration, Collection<Instance> instances,
			Set<String> props) {
		Collection<String> identifiers = IdocRenderer.getInstancesIdentifiers(jsonConfiguration);
		Set<RenderProperty> pro = getInstancesPropertiesLabels(identifiers, instances, props);
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
	 * @param props
	 *            the properties
	 * @param headerType
	 *            the header type
	 * @return data for generating docx
	 */
	private Map<String, List<RowRenderProperty>> prepareData(Collection<Instance> instances, Set<String> props, String headerType) {
		Map<String, List<RowRenderProperty>> data = new LinkedHashMap<>(instances.size());
		for (Instance instance : instances) {
			List<RowRenderProperty> extractPropertiesData = extractPropertiesData(instance, props, headerType);
			data.put(instance.getId().toString(), extractPropertiesData);
		}
		return data;
	}

}
