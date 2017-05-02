package com.sirma.itt.seip.export.renders;

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
import javax.json.JsonObject;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.export.renders.objects.RenderProperty;
import com.sirma.itt.seip.export.renders.objects.RowRenderProperty;
import com.sirma.itt.seip.export.renders.objects.RowRenderPropertyType;
import com.sirma.itt.seip.export.renders.utils.JsoupUtil;
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
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.export.renders.html.table.HtmlTableBuilder;

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
	protected JsonToConditionConverter convertor;

	@Inject
	protected DictionaryService dictionaryService;

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
	 * @param properties
	 *            the properties
	 * @param addInstanceHeader
	 *            the add instance header
	 * @param headerType
	 *            the header type
	 * @return the list
	 */
	protected List<RowRenderProperty> extractPropertiesData(Instance instance, Collection<String> properties,
			String headerType) {
		List<RowRenderProperty> propertyValues = new LinkedList<>();
		if (StringUtils.isNotBlank(headerType) && DefaultProperties.DEFAULT_HEADERS.contains(headerType)) {
			RowRenderProperty sanitizedHeader = sanitizeHeaderRenderProperty(instance, headerType);
			propertyValues.add(sanitizedHeader);
		}
		for (String property : properties) {
			propertyValues.add(createRenderProperty(instance, property, headerType));
		}
		return propertyValues;
	}

	/**
	 * Create RenderProperty with filled data based on property of instance.
	 *
	 * @param instance
	 *            the instance
	 * @param property
	 *            the property
	 * @param headerType
	 *            the header type of the widget
	 */
	protected RowRenderProperty createRenderProperty(Instance instance, String property, String headerType) {
		PropertyDefinition propertyDefinition = dictionaryService.getProperty(property, instance);
		if (propertyDefinition != null) {
			RowRenderProperty renderProperty = new RowRenderProperty(property, propertyDefinition.getLabel());
			return getPropertyValue(instance, propertyDefinition, renderProperty, headerType);
		}
		RowRenderProperty renderProperty = new RowRenderProperty(property, property);
		renderProperty.setType(RowRenderPropertyType.HIDDEN);
		renderProperty.addValue(VALUE, "");
		return renderProperty;
	}

	/**
	 * Prepare links from passed instances.
	 *
	 * @param instances
	 *            the instances in cell
	 * @param renderProperty
	 *            the render property
	 * @param headerType
	 *            the header type
	 */
	protected void prepareInstancesLinks(Collection<Instance> instances, RowRenderProperty renderProperty,
			String headerType) {
		ArrayList<String> hyperLinks = new ArrayList<>(instances.size());
		String typeOfHeader = StringUtils.isNotBlank(headerType)
				&& DefaultProperties.DEFAULT_HEADERS.contains(headerType) ? headerType
						: DefaultProperties.HEADER_COMPACT;
		for (Instance propertyInstance : instances) {
			hyperLinks.add(
					IdocRenderer.getHyperlink(propertyInstance, typeOfHeader, systemConfiguration.getUi2Url().get()));
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
	 * @param headerType
	 *            the header type of the widget
	 * @return the property value
	 */
	@SuppressWarnings({ "unchecked" })
	protected RowRenderProperty getPropertyValue(Instance instance, PropertyDefinition propertyDefinition,
			RowRenderProperty renderProperty, String headerType) {
		Serializable propertyValue = instance.get(propertyDefinition.getIdentifier());
		if (propertyValue != null) {
			// check is codelist
			if (PropertyDefinition.hasCodelist().test(propertyDefinition)) {
				Integer codelist = propertyDefinition.getCodelist();
				if (propertyDefinition.isMultiValued().booleanValue()) {
					propertyValue = processCodelistMultiValues(propertyValue, codelist);
				} else {
					propertyValue = codelistService.getDescription(codelist, propertyValue.toString());
				}
			}
			if (PropertyDefinition.hasType(DataTypeDefinition.DATETIME).test(propertyDefinition)) {
				FormattedDateTime formattedDateTime = typeConverter.convert(FormattedDateTime.class, propertyValue);
				propertyValue = formattedDateTime.getFormatted();
			}
			if (PropertyDefinition.hasType(DataTypeDefinition.DATE).test(propertyDefinition)) {
				FormattedDate formattedDateTime = typeConverter.convert(FormattedDate.class, propertyValue);
				propertyValue = formattedDateTime.getFormatted();
			}
			ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
			if (controlDefinition != null) {
				propertyValue = getControlDefinitionLabel(propertyValue, controlDefinition);
			}
			if (PropertyDefinition.isObjectProperty().test(propertyDefinition)) {
				Collection<Serializable> instanceIds;
				if (propertyValue instanceof Collection<?>) {
					instanceIds = (Collection<Serializable>) propertyValue;

				} else {
					instanceIds = Collections.singletonList(propertyValue);

				}
				Collection<Instance> loadedInstances = loadInstances(instanceIds);
				if (!loadedInstances.isEmpty()) {
					prepareInstancesLinks(loadedInstances, renderProperty, headerType);
				}
			}
		}
		checkHiddenConditions(instance, propertyDefinition, renderProperty, propertyValue);
		renderProperty.addValue(VALUE, propertyValue);
		return renderProperty;
	}

	private void checkHiddenConditions(Instance instance, PropertyDefinition propertyDefinition,
			RowRenderProperty renderProperty, Serializable propertyValue) {
		//hidden properties have to be visible in preview system never.
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
	private static Serializable getControlDefinitionLabel(Serializable propertyValue,
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
		Condition tree = convertor.parseCondition(criteria);
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(tree);
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(tree.getRules().size()));
		searchRequest.setSearchTree(tree);
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
	 * @param identifiers
	 *            the identifiers
	 * @param instances
	 *            list of instances
	 * @param properties
	 *            list of properties
	 * @return set of {@link RenderProperty} represented label and name of property.
	 */
	protected Set<RenderProperty> getInstancesPropertiesLabels(Collection<String> identifiers,
			Collection<Instance> instances, Set<String> properties) {
		Map<String, Set<String>> data = new LinkedHashMap<>();
		for (Instance instance : instances) {
			DefinitionModel instanceDefinitionModel = dictionaryService.getInstanceDefinition(instance);
			for (String propertyName : properties) {
				Optional<PropertyDefinition> field = instanceDefinitionModel.getField(propertyName);
				if (field.isPresent()) {
					addToMap(data, propertyName, field.get().getLabel());
				} else {
					getLabelByInstanceIdentifier(identifiers, data, propertyName);
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
			DefinitionModel definitionModel = dictionaryService.find(identifier);
			if (definitionModel != null) {
				definitionModel.getField(propertyName).ifPresent(
						modelField -> addToMap(data, propertyName, modelField.getLabel()));
			} else {
				String fullURI = namespaceRegistryService.buildFullUri(identifier);
				DataTypeDefinition typeDef = dictionaryService.getDataTypeDefinition(fullURI);
				if (typeDef != null) {
					List<DefinitionModel> allDefinitions = dictionaryService.getAllDefinitions(GenericDefinition.class);

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
	private static void addToMap(Map<String, Set<String>> data, String propertyName, String label) {
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
	protected RowRenderProperty sanitizeHeaderRenderProperty(Instance instance, String headerType) {
		RowRenderProperty instanceHeaderProperty = new RowRenderProperty(headerType, ENTITY_LABEL);
		instanceHeaderProperty.setType(RowRenderPropertyType.HYPERLINK);
		String hyperlink = IdocRenderer.getHyperlink(instance, headerType, systemConfiguration.getUi2Url().get());
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
}