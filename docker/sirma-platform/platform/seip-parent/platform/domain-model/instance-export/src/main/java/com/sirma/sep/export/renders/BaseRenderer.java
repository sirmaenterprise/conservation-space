package com.sirma.sep.export.renders;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.search.converters.JsonToDateRangeConverter;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.export.renders.html.table.HtmlTableBuilder;
import com.sirma.sep.export.renders.objects.RenderProperty;
import com.sirma.sep.export.renders.objects.RowRenderProperty;
import com.sirma.sep.export.renders.objects.RowRenderPropertyType;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Abstract class used to supply common methods for idoc renderers.
 *
 * @author Hristo Lungov
 */
public abstract class BaseRenderer implements IdocRenderer {

	@Inject
	protected SearchService searchService;

	@Inject
	protected InstanceTypeResolver instanceResolver;

	@Inject
	protected CodelistService codelistService;

	@Inject
	protected InstanceLoadDecorator instanceDecorator;

	@Inject
	protected SystemConfiguration systemConfiguration;

	@Inject
	protected JsonToConditionConverter converter;

	@Inject
	protected DefinitionService definitionService;

	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected JsonToDateRangeConverter jsonToDateRangeConverter;

	@Inject
	protected SemanticDefinitionService semanticDefinitionService;

	@Inject
	protected NamespaceRegistryService namespaceRegistryService;

	@Inject
	protected TypeMappingProvider typeProvider;

	@Inject
	private ConditionsManager conditionsManager;

	@Inject
	protected LabelProvider labelProvider;

	/**
	 * Loads instances by given list of ids.
	 *
	 * @param selectedInstances
	 *            the ids
	 * @return list of instances
	 */
	protected Collection<Instance> loadInstances(Collection<Serializable> selectedInstances) {
		return loadInstances(selectedInstances, true);
	}

	/**
	 * Loads instances by given list of ids.
	 *
	 * @param selectedInstances
	 *            the selected instances
	 * @param decorate
	 *            to decorate found instance or not
	 * @return list of instances
	 */
	protected Collection<Instance> loadInstances(Collection<Serializable> selectedInstances, boolean decorate) {
		Collection<Instance> instances = instanceResolver.resolveInstances(selectedInstances);
		if (decorate) {
			instanceDecorator.decorateResult(instances);
		}
		return instances;
	}

	/**
	 * Extract properties data for instance.
	 *
	 * @param instance
	 *            the instance
	 * @param selectedProperties
	 *            the properties
	 * @param selectedSubProperties
	 *            selected sub properties for an object property
	 * @param headersConfig
	 *            the header configuration of the widget
	 * @return the list
	 */
	protected List<RowRenderProperty> extractPropertiesData(Instance instance, Map<String, List<String>> selectedProperties,
			Map<String, Set<String>> selectedSubProperties, Map<String, Object> headersConfig) {
		String headerType = (String) headersConfig.get(INSTANCE_HEADER_TYPE);

		List<RowRenderProperty> propertyValues = new LinkedList<>();
		if (StringUtils.isNotBlank(headerType) && DefaultProperties.DEFAULT_HEADERS.contains(headerType)) {
			RowRenderProperty sanitizedHeader = sanitizeHeaderRenderProperty(instance, headersConfig);
			propertyValues.add(sanitizedHeader);
		}
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		// Render properties for given definition. This is used when instances are selected manually
		List<String> propertiesToRender = selectedProperties.get(instanceDefinition.getIdentifier());
		if (propertiesToRender == null) {
			// Render properties for given class. This is used when instances are selected automatically
			// but type is known (for example Project, Case etc.)
			propertiesToRender = selectedProperties.get(instance.type().getId());
		}
		if (propertiesToRender == null || propertiesToRender.isEmpty()) {
			// Render all selected properties. This is used when instances are selected automatically
			// but type is unknown (for example object type is too common like CulturalObject or no object type is selected
			propertiesToRender = new ArrayList<>(IdocRenderer.getSelectedPropertiesSet(selectedProperties));
		}
		for (String property : propertiesToRender) {
			propertyValues.addAll(createRenderProperty(instance, instanceDefinition, property,
					getPropertySubProperties(selectedSubProperties, property), headersConfig));
		}
		return propertyValues;
	}

	private static Set<String> getPropertySubProperties(Map<String, Set<String>> selectedSubProperties,
			String property) {
		Set<String> subProperties = selectedSubProperties.get(property);
		return subProperties != null ? subProperties : Collections.emptySet();
	}

	/**
	 * Create RenderProperty with filled data based on property of instance.
	 *
	 * @param instance
	 *            the instance
	 * @param instanceDefinition
	 *            the current instance definition
	 * @param property
	 *            the property
	 * @param selectedSubProperties
	 *            selected sub properties for an object property
	 * @param headersConfig
	 *            the header configuration of the widget
	 */
	protected List<RowRenderProperty> createRenderProperty(Instance instance, DefinitionModel instanceDefinition,
			String property, Set<String> selectedSubProperties, Map<String, Object> headersConfig) {
		PropertyDefinition propertyDefinition = instanceDefinition.getField(property).orElse(null);
		if (propertyDefinition != null) {
			RowRenderProperty renderProperty = new RowRenderProperty(property, propertyDefinition.getLabel());
			if (propertyDefinition.getControlDefinition() != null && IdocRenderer.RICHTEXT
					.equalsIgnoreCase(propertyDefinition.getControlDefinition().getIdentifier())) {
				renderProperty.setType(RowRenderPropertyType.HTML);
			}
			return getPropertyValue(instance, propertyDefinition, renderProperty, selectedSubProperties, headersConfig);
		}
		RowRenderProperty renderProperty = new RowRenderProperty(property, property);
		renderProperty.setType(RowRenderPropertyType.HIDDEN);
		renderProperty.addValue(VALUE, "");
		return Collections.singletonList(renderProperty);
	}

	/**
	 * Prepare links from passed instances.
	 *
	 * @param instances
	 *            the instances in cell
	 * @param renderProperty
	 *            the render property
	 * @param headersConfig
	 *            the header configuration of the widget
	 */
	protected void prepareInstancesLinks(Collection<Instance> instances, RowRenderProperty renderProperty,
			Map<String, Object> headersConfig) {
		ArrayList<String> hyperLinks = new ArrayList<>(instances.size());
		headersConfig.computeIfAbsent(INSTANCE_HEADER_TYPE, key -> headersConfig.put(key, DefaultProperties.HEADER_COMPACT));
		
		for (Instance propertyInstance : instances) {
			hyperLinks.add(
					IdocRenderer.getHyperlink(propertyInstance, headersConfig, systemConfiguration.getUi2Url().get()));
		}
		renderProperty.addValue(HYPERLINKS, hyperLinks);
		renderProperty.setType(RowRenderPropertyType.HYPERLINK);
	}

	/**
	 * Gets the property value from instance and check its type in property definition to get it correct.
	 *
	 * @param instance
	 *            the instance
	 * @param propertyDefinition
	 *            the definition property
	 * @param renderProperty
	 *            the render property
	 * @param selectedSubProperties
	 *            selected sub properties for an object property
	 * @param headersConfig
	 *            the header configuration of the widget
	 * @return the property value
	 */
	protected List<RowRenderProperty> getPropertyValue(Instance instance, PropertyDefinition propertyDefinition,
			RowRenderProperty renderProperty, Set<String> selectedSubProperties, Map<String, Object> headersConfig) {

		List<RowRenderProperty> renderedProperties = new LinkedList<>();
		List<RowRenderProperty> renderedSubProperties = new LinkedList<>();
		Serializable propertyValue = instance.get(propertyDefinition.getIdentifier());
		if (propertyValue != null) {
			propertyValue = getValueByType(propertyValue, propertyDefinition);
			if (PropertyDefinition.isObjectProperty().test(propertyDefinition)) {
				Collection<Instance> loadedInstances = loadInstances(getPropertyIds(propertyValue));
				prepareInstancesLinks(loadedInstances, renderProperty, headersConfig);
				getSubPropertiesValue(renderProperty, renderedSubProperties, selectedSubProperties, loadedInstances,
						headersConfig);
			}
		} else {
			for (String subProperty : selectedSubProperties) {
				populateSubPropertyValue(subProperty, "", renderedSubProperties);
			}
		}
		checkHiddenConditions(instance, propertyDefinition, renderProperty, propertyValue);
		renderProperty.addValue(VALUE, propertyValue);
		renderedProperties.add(renderProperty);
		renderedProperties.addAll(renderedSubProperties);
		return renderedProperties;
	}

	private Serializable getValueByType(Serializable propertyValue, PropertyDefinition propertyDefinition) {
		Serializable value = propertyValue;
		if (PropertyDefinition.hasCodelist().test(propertyDefinition)) {
			value = extractCodelistValue(propertyDefinition, propertyValue);
		} else if (PropertyDefinition.hasType(DataTypeDefinition.DATETIME).test(propertyDefinition)) {
			FormattedDateTime formattedDateTime = typeConverter.convert(FormattedDateTime.class, propertyValue);
			value = formattedDateTime.getFormatted();
		} else if (PropertyDefinition.hasType(DataTypeDefinition.DATE).test(propertyDefinition)) {
			FormattedDate formattedDateTime = typeConverter.convert(FormattedDate.class, propertyValue);
			value = formattedDateTime.getFormatted();
		}
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		if (controlDefinition != null) {
			value = getControlDefinitionLabel(propertyValue, controlDefinition);
		}
		return value;
	}

	private Serializable extractCodelistValue(PropertyDefinition propertyDefinition, Serializable propertyValue) {
		Integer codelist = propertyDefinition.getCodelist();
		if (propertyDefinition.isMultiValued().booleanValue()) {
			return processCodelistMultiValues(propertyValue, codelist);
		}
		return codelistService.getDescription(codelist, propertyValue.toString());
	}

	private void getSubPropertiesValue(RowRenderProperty renderProperty, List<RowRenderProperty> renderedSubProperties,
			Set<String> selectedSubProperties, Collection<Instance> instances, Map<String, Object> headersConfig) {
		if (instances.size() == 1) {
			Instance loadedInstance = instances.iterator().next();
			DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(loadedInstance);
			for (String subProperty : selectedSubProperties) {
				boolean isObjectSubproperty = instanceDefinition.getField(subProperty)
						.filter(PropertyDefinition.isObjectProperty()).isPresent();
				String subPropertyKey = renderProperty.getName() + IdocRenderer.SEPARATOR + subProperty;
				if (isObjectSubproperty) {
					Collection<Instance> loadedInstances = loadInstances(
							loadedInstance.getAsCollection(subProperty, LinkedList::new));
					RowRenderProperty renderedSubProperty = new RowRenderProperty(subPropertyKey,
							loadedInstance.getAsString(subProperty));
					prepareInstancesLinks(loadedInstances, renderedSubProperty, headersConfig);
					renderedSubProperties.add(renderedSubProperty);
				} else {
					PropertyDefinition propertyDefinition = instanceDefinition.getField(subProperty).orElse(null);
					Serializable subPropertyValue = loadedInstance.get(subProperty);
					if (propertyDefinition != null && subPropertyValue != null) {
						subPropertyValue = getValueByType(subPropertyValue, propertyDefinition);
					}
					populateSubPropertyValue(subPropertyKey, (String) subPropertyValue, renderedSubProperties);
				}
			}
		} else {
			for (String subProperty : selectedSubProperties) {
				String subPropertyKey = renderProperty.getName() + IdocRenderer.SEPARATOR + subProperty;
				populateSubPropertyValue(subPropertyKey,
						labelProvider.getLabel(IdocRenderer.KEY_LABEL_MULTIPLE_OBJECTS), renderedSubProperties);
			}
		}
	}

	private static void populateSubPropertyValue(String subProperty, String value,
			List<RowRenderProperty> renderedSubProperties) {
		RowRenderProperty renderedSubProperty = new RowRenderProperty(subProperty, value);
		renderedSubProperty.addValue(VALUE, value);
		renderedSubProperties.add(renderedSubProperty);
	}

	private static Collection<Serializable> getPropertyIds(Serializable propertyValue) {
		if (propertyValue instanceof Collection<?>) {
			return (Collection<Serializable>) propertyValue;
		}
		return Collections.singletonList(propertyValue);
	}

	private void checkHiddenConditions(Instance instance, PropertyDefinition propertyDefinition,
			RowRenderProperty renderProperty, Serializable propertyValue) {
		// hidden properties have to be visible in preview system never.
		if (DisplayType.SYSTEM.equals(propertyDefinition.getDisplayType())) {
			renderProperty.setType(RowRenderPropertyType.HIDDEN);
		} else if (propertyValue == null && !propertyDefinition.isPreviewEnabled()) {
			renderProperty.setType(RowRenderPropertyType.HIDDEN);
		} else if (conditionsManager.evalPropertyConditions(propertyDefinition, ConditionType.HIDDEN, instance)) {
			renderProperty.setType(RowRenderPropertyType.HIDDEN);
		}
	}

	/**
	 * Check condition if region is hidden.
	 *
	 * @param instance
	 *            the instance.
	 * @param regionDefinition
	 *            the region definition.
	 * @return true, region is hidden.
	 */
	protected boolean checkHiddenRegionCondition(Instance instance, RegionDefinition regionDefinition) {
		return conditionsManager.evalPropertyConditions(regionDefinition, ConditionType.HIDDEN, instance);
	}

	/**
	 * Gets the control definition label.
	 *
	 * @param propertyValue
	 *            the property value
	 * @param controlDefinition
	 *            the control definition
	 * @return the control definition label
	 */
	public static Serializable getControlDefinitionLabel(Serializable propertyValue,
			ControlDefinition controlDefinition) {
		Optional<PropertyDefinition> field = controlDefinition.getField(propertyValue.toString());
		if (field.isPresent()) {
			return field.get().getLabel();
		}
		return propertyValue;
	}

	/**
	 * Gets the selected instances from criteria because no default selected.
	 *
	 * @param currentInstanceId
	 *            the current instance id
	 * @param jsonConfiguration
	 *            the json configuration
	 * @param load
	 *            to load or not searched instances
	 * @return the selected instances from criteria
	 */
	protected List<Instance> getSelectedInstancesFromCriteria(String currentInstanceId, JsonObject jsonConfiguration,
			boolean load) {
		JsonObject criteria = jsonConfiguration.getJsonObject(SEARCH_CRITERIA);
		Condition tree = converter.parseCondition(criteria);
		ConditionBuilder builder = SearchCriteriaBuilder.createConditionBuilder().from(tree);
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(builder, tree);
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(tree.getRules().size()));
		searchRequest.setSearchTree(builder.build());
		SearchArguments<Instance> searchArgs = searchService.parseRequest(searchRequest);
		String newQuery = searchArgs.getStringQuery().replace("current_object", currentInstanceId);
		searchArgs.setStringQuery(newQuery);
		String orderBy = jsonConfiguration.getString("orderBy",
				EMF.PREFIX + SPARQLQueryHelper.URI_SEPARATOR + EMF.MODIFIED_ON.getLocalName());
		String orderDirection = jsonConfiguration.getString("orderDirection", "desc");
		Sorter sorter = Sorter.buildSorterFromConfig(orderBy + "|" + orderDirection);
		searchArgs.getSorters().clear();
		searchArgs.getSorters().add(sorter);
		searchArgs.setPageSize(searchArgs.getMaxSize());
		searchArgs.setPageNumber(1);
		if (load) {
			searchService.searchAndLoad(Instance.class, searchArgs);
		} else {
			searchService.search(Instance.class, searchArgs);
		}
		return searchArgs.getResult();
	}

	/**
	 * Gets and load the selected instances from criteria because no default selected.
	 *
	 * @param jsonConfiguration
	 *            the json configuration
	 * @return the selected instances from criteria
	 */
	protected List<Instance> getSelectedInstancesFromCriteria(String currentInstanceId, JsonObject jsonConfiguration) {
		return getSelectedInstancesFromCriteria(currentInstanceId, jsonConfiguration, true);
	}

	/**
	 * Process codelist multi values with default system line separator.
	 *
	 * @param propertyValue
	 *            the property value
	 * @param codelist
	 *            the codelist
	 * @return the serializable
	 */
	protected Serializable processCodelistMultiValues(Serializable propertyValue, Integer codelist) {
		return processCodelistMultiValues(propertyValue, codelist, ", ");
	}

	/**
	 * Used for concatination of multivalued codelist fields.
	 *
	 * @param propertyValue
	 *            the property value
	 * @param codelist
	 *            the codelist
	 * @param separator
	 *            the separator between each codelist value
	 * @return the serializable
	 */
	protected Serializable processCodelistMultiValues(Serializable propertyValue, Integer codelist, String separator) {
		Collection<String> codelistValues = (Collection<String>) propertyValue;
		return codelistValues.stream().map(clValue -> codelistService.getDescription(codelist, clValue)).collect(
				Collectors.joining(separator));
	}

	/**
	 * Get from instances definition model the specified property label. Note: if label not found will not be added in
	 * result. Also it returns set because some of properties may be or may not be available for current instance.
	 *
	 * @param jsonConfiguration
	 *            widget configuration
	 * @param identifiers
	 *            the identifiers
	 * @param instances
	 *            list of instances
	 * @param properties
	 *            list of properties
	 * @param selectedSubProperties
	 *            selected sub properties for an object property
	 * @return set of {@link RenderProperty} represented label and name of property.
	 */
	protected Set<RenderProperty> getInstancesPropertiesLabels(JsonObject jsonConfiguration,
			Collection<String> identifiers, Collection<Instance> instances, Set<String> properties,
			Map<String, Set<String>> selectedSubProperties) {

		Map<String, Set<String>> data = new LinkedHashMap<>();
		Map<String, String> headersInfo = new LinkedHashMap<>();

		Optional.ofNullable(jsonConfiguration.getJsonArray("columnHeaders"))
				.ifPresent(columnHeaders -> columnHeaders.forEach(element -> {
					JsonObject object = (JsonObject) element;
					JsonArray labels = object.getJsonArray("labels");
					headersInfo.put(object.getString(DefaultProperties.NAME), labels.getString(0));
				}));

		for (Instance instance : instances) {
			DefinitionModel instanceDefinitionModel = definitionService.getInstanceDefinition(instance);
			for (String propertyName : properties) {
				Optional<PropertyDefinition> field = instanceDefinitionModel.getField(propertyName);
				if (field.isPresent()) {
					addToMap(data, propertyName, field.get().getLabel());
				} else {
					getLabelByInstanceIdentifier(identifiers, data, propertyName);
				}
				for (String subPropertyName : getPropertySubProperties(selectedSubProperties, propertyName)) {
					String name = propertyName + IdocRenderer.SEPARATOR + subPropertyName;
					addToMap(data, name, headersInfo.get(name));
				}
			}
		}
		return getPropertyLabels(data);
	}

	/**
	 * Fetch label from passed widget config for instance identifier because sometimes when cross selection is made in
	 * widget and some properties.
	 *
	 * @param identifiers
	 *            the instance identifiers
	 * @param data
	 *            the data
	 * @param propertyName
	 *            the property name
	 */
	private void getLabelByInstanceIdentifier(Collection<String> identifiers, Map<String, Set<String>> data,
			String propertyName) {
		for (String identifier : identifiers) {
			DefinitionModel definitionModel = definitionService.find(identifier);
			if (definitionModel != null) {
				definitionModel.getField(propertyName).ifPresent(
						modelField -> addToMap(data, propertyName, modelField.getLabel()));
			} else {
				String fullURI = namespaceRegistryService.buildFullUri(identifier);
				DataTypeDefinition typeDef = definitionService.getDataTypeDefinition(fullURI);
				if (typeDef != null) {
					List<DefinitionModel> allDefinitions = definitionService.getAllDefinitions(GenericDefinition.class);

					ClassInstance classInstance = semanticDefinitionService.getClassInstance(fullURI);
					Set<String> rdfTypes = createHashSet(1 + classInstance.getSubClasses().size());
					rdfTypes.add(fullURI);
					// for non top level classes collect their children
					classInstance.getSubClasses().keySet().stream().map(namespaceRegistryService::buildFullUri).forEach(
							rdfTypes::add);

					allDefinitions
							.stream()
								.filter(filterByDefinitionType(typeDef).and(filterByRdfType(rdfTypes)))
								.flatMap(DefinitionModel::fieldsStream)
								.distinct()
								.filter(prop -> prop.getIdentifier().equals(propertyName))
								.findFirst()
								.ifPresent(prop -> addToMap(data, propertyName, prop.getLabel()));
				}
			}
		}
	}

	/**
	 * Filter definition models if they are of the generic definitions so that they match the type represented by the
	 * given data type definition.
	 *
	 * @param typeDefinition
	 *            the type definition
	 * @return the predicate
	 */
	private Predicate<DefinitionModel> filterByDefinitionType(DataTypeDefinition typeDefinition) {
		return model -> !(model instanceof GenericDefinition)
				|| nullSafeEquals(typeDefinition.getName(), typeProvider.getDataTypeName(model.getType()), true);
	}

	/**
	 * filter definitions that have semantic that match the requested or does not have such field at all (this is for
	 * cases/WF, etc).
	 *
	 * @param types
	 *            the types
	 * @return the predicate
	 */
	private static Predicate<DefinitionModel> filterByRdfType(Set<String> types) {
		return model -> {
			Optional<PropertyDefinition> type = model.getField(DefaultProperties.SEMANTIC_TYPE);
			return !type.isPresent() || type.get().getDefaultValue() == null
					|| types.contains(type.get().getDefaultValue());
		};
	}

	/**
	 * Add label to Map which later used for creating proper headings, where we can have multiple labels for a single
	 * heading.
	 *
	 * @param data
	 *            the data
	 * @param propertyName
	 *            the property name
	 * @param label
	 *            the label
	 */
	public static void addToMap(Map<String, Set<String>> data, String propertyName, String label) {
		if (data.containsKey(propertyName)) {
			data.get(propertyName).add(label);
		} else {
			Set<String> labels = new LinkedHashSet<>(1);
			labels.add(label);
			data.put(propertyName, labels);
		}
	}

	/**
	 * Convert Map with labels to set, where for each property join the labels to multi label.
	 *
	 * @param data
	 *            the data
	 * @return set of {@link RenderProperty} represented label and name of property.
	 */
	private static Set<RenderProperty> getPropertyLabels(Map<String, Set<String>> data) {
		Set<RenderProperty> labels = new LinkedHashSet<>(data.size());
		for (Entry<String, Set<String>> entry : data.entrySet()) {
			String value = entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.joining(", "));
			labels.add(new RenderProperty(entry.getKey(), value));
		}
		return labels;
	}

	/**
	 * Removes html tags from header and extracts url from href if exist. Creates RenderProperty representing the
	 * instance header.
	 *
	 * @param instance
	 *            the instance
	 * @param headerType
	 *            the header type of the widget
	 * @return the render property
	 */
	protected RowRenderProperty sanitizeHeaderRenderProperty(Instance instance, Map<String, Object> headersConfig) {
		String headerType = (String) headersConfig.get(INSTANCE_HEADER_TYPE);

		RowRenderProperty instanceHeaderProperty = new RowRenderProperty(headerType, ENTITY_LABEL);
		instanceHeaderProperty.setType(RowRenderPropertyType.HYPERLINK);
		String hyperlink = IdocRenderer.getHyperlink(instance, headersConfig, systemConfiguration.getUi2Url().get());
		ArrayList<String> hyperLinks = new ArrayList<>(1);
		hyperLinks.add(hyperlink);
		instanceHeaderProperty.addValue(HYPERLINKS, hyperLinks);
		return instanceHeaderProperty;
	}

	/**
	 * Check have to show footer.
	 *
	 * @param jsonConfiguration
	 *            widget configuration.
	 * @return true if footer have to be shown.
	 */
	public static boolean showFooter(JsonObject jsonConfiguration) {
		return jsonConfiguration.getBoolean(SHOW_FOOTER, false);
	}

	/**
	 * Check if <code>table</code> is into another table then will set width <code>width</code>.
	 *
	 * @param table
	 *            to be checked.
	 * @param element
	 *            widget element.
	 * @param width
	 *            the width to be set.
	 */
	protected static void setWidth(HtmlTableBuilder table, Element element, String width) {
		if (isInnerTable(element)) {
			table.addAttribute(JsoupUtil.ATTRIBUTE_WIDTH, width);
		}
	}

	/**
	 * Check if <code>table</code> is into another table.
	 *
	 * @param element
	 *            widget element.
	 * @return true if element is into other table.
	 */
	protected static boolean isInnerTable(Element element) {
		return "td".equals(element.parent().tagName());
	}

	@Override
	public void afterRender(Element newElement, ContentNode node) {
		Element element = node.getElement();
		element.after(newElement);
		newElement.after("<p />");
		element.remove();
	}

	/**
	 * Build table with one row. It will be populated with message with key <code>messageKye</code>.
	 *
	 * @param tableBuilder - the table builder.
	 * @param messageKey - the key of message.
	 * @return filled html table with message.
	 */
	protected Element buildOneRowMessageTable(HtmlTableBuilder tableBuilder, String messageKey) {
		HtmlTableBuilder.addNoResultRow(tableBuilder, labelProvider.getLabel(messageKey));
		return tableBuilder.build();
	}

	/**
	 * Populate table with empty search result. If selection mode is manually key "select.object.none" will be used
	 * otherwise "select.object.results.none"
	 * @param selectionMode widget selection mode.
	 * @param tableBuilder - the table builder.
	 * @return html representation of filled table with empty search result.
	 */
	protected Element buildEmptySelectionTable(String selectionMode, HtmlTableBuilder tableBuilder) {
		String messageKey = MANUALLY.equals(selectionMode) ? KEY_LABEL_SELECT_OBJECT_NONE : KEY_LABEL_SELECT_OBJECT_RESULTS_NONE;
		return buildOneRowMessageTable(tableBuilder, messageKey);
	}

	/**
	 * Check if widget has search criteria.
	 * @param widgetConfiguration - the widget configuration.
	 * @return true if widget configuration contains search criteria.
	 */
	protected boolean hasSearchCriteria(JsonObject widgetConfiguration) {
		JsonObject criteria = widgetConfiguration.getJsonObject(SEARCH_CRITERIA);
		return criteria != null && !criteria.isEmpty();
	}
	
	/**
	 * Extracts and converts widget configuration object
	 * @param node
	 * @return {@link JsonObject} containing widget's configuration
	 */
	protected JsonObject getWidgetConfiguration(ContentNode node) {
		Widget widget = (Widget) node;
		WidgetConfiguration configuration = widget.getConfiguration();
		// convert widget configuration to json
		return IdocRenderer.toJson(configuration);
	}
	
	/**
	 * Generates widget's frame. A table with widget's title and borders.
	 * @param widgetConfiguration
	 * @return {@link HtmlTableBuilder} widget's table
	 */
	protected HtmlTableBuilder renderWidgetFrame(JsonObject widgetConfiguration) {
		// get the widget title
		String widgetTitle = IdocRenderer.getWidgetTitle(widgetConfiguration);
		Boolean showHeader = IdocRenderer.getShowWidgetHeader(widgetConfiguration);
		// create table
		return new HtmlTableBuilder(widgetTitle, showHeader);
	}
}