package com.sirma.itt.objects.web.label.retrieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.PropertyInstance;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetriever;
import com.sirma.itt.emf.label.retrieve.PairFieldValueRetriever;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Converts the objects definition id to object label.
 * 
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 2)
public class ObjectSubTypeFieldValueRetriever extends PairFieldValueRetriever {
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<String>(1);
		SUPPORTED_FIELDS.add(FieldId.OBJECTSUBTYPE);
	}
	private static final String TITLE = "title";
	private static final String INSTANCE = "instance";

	private Map<String, String> objectSubTypeCache;

	private static final Logger LOGGER = Logger.getLogger(ObjectSubTypeFieldValueRetriever.class);

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private ServiceRegister serviceRegister;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private AllowedChildrenTypeProvider typeProvider;

	/**
	 * Inits the cache.
	 */
	@PostConstruct
	public void init() {
		objectSubTypeCache = new HashMap<>(500);
	}

	/**
	 * Gets the label for the specified value. First it tries to get the label from the semantic and
	 * if no label is present it tries to get it from the definitions.
	 * 
	 * @param value
	 *            the value
	 * @return the label
	 */
	@Override
	public String getLabel(String... value) {

		if (!ArrayUtils.isEmpty(value) && value[0] != null && value[1] != null) {
			String subtype = value[0];
			String cachedSubtypeLabel = objectSubTypeCache.get(getCurrentUserLanguage() + value[0]);
			if (cachedSubtypeLabel != null) {
				return cachedSubtypeLabel;
			} else {
				String semanticSubtypeLabel = getLabelFromSemantic(subtype);
				if (subtype.equals(semanticSubtypeLabel)) {
					subtype = getLabelFromDefinitions(value);
				} else {
					subtype = semanticSubtypeLabel;
				}
				objectSubTypeCache.put(getCurrentUserLanguage() + value[0], subtype);
			}
			return subtype;
		}
		return null;
	}

	/**
	 * Gets the label from definitions. This requires 2 values - the first one being the semantic
	 * uri of the reference class, the second one - the definition id. (e.g. emf:Project and
	 * PRJ10001).
	 * 
	 * @param value
	 *            the definition id of the object and the parent's definition id
	 * @return the label from definitions
	 */
	private String getLabelFromDefinitions(String... value) {
		String fullURI = namespaceRegistryService.buildFullUri(value[1]);
		DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(fullURI);
		Class clazz = definition.getJavaClass();
		DefinitionModel model;
		if (clazz.equals(DocumentInstance.class)) {
			model = dictionaryService.getDefinition(DocumentDefinitionTemplate.class, value[0]);
		} else if (clazz.equals(TaskInstance.class)) {
			model = dictionaryService.getDefinition(TaskDefinitionTemplate.class, value[0]);
		} else {
			model = dictionaryService.getDefinition(clazz, value[0]);
		}
		PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model,
				DefaultProperties.TYPE);
		if (property != null) {
			if ((property.getCodelist() != null) && (property.getCodelist() > 0)) {
				CodeValue codeValue = codelistService
						.getCodeValue(property.getCodelist(), value[0]);
				if (codeValue != null) {
					return codeValue.getProperties().get(getCurrentUserLanguage()).toString();
				}
			}
		}
		return value[0];
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
			} else {
				PropertyInstance model = semanticDefinitionService
						.getRelation(namespaceRegistryService.buildFullUri(value));
				if (model != null) {
					return model.getProperties().get("title").toString();
				}
			}
		} catch (Exception e) {
			// Nothing to do here, this is normal.
		}
		return value;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		offset = offset != null ? offset : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();

		List<ClassInstance> classes = semanticDefinitionService.getClasses();
		Set<ClassInstance> processed = CollectionUtils.createLinkedHashSet(classes.size());

		for (ClassInstance classInstance : classes) {
			if (isSearchable(classInstance) && !processed.contains(classInstance)) {
				processed.add(classInstance);
				String fullUri = namespaceRegistryService.buildFullUri(classInstance
						.getProperties().get(INSTANCE).toString());

				total = addDefinitionsForType(results, fullUri, filter, offset, limit, total);
				if (!classInstance.getSubClasses().isEmpty()) {
					for (ClassInstance subInstance : classInstance.getSubClasses().values()) {
						if (isSearchable(subInstance)) {
							processed.add(subInstance);
							String text = subInstance.getProperties().get(TITLE).toString();
							String id = subInstance.getProperties().get(INSTANCE).toString();
							if (StringUtils.isNullOrEmpty(filter)
									|| text.toLowerCase().startsWith(filter.toLowerCase())) {
								validateAndAddPair(results, id, text, filter, offset, limit, total);
								total++;
							}
						}
					}
				}
			}
		}

		List<PropertyInstance> relations = semanticDefinitionService.getRelations();
		for (PropertyInstance propertyInstance : relations) {
			String text = propertyInstance.getProperties().get(TITLE).toString();
			String id = propertyInstance.getProperties().get(INSTANCE).toString();
			if (StringUtils.isNullOrEmpty(filter)
					|| text.toLowerCase().startsWith(filter.toLowerCase())) {
				validateAndAddPair(results, id, text, filter, offset, limit, total);
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

	private long addDefinitionsForType(List<Pair<String, String>> results, String fullUri,
			String filter, Integer offset, Integer limit, long total) {

		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(fullUri);
		if (typeDefinition == null) {
			return total;
		}
		if (ObjectInstance.class.isAssignableFrom(typeDefinition.getJavaClass())) {
			return total;
		}
		if (!fullUri.equals(typeDefinition.getFirstUri())) {
			return total;
		}

		InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
				.getInstanceService(typeDefinition.getJavaClass());
		if (instanceService == null) {
			return total;
		}

		String language = getCurrentUserLanguage();

		List<DefinitionModel> allDefinitions = dictionaryService.getAllDefinitions(instanceService
				.getInstanceDefinitionClass());
		for (DefinitionModel model : allDefinitions) {
			if (model instanceof GenericDefinition) {
				if (!EqualsHelper.nullSafeEquals(typeDefinition.getName(),
						typeProvider.getDataTypeName(((GenericDefinition) model).getType()), true)) {
					continue;
				}
			}
			PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model,
					DefaultProperties.TYPE);
			if (property != null) {
				if ((property.getCodelist() != null) && (property.getCodelist() > 0)) {
					CodeValue codeValue = codelistService.getCodeValue(property.getCodelist(),
							property.getDefaultValue());
					if (codeValue != null) {

						String text = codeValue.getProperties().get(language).toString();
						String id = codeValue.getValue();

						if (StringUtils.isNullOrEmpty(filter)
								|| text.toLowerCase().startsWith(filter.toLowerCase())) {
							validateAndAddPair(results, id, text, filter, offset, limit, total);
							total++;
						}
					}
				}
			}
		}

		return total;
	}

	/**
	 * Check if the instance is searcheable
	 * 
	 * @param classInstance
	 *            the class to check
	 * @return true if is searcheable
	 */
	private boolean isSearchable(ClassInstance classInstance) {
		return Boolean.TRUE == classInstance.getProperties().get("searchable");
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}

}
