package com.sirma.sep.export.renders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.jsoup.nodes.Element;

import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueHtmlBuilder;
import com.sirma.sep.export.renders.html.table.HtmlValueTextBuilder;
import com.sirma.sep.export.renders.objects.RowRenderProperty;
import com.sirma.sep.export.renders.objects.RowRenderPropertyType;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * ObjectDataWidget load data and create object-data widget in word document.
 *
 * @author Hristo Lungov
 */
@Extension(target = IdocRenderer.PLUGIN_NAME, order = 3)
public class ObjectDataWidgetRenderer extends BaseRenderer {

	protected static final String OBJECT_DATA_WIDGET_NAME = "object-data-widget";
	private static final String LABEL_POSITION = "labelPosition";
	private static final String LABEL_HIDDEN = "label-hidden";

	@Inject
	private DefinitionHelper definitionHelper;

	@Override
	public boolean accept(ContentNode node) {
		// verify that node is correct widget
		return node.isWidget() && OBJECT_DATA_WIDGET_NAME.equals(((Widget) node).getName());
	}

	@Override
	public Element render(String currentInstanceId, ContentNode node) {
		JsonObject jsonConfiguration = getWidgetConfiguration(node);
		HtmlTableBuilder tableBuilder = renderWidgetFrame(jsonConfiguration);
		
		String selectionMode = IdocRenderer.getSelectionMode(jsonConfiguration);		
		setWidth(tableBuilder, node.getElement(), NIGHTY_NINE_PRECENT);
		if (!IdocRenderer.areWidgetBordersVisible(jsonConfiguration)) {
			tableBuilder.addAttribute(JsoupUtil.ATTRIBUTE_BORDER, "0");
		}
		if (IdocRenderer.AUTOMATICALLY.equalsIgnoreCase(selectionMode) && !hasSearchCriteria(jsonConfiguration)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA);
		}
		// get selected instances
		Collection<Instance> selectedInstances = getSelectedInstances(currentInstanceId, selectionMode, jsonConfiguration);
		return build(jsonConfiguration, tableBuilder, selectedInstances);
	}

	/**
	 * Build table of object data widget.
	 * @param jsonConfiguration configuration of the widget
	 * @param tableBuilder the table builder
	 * @param selectedInstances the instance id.
	 * @return html representation of filled object date widget.
	 */
	private Element build(JsonObject jsonConfiguration, HtmlTableBuilder tableBuilder, Collection<Instance> selectedInstances) {
		// get instance header type
		Map<String,Object> instanceHeaderConfig = IdocRenderer.getInstanceHeaderConfig(jsonConfiguration);

		if (selectedInstances.size() > 1) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_MORE_THAN_ONE);
		}
		// get all selected properties
		Set<String> selectedProperties = IdocRenderer
				.getSelectedPropertiesSet(IdocRenderer.getSelectedProperties(jsonConfiguration));
		// get all data needed to start writing in word document
		Map<String, List<RowRenderProperty>> prepareData = prepareData(selectedInstances, selectedProperties, instanceHeaderConfig);
		if (prepareData.isEmpty()) {
			return buildEmptySelectionTable(IdocRenderer.getSelectionMode(jsonConfiguration), tableBuilder);
		}
		return buildMoreThanOneResultTable(jsonConfiguration, tableBuilder, selectedInstances, instanceHeaderConfig, prepareData);
	}

	/**
	 * Populate table with more than one result from search.
	 *
	 * @param jsonConfiguration 
	 * 			 configuration of widget.
	 * @param tableBuilder 
 				 the table builder.
	 * @param selectedInstances  
	 * 			 selected instances to populate table with.
	 * @param headersConfig 
 				 configuration of the instance headers.
	 * @param preparedData 	
	 * 			 previously prepared data to be appended
	 * 
	 * @return html representation of filled table with more than one result from search.
	 */
	private Element buildMoreThanOneResultTable(JsonObject jsonConfiguration, HtmlTableBuilder tableBuilder,
			Collection<Instance> selectedInstances, Map<String,Object> headersConfig,
			Map<String, List<RowRenderProperty>> preparedData) {
		if (DefaultProperties.DEFAULT_HEADERS.contains(headersConfig.get(INSTANCE_LINK_TYPE))) {
			for (Instance instance : selectedInstances) {
				String hyperlink = IdocRenderer.getHyperlink(instance, headersConfig,
															 systemConfiguration.getUi2Url().get());
				tableBuilder.addTdValue(0, 0, 2, new HtmlValueHtmlBuilder(hyperlink));
			}
		}
		for (Entry<String, List<RowRenderProperty>> entry : preparedData.entrySet()) {
			List<RowRenderProperty> properties = entry.getValue();
			createTableDataRows(tableBuilder, properties, areLabelsHidden(jsonConfiguration));
		}

		return tableBuilder.build();
	}

	/**
	 * Populate table with empty search result.
	 * @param tableBuilder - the table builder.
	 * @return html representation of filled table with empty search result.
	 */
	@Override
	protected Element buildEmptySelectionTable(String selectionMode, HtmlTableBuilder tableBuilder) {
		if (MANUALLY.equals(selectionMode)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECT_OBJECT_NONE);
		} else if (AUTOMATICALLY.equals(selectionMode)) {
			return buildOneRowMessageTable(tableBuilder, KEY_LABEL_SELECT_OBJECT_RESULTS_NONE);
		}
		return tableBuilder.build();
	}

	/**
	 * Creates and fills rows in table with widget data.
	 *
	 * @param tableBuilder
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
			value = IdocRenderer.extractValidValue(renderProperty.getValues().get(VALUE));
			// if firstCell property is hyperlink type add available hyperlinks where each is in new paragraph
			if (renderProperty.isHyperLink()) {
				List<String> hyperLinks = (List<String>) renderProperty.getValues().get(HYPERLINKS);
				// hyperlinks for multivalued fields e.g. Watchers
				for (int j = 0; j < hyperLinks.size(); j++) {
					tableBuilder.addTdValue(trIndex, secondTDIndex, new HtmlValueHtmlBuilder(hyperLinks.get(j)));
				}
			} else if (renderProperty.isHtml()) {
				tableBuilder.addTdValue(trIndex, secondTDIndex, new HtmlValueHtmlBuilder(value));
			} else {
				// just an string value type render property
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
	 * @param headersConfig 
 				 configuration of the instance headers.
	 * @return data for generating docx
	 */
	private Map<String, List<RowRenderProperty>> prepareData(Collection<Instance> instances, Set<String> properties,
			Map<String, Object> headersConfig) {
		return instances.stream().collect(
				Collectors.toMap(getInstanceId(), populateRenderRowProperties(properties, headersConfig)));

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
	 * @param headersConfig 
 				 configuration of the instance headers.
	 * @return the function<? super instance,? extends list< row render property>>
	 */
	private Function<? super Instance, ? extends List<RowRenderProperty>> populateRenderRowProperties(
			Set<String> properties, Map<String, Object> headersConfig) {
		return instance -> fetchPropertyDefinitionOrdered(instance)
				.filter(propertyDefinition -> properties.contains(propertyDefinition.getIdentifier()))
					.map(propertyDefinition ->
						createRowRenderProperty(headersConfig, instance, propertyDefinition)
					)
					.collect(Collectors.toList());
	}

	/**
	 * Creates the row render property.
	 *
	 * @param headersConfig 
 				 configuration of the instance headers.
	 * @param instance the instance
	 * @param propertyDefinition the property definition
	 * @return the row render property
	 */
	private RowRenderProperty createRowRenderProperty(Map<String, Object> headersConfig, Instance instance, PropertyDefinition propertyDefinition) {
		RowRenderProperty renderProperty = new RowRenderProperty(propertyDefinition.getIdentifier(), propertyDefinition.getLabel());
		if (propertyDefinition.getControlDefinition() != null
				&& IdocRenderer.RICHTEXT.equalsIgnoreCase(propertyDefinition.getControlDefinition().getIdentifier())) {
			renderProperty.setType(RowRenderPropertyType.HTML);
		}
		return getPropertyValue(instance, propertyDefinition, renderProperty, new HashSet<>(), headersConfig).get(0);
	}

	/**
	 * Fetch list with property definitions ordered as definition.
	 *
	 * @param instance
	 *            the instance which property definitions have t obe fetched.
	 * @return fetched property definitions.
	 */
	private Stream<PropertyDefinition> fetchPropertyDefinitionOrdered(Instance instance) {
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
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
