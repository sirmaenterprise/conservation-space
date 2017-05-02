package com.sirma.itt.objects.web.definitions;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_DEFINITION_ID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.web.util.InstancePropertyComparator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.rest.DefinitionModelObject;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Provides information about the definitions of the models used in the application. End-point for <b>UI2</b> REST
 * clients.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
@Path("/definitions")
@Produces(Versions.V2_JSON)
public class DefinitionsRestService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String INSTANCE = "instance";
	private static final String DEFINITION_ID = "definitionId";
	private static final String DEFINITIONS = "definitions";
	private static final String CL_SUB_TYPES = "clSubTypes";
	private static final String SUB_TYPES = "subTypes";

	@Inject
	private AuthorityService authorityService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private TypeMappingProvider typeProvider;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private UserPreferences userPreferences;

	/**
	 * Retrieves all available types in the system based on the provided parameters.
	 *
	 * @param classFilter
	 *            - list of classes to filter out types
	 * @param skipDefinitionTypes
	 *            - to add or not to add types from definitions
	 * @return hierarchical JSON representation of the types
	 */
	@GET
	@Path("/types")
	public String getTypes(@QueryParam("classFilter") List<String> classFilter,
			@QueryParam("skipDefinitionTypes") boolean skipDefinitionTypes) {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<ClassInstance> classes = getClasses(classFilter);
		JSONArray result = new JSONArray();
		Set<ClassInstance> history = new HashSet<>();

		try {
			Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.enable();
			for (ClassInstance clazz : classes) {
				addTypeHierarchy(clazz, result, null, history, skipDefinitionTypes, false);
			}
		} finally {
			Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.disable();
		}
		LOGGER.trace("Types fetched in {} ms.", tracker.stop());
		return result.toString();
	}

	/**
	 * Retrieves {@link DefinitionModel} for passed definition id.
	 *
	 * @param definitionId
	 * @return
	 */
	@GET
	@Path("/{definition-id}")
	public DefinitionModelObject getDefinitionModel(@PathParam(KEY_DEFINITION_ID) String definitionId) {
		DefinitionModel model = dictionaryService.find(definitionId);
		if (model == null) {
			throw new ResourceException(NOT_FOUND,
					new ErrorData().setMessage("Could not find definition for id: " + definitionId), null);
		}
		DefinitionModelObject object = new DefinitionModelObject();
		object.setDefinitionModel(model);
		return object;
	}

	/**
	 * Retrieves classes based on the provided filter, or all available if the filter is blank.
	 *
	 * @param classFilter
	 *            - the filter
	 * @return list of classes
	 */
	private List<ClassInstance> getClasses(List<String> classFilter) {
		List<ClassInstance> classes = new LinkedList<>();
		if (CollectionUtils.isNotEmpty(classFilter)) {
			for (String id : classFilter) {
				ClassInstance clazz = semanticDefinitionService.getClassInstance(id);
				addNonNullValue(classes, clazz);
			}
			Collections.sort(classes, InstancePropertyComparator.BY_TITLE_COMPARATOR);
		} else {
			classes.add(semanticDefinitionService.getRootClass());
		}
		return classes;
	}

	/**
	 * Recursively builds types and sub types based on the currently provided class.
	 *
	 * @param clazz
	 *            - the currently provided class
	 * @param rootArray
	 *            - the root array where main types are added
	 * @param subArray
	 *            - the current sub array of a main type
	 * @param history
	 *            - set to remember which classes are added to avoid duplication
	 * @param skipDefinitionTypes
	 *            - flag to add or not definition sub types
	 * @param subClass
	 *            - flag if the current type is a sub type
	 */
	private void addTypeHierarchy(ClassInstance clazz, JSONArray rootArray, JSONArray subArray,
			Set<ClassInstance> history, boolean skipDefinitionTypes, boolean subClass) {
		if (clazz == null) {
			LOGGER.error("Invalid/null class is provided to get hierarchy for. Currect model is: {}", history);
			return;
		}
		if (!history.add(clazz)) {
			return;
		}

		JSONArray subArrayLocal = subArray;
		boolean searchable = isSearchable(clazz);

		if (searchable && !isForbiddenLibrary(clazz)) {
			JSONObject type = addTypeInfo(clazz, subClass, skipDefinitionTypes);
			if (subClass) {
				subArrayLocal.put(type);
			} else {
				rootArray.put(type);
				subArrayLocal = JsonUtil.getJsonArray(type, SUB_TYPES);
			}
		}

		List<ClassInstance> subClasses = new LinkedList<>(clazz.getSubClasses().values());
		Collections.sort(subClasses, InstancePropertyComparator.BY_TITLE_COMPARATOR);
		for (ClassInstance sub : subClasses) {
			LOGGER.trace("Check subclass {} of {}", sub, clazz);
			// technically it's a sub class but if the parent is not searchable we threat it as top level
			addTypeHierarchy(sub, rootArray, subArrayLocal, history, skipDefinitionTypes, searchable);
		}
	}

	/**
	 * Builds {@link JSONObject} from the provided class.
	 *
	 * @param clazz
	 *            - the provided class
	 * @param subType
	 *            - flag if the current type is a sub type
	 * @param skipDefinitionTypes
	 *            - flag to add or not definition sub types
	 * @return the builded JSON object with type information
	 */
	private JSONObject addTypeInfo(ClassInstance clazz, boolean subType, boolean skipDefinitionTypes) {
		JSONObject value = new JSONObject();
		JsonUtil.addToJson(value, DefaultProperties.URI, clazz.get(INSTANCE));
		JsonUtil.addToJson(value, DefaultProperties.TITLE, clazz.getLabel(userPreferences.getLanguage()));

		if (!subType) {
			JSONArray codelistSubTypes = new JSONArray();
			JsonUtil.addToJson(value, CL_SUB_TYPES, codelistSubTypes);

			JSONArray subtypesArray = new JSONArray();
			JsonUtil.addToJson(value, SUB_TYPES, subtypesArray);

			if (!skipDefinitionTypes) {
				String fullUri = namespaceRegistryService.buildFullUri(clazz.get(INSTANCE).toString());
				addDefinitionsForType(codelistSubTypes, fullUri);
			}
		}

		if (clazz.getProperties().get(DEFINITIONS) != null) {
			JsonUtil.addToJson(value, DEFINITION_ID,
					((Set<String>) clazz.getProperties().get(DEFINITIONS)).iterator().next());
		}

		return value;
	}

	/**
	 * Extracts definition types from the provided full URI.
	 *
	 * @param subTypeArray
	 *            - the array where sub types are added
	 * @param fullUri
	 *            - the full URI
	 */
	private void addDefinitionsForType(JSONArray subTypeArray, String fullUri) {
		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(fullUri);
		if (!isDefinitionValid(typeDefinition, fullUri)) {
			return;
		}

		for (DefinitionModel model : dictionaryService.getAllDefinitions(GenericDefinition.class)) {
			if (model instanceof GenericDefinition && !EqualsHelper.nullSafeEquals(typeDefinition.getName(),
					typeProvider.getDataTypeName(((GenericDefinition) model).getType()), true)) {
				continue;
			}
			String uriValue = dictionaryService.getDefinitionIdentifier(model);
			ClassInstance definitionClass = semanticDefinitionService.getClassInstance(uriValue);
			if (definitionClass == null || !isForbiddenLibrary(definitionClass)) {
				PropertyDefinition property = getProperty(model);
				buildCodelistTypeData(subTypeArray, property);
			}
		}
	}

	private static PropertyDefinition getProperty(DefinitionModel model) {
		return PathHelper.findProperty(model, (PathElement) model, DefaultProperties.TYPE);
	}

	private static boolean isDefinitionValid(DataTypeDefinition typeDefinition, String fullUri) {
		if (typeDefinition == null) {
			return false;
		}
		// if (ObjectInstance.class.isAssignableFrom(typeDefinition.getJavaClass())) {
		// return false;
		// }
		// if (!fullUri.equals(typeDefinition.getFirstUri())) {
		// return false;
		// }
		return true;
	}

	private void buildCodelistTypeData(JSONArray subtypeArray, PropertyDefinition property) {
		if (property != null && property.getCodelist() != null && property.getCodelist() > 0) {
			CodeValue codeValue = codelistService.getCodeValue(property.getCodelist(), property.getDefaultValue());
			if (codeValue != null) {
				JSONObject value = new JSONObject();
				String extractRootPath = PathHelper.extractLastElementInPath(property.getParentPath());
				JsonUtil.addToJson(value, DefaultProperties.URI, extractRootPath);
				JsonUtil.addToJson(value, DefaultProperties.TITLE, codeValue.get(userPreferences.getLanguage()));
				subtypeArray.put(value);
			}
		}
	}

	/**
	 * Checks if it's a forbidden library (if it part of the library set and it not part of allowed libraries for user).
	 *
	 * @param clazz
	 *            the class
	 * @return true, if is forbidden library or false if not
	 */
	private boolean isForbiddenLibrary(ClassInstance clazz) {
		return !authorityService.isActionAllowed(clazz, ActionTypeConstants.VIEW_DETAILS, null)
				&& clazz.type().isPartOflibrary();
	}

	/**
	 * Check if the instance is searchable.
	 *
	 * @param classInstance
	 *            the class to check
	 * @return true if is searchable
	 */
	private static boolean isSearchable(ClassInstance classInstance) {
		return classInstance.type().isSearchable();
	}
}
