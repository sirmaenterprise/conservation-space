package com.sirma.itt.objects.web.label.retrieve;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetriever;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.PairFieldValueRetriever;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Converts the objects definition id to object label.
 *
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 2)
public class ObjectSubTypeFieldValueRetriever extends PairFieldValueRetriever {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * The set of supported objects that are returned by the method {@link #getSupportedObjects()}.
	 */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECT_SUBTYPE);
	}

	private static final String TITLE = DefaultProperties.TITLE;
	private static final String INSTANCE = "instance";

	@Inject
	private ContextualMap<String, String> objectSubTypeCache;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceTypes instanceTypes;

	/**
	 * Gets the label for the specified value. First it tries to get the label from the semantic and if no label is
	 * present it tries to get it from the definitions.
	 *
	 * @param value
	 *            the value
	 * @param additionalParameters
	 *            additional parameters: {@link FieldValueRetrieverParameters#OBJECTTYPE} - object type for the given
	 *            subtype. <b>Required</b>
	 * @return the label
	 */
	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null && additionalParameters != null
				&& additionalParameters.get(FieldValueRetrieverParameters.OBJECTTYPE) != null) {
			return objectSubTypeCache.computeIfAbsent(getCurrentUserLanguage() + value,
					(k) -> getSemanticLabel(value, additionalParameters));
		}
		return null;
	}

	private String getSemanticLabel(String value, SearchRequest additionalParameters) {
		String subtype = value;
		String semanticSubtypeLabel = getLabelFromSemantic(subtype);
		if (subtype.equals(semanticSubtypeLabel)) {
			subtype = getLabelFromDefinitions(value,
					additionalParameters.getFirst(FieldValueRetrieverParameters.OBJECTTYPE));
		} else {
			subtype = semanticSubtypeLabel;
		}
		return subtype;
	}

	/**
	 * Gets the label from definitions. This requires 2 values - the first one being the semantic uri of the reference
	 * class, the second one - the definition id. (e.g. emf:Project and PRJ10001).
	 *
	 * @param objectSubType
	 *            the definition id of the object
	 * @param objectType
	 *            parent's definition id
	 * @return the label from definitions
	 */
	private String getLabelFromDefinitions(String objectSubType, String objectType) {
		String codeValueName = objectSubType;

		DefinitionModel model = definitionService.find(objectSubType);
		if (model == null) {
			LOGGER.debug("No data type definition for {}", objectSubType);
			return objectSubType;
		}

		PathElement parent = PathHelper.getRootElement((PathElement) model);
		PropertyDefinition property;
		if (parent != null) {

			property = PathHelper.findProperty((DefinitionModel) parent, (PathElement) model, DefaultProperties.TYPE);
		} else {
			property = PathHelper.findProperty(model, (PathElement) model, DefaultProperties.TYPE);
		}
		if (property != null && property.getCodelist() != null && property.getCodelist().intValue() > 0) {
			CodeValue codeValue = codelistService.getCodeValue(property.getCodelist(), codeValueName);
			if (codeValue != null) {
				return codeValue.getProperties().get(getCurrentUserLanguage()).toString();
			}
		}
		return objectSubType;
	}

	/**
	 * Gets the label from semantic. Works both for normal objects and relations.
	 *
	 * @param value
	 *            the value
	 * @return the label from semantic
	 */
	private String getLabelFromSemantic(String value) {
		try {
			ClassInstance instance = semanticDefinitionService.getClassInstance(value);
			if (instance != null) {
				return instance.getLabel(getCurrentUserLanguage());
			}
			PropertyInstance model = semanticDefinitionService.getRelation(value);
			if (model != null) {
				return model.getLabel(getCurrentUserLanguage());
			}
		} catch (Exception e) {
			// Nothing to do here, this is normal.
			LOGGER.trace("Could not get label from semantic", e);
		}
		return value;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		int localOffset = offset != null ? offset.intValue() : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();

		List<ClassInstance> classes = semanticDefinitionService.getClasses();
		Set<ClassInstance> processed = CollectionUtils.createLinkedHashSet(classes.size());

		for (ClassInstance classInstance : classes) {
			if (isSearchable(classInstance) && !processed.contains(classInstance)) {
				processed.add(classInstance);
				String fullUri = namespaceRegistryService
						.buildFullUri(classInstance.getProperties().get(INSTANCE).toString());

				total = addDefinitionsForType(results, fullUri, filter, localOffset, limit, total);
				if (!classInstance.getSubClasses().isEmpty()) {
					for (ClassInstance subInstance : classInstance.getSubClasses().values()) {
						if (isSearchable(subInstance)) {
							processed.add(subInstance);
							String text = subInstance.getProperties().get(TITLE).toString();
							String id = subInstance.getProperties().get(INSTANCE).toString();
							if (StringUtils.isBlank(filter)
									|| text.toLowerCase().startsWith(filter.toLowerCase())) {
								validateAndAddPair(results, id, text, filter, localOffset, limit, total);
								total++;
							}
						}
					}
				}
			}
		}

		List<PropertyInstance> relations = semanticDefinitionService.getSearchableRelations();
		for (PropertyInstance propertyInstance : relations) {
			String text = propertyInstance.getProperties().get(TITLE).toString();
			String id = propertyInstance.getProperties().get(INSTANCE).toString();
			if (StringUtils.isBlank(filter) || text.toLowerCase().startsWith(filter.toLowerCase())) {
				validateAndAddPair(results, id, text, filter, localOffset, limit, total);
				total++;
			}

		}

		return new RetrieveResponse(total, results);
	}

	/**
	 * Adds the definitions for type.
	 *
	 * @param results
	 *            the results
	 * @param fullUri
	 *            class full uri
	 * @param filter
	 *            the filter used to filter labels. "starts with" filter should be applied
	 * @param offset
	 *            the offset of the returned results
	 * @param limit
	 *            the number of the results to be returned
	 * @param total
	 *            the total number of results as of this moment
	 * @return total number of results when method exits
	 */
	private long addDefinitionsForType(List<Pair<String, String>> results, String fullUri, String filter, int offset,
			Integer limit, long total) {
		Optional<InstanceType> type = instanceTypes.from(fullUri);
		if (!type.isPresent()) {
			return total;
		}

		String language = getCurrentUserLanguage();
		InstanceType instanceType = type.get();

		List<Pair<String, String>> filterd = definitionService
				.getAllDefinitions(instanceType)
					.map(model -> model.getField(DefaultProperties.TYPE).orElse(null))
					.filter(Objects::nonNull)
					.filter(PropertyDefinition.hasCodelist())
					.map(property -> codelistService.getCodeValue(property.getCodelist(), property.getDefaultValue()))
					.map(Pair.from(CodeValue::getValue, value -> value.getProperties().get(language).toString()))
					.filter(pair -> StringUtils.isBlank(filter)
							|| pair.getSecond().toLowerCase().startsWith(filter.toLowerCase()))
					.collect(Collectors.toList());

		long localTotal = total;
		for (Pair<String, String> model : filterd) {
			if (offset <= localTotal && (limit == null || results.size() < limit.intValue())) {
				results.add(model);
			}
			localTotal++;
		}

		return localTotal;
	}

	/**
	 * Check if the instance is searcheable.
	 */
	private static boolean isSearchable(ClassInstance classInstance) {
		return classInstance.type().isSearchable();
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
