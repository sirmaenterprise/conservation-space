package com.sirma.itt.seip.export.renders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.jsoup.nodes.Element;

import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.export.renders.objects.RowRenderProperty;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Widget;
import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueHyperlinkBuilder;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlValueTextBuilder;

/**
 * ObjectDataWidget load data and create object-data widget in word document.
 * 
 * @author Hristo Lungov
 */
@ApplicationScoped
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 3)
public class ObjectDataWidget extends BaseRenderer {

	protected static final String OBJECT_DATA_WIDGET_NAME = "object-data-widget";
	private static final String LABEL_POSITION = "labelPosition";
	private static final String LABEL_HIDDEN = "label-hidden";

	@Inject
	private LabelProvider labelProvider;
	
	@Inject
	private DefinitionHelper definitionHelper;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		if (node.isWidget() && OBJECT_DATA_WIDGET_NAME.equals(((Widget) node).getName())) {
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
		// get instance header type
		String instanceHeader = jsonConfiguration.getString("instanceLinkType", "");
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(jsonConfiguration);
		// create table
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder(widgetTitle);
		if (!IdocRenderer.areWidgetBordersVisible(jsonConfiguration)) {
			tableBuilder.addAttribute(JsoupUtil.ATTRIBUTE_BORDER, "0");
		}
		// get selected instances
		Collection<Instance> selectedInstances = getSelectedInstances(currentInstanceId, selectionMode,
				jsonConfiguration);
		if (selectedInstances.size() > 1) {
			HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_MORE_THAN_ONE));
			setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
			return tableBuilder.build();
		}
		// get all selected properties
		Set<String> selectedProperties = IdocRenderer.getSelectedProperties(jsonConfiguration);
		// get all data needed to start writing in word document
		Map<String, List<RowRenderProperty>> prepareData = prepareData(selectedInstances, selectedProperties, instanceHeader);
		if (prepareData.isEmpty()) {
			if (MANUALLY.equals(selectionMode)) {
				HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_NONE));
			} else if (AUTOMATICALLY.equals(selectionMode)) {
				HtmlTableBuilder.addNoResultRow(tableBuilder,
						labelProvider.getLabel(KEY_LABEL_SELECT_OBJECT_RESULTS_NONE));
			}
		} else {
			if (DefaultProperties.DEFAULT_HEADERS.contains(instanceHeader)) {
				for (Instance instance : selectedInstances) {
					String hyperlink = IdocRenderer.getHyperlink(instance, instanceHeader,
							systemConfiguration.getUi2Url().get());
					tableBuilder.addTdValue(0, 0, 2, new HtmlValueHyperlinkBuilder(hyperlink));
				}
			}
			for (Entry<String, List<RowRenderProperty>> entry : prepareData.entrySet()) {
				List<RowRenderProperty> properties = entry.getValue();
				createTableDataRows(tableBuilder, properties, areLabelsHidden(jsonConfiguration));
			}
		}

		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		return tableBuilder.build();
	}

	/**
	 * Creates and fills rows in table with widget data.
	 *
	 * @param table
	 *            the word document table representing the widget
	 * @param properties
	 *            the properties for filling each row
	 */
	private static void createTableDataRows(HtmlTableBuilder tableBuilder, List<RowRenderProperty> properties,
			boolean areLabelsHidden) {
		for (int i = 0; i < properties.size(); i++) {
			int trIndex = tableBuilder.getRowCount();
			int secondTDIndex = areLabelsHidden ? 0 : 1;
			RowRenderProperty renderProperty = properties.get(i);
			if (renderProperty.isHidden()) {
				continue;
			}
			String value = IdocRenderer.extractValidValue(renderProperty.getLabel());
			if (!areLabelsHidden) {
				tableBuilder.addTdValue(trIndex, 0, new HtmlValueTextBuilder(value));
			}
			// iffirstCellroperty is hyperlink type add available hyperlinks where each is in new paragraph
			if (renderProperty.isHyperLink()) {
				List<String> hyperLinks = (List<String>) renderProperty.getValues().get(HYPERLINKS);
				// hyperlinks for multivalued fields e.g. Watchers
				for (int j = 0; j < hyperLinks.size(); j++) {
					tableBuilder.addTdValue(trIndex, secondTDIndex, new HtmlValueHyperlinkBuilder(hyperLinks.get(j)));
				}
			} else {
				// just an string value type render property
				value = IdocRenderer.extractValidValue(renderProperty.getValues().get(VALUE));
				tableBuilder.addTdValue(trIndex, secondTDIndex, new HtmlValueTextBuilder(value));
			}
		}
	}

	/**
	 * Prepares data which will be exported to word.
	 *
	 * @param instances
	 *            list of instances
	 * @param properties
	 *            the properties from widget configuration
	 * @param headerType
	 *            the header type of the widget
	 * @return data for generating docx
	 */
	private Map<String, List<RowRenderProperty>> prepareData(Collection<Instance> instances, Set<String> properties,
			String headerType) {
		return instances.stream().collect(
				Collectors.toMap(getInstanceId(), populateRenderRowProperties(properties, headerType)));

	}

	/**
	 * Gets the instance id.
	 *
	 * @return the instanceId of instance.
	 */
	private static Function<? super Instance, ? extends String> getInstanceId() {
		return instance -> instance.getId().toString();
	}

	/**
	 * Populate properties of instance.
	 *
	 * @param properties
	 *            the properties
	 * @param headerType
	 *            the header type.
	 * @return the function<? super instance,? extends list< row render property>>
	 */
	private Function<? super Instance, ? extends List<RowRenderProperty>> populateRenderRowProperties(
			Set<String> properties, String headerType) {
		return instance -> fetchPropertyDefinitionOrdered(instance)
				.filter(propertyDefinition -> properties.contains(propertyDefinition.getIdentifier()))
					.map(propertyDefinition -> 
						createRowRenderProperty(headerType, instance, propertyDefinition)
					)
					.collect(Collectors.toList());
	}

	/**
	 * Creates the row render property.
	 *
	 * @param headerType  the header type of the widget
	 * @param instance the instance
	 * @param propertyDefinition the property definition
	 * @return the row render property
	 */
	private RowRenderProperty createRowRenderProperty(String headerType, Instance instance, PropertyDefinition propertyDefinition) {
		RowRenderProperty renderProperty = new RowRenderProperty(propertyDefinition.getIdentifier(), propertyDefinition.getLabel());
		return getPropertyValue(instance, propertyDefinition, renderProperty, headerType);
	}

	/**
	 * Fetch list with property definitions ordered as definition.
	 *
	 * @param instance
	 *            the instance which property definitions have t obe fetched.
	 * @return fetched property definitions.
	 */
	private Stream<PropertyDefinition> fetchPropertyDefinitionOrdered(Instance instance) {
		DefinitionModel instanceDefinition = dictionaryService.getInstanceDefinition(instance);
		return definitionHelper.collectAllFields(instanceDefinition).stream().flatMap(
				field -> extractPropertyDefinition(instance, field));

	}

	/**
	 * If <code>field</code> is PropertyDefinition will be added to <code>orderedPropertyDefinitions</code>. If
	 * <code>field</code> is RegionDefinition all property definitions will be added to
	 * <code>orderedPropertyDefinitions</code>.
	 *
	 * @param instance
	 *            the instance.
	 * @param field
	 *            the field.
	 */
	private Stream<PropertyDefinition> extractPropertyDefinition(Instance instance, Ordinal field) {
		if (field instanceof PropertyDefinition) {
			return Stream.of((PropertyDefinition) field);
		} 
		if (field instanceof RegionDefinition && !checkHiddenRegionCondition(instance, (RegionDefinition) field)) {
			return ((RegionDefinition) field).getFields().stream();
		}
		return Stream.empty();
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
	private Collection<Instance> getSelectedInstances(String currentInstanceId, String selectionMode,
			JsonObject jsonConfiguration) {
		if (CURRENT.equals(selectionMode)) {
			return loadInstances(Arrays.asList(currentInstanceId));
		}
		if (MANUALLY.equals(selectionMode)) {
			if (jsonConfiguration.containsKey(SELECTED_OBJECT)) {
				Collection<Serializable> selectedObjects = Collections
						.singleton(jsonConfiguration.getString(SELECTED_OBJECT));
				return loadInstances(selectedObjects);
			}
			return Collections.emptyList();
		}
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration);
	}

	private static boolean areLabelsHidden(JsonObject jsonConfiguration) {
		return LABEL_HIDDEN.equalsIgnoreCase(jsonConfiguration.getString(LABEL_POSITION, ""));
	}

}
