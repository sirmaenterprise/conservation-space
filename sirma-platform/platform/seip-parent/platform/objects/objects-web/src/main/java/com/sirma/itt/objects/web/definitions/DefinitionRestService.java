/**
 * Copyright (c) 2013 22.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.objects.web.definitions;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.properties.PropertiesConverter;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Provides information about the definitions of the models used in the application.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
@Path("/definition")
// TODO: move - used in UI2
public class DefinitionRestService extends EmfRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	// WILL BE REMOVED
	private static final String PARENT_INSTANCE_ID = "parentInstanceId";
	private static final String OPERATION = "operation";
	private static final String MANDATORY = "mandatory";

	private static final String LABEL = "label";
	private static final String FIELDS = "fields";
	private static final String HEADERS = "headers";

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private TypeMappingProvider typeProvider;

	@Inject
	private PropertiesConverter propertyConverter;

	@Inject
	private DefinitionHelperOld definitionHelperOld;

	@Inject
	private DefinitionHelper definitionHelper;

	@Inject
	private HeadersService headersService;

	/**
	 * Retrieves system or semantic definition fields as is without modifications. Allows batch of definitions to be
	 * retrieved. If no identifiers are requested then return all main searchable (semantic) types
	 *
	 * @param data
	 *            definition identifiers or semantic URIs
	 * @return {@link Response} representing JSON model tree with definition fields and fields parameters
	 */
	@POST
	@Path("/fields")
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveDefinitionFields(String data) {
		JSONObject jsonData = JsonUtil.createObjectFromString(data);
		JSONArray jsonArray = JsonUtil.getJsonArray(jsonData, "identifiers");
		List<String> identifiers = (List<String>) JsonUtil.jsonArrayToList(jsonArray);

		JSONArray result = new JSONArray();
		if (identifiers == null) {
			identifiers = new ArrayList<>();
		}
		if (identifiers.isEmpty()) {
			identifiers.addAll(getMainSearchableTypes());
		}

		for (String identifier : identifiers) {
			DefinitionModel definition = definitionService.find(identifier);
			if (definition != null) {
				JSONObject convert = typeConverter.convert(JSONObject.class, definition);
				JsonUtil.addToJson(convert, LABEL, definitionHelper.getDefinitionLabel(definition));
				JsonUtil.addToJson(result, convert);
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

					final TypeConverter converter = typeConverter;
					JSONArray props = new JSONArray(allDefinitions
							.stream()
							.filter(filterByDefinitionType(typeDef).and(filterByRdfType(rdfTypes)))
							.flatMap(DefinitionModel::fieldsStream)
							.distinct()
							.map(prop -> converter.convert(JSONObject.class, prop))
							.collect(Collectors.toList()));

					JSONObject obj = new JSONObject();
					JsonUtil.addToJson(obj, DefaultProperties.UNIQUE_IDENTIFIER, identifier);
					JsonUtil.addToJson(obj, LABEL, classInstance.getLabel(getlanguage(null)));
					JsonUtil.addToJson(obj, FIELDS, props);
					JsonUtil.addToJson(result, obj);
					LOGGER.trace("Adding {} fields for {}", props.length(), identifier);
				} else {
					LOGGER.info("No datatype definition found for {}", identifier);
				}
			}
		}
		return RestUtil.buildResponse(Response.Status.OK, result.toString());
	}

	/**
	 * Filter definition models if they are of the generic definitions so that they match the type represented by the
	 * given data type definition
	 */
	private Predicate<DefinitionModel> filterByDefinitionType(DataTypeDefinition typeDefinition) {
		return model -> !(model instanceof GenericDefinition)
				|| nullSafeEquals(typeDefinition.getName(), typeProvider.getDataTypeName(model.getType()), true);
	}

	/**
	 * filter definitions that have semantic that match the requested or does not have such field at all (this is for
	 * cases/WF, etc)
	 */
	private static Predicate<DefinitionModel> filterByRdfType(Set<String> types) {
		return model -> {
			Optional<PropertyDefinition> type = model.getField(DefaultProperties.SEMANTIC_TYPE);

			return !type.isPresent() || type.get().getDefaultValue() == null
					|| types.contains(type.get().getDefaultValue());
		};
	}

	/**
	 * @return main searchable types URIs
	 */
	private List<String> getMainSearchableTypes() {
		return definitionService
				.getAllDefinitions()
				.flatMap(toSemanticDefinitions())
				.filter(searchableSemanticDefinition())
				.sorted(sortByLabel())
				.flatMap(toURI())
				.distinct()
				.collect(Collectors.toList());
	}

	private Function<DefinitionModel, Stream<ClassInstance>> toSemanticDefinitions() {
		return model -> {
			Optional<PropertyDefinition> field = model.getField(SEMANTIC_TYPE);
			if (field.isPresent()) {
				String semanticType = field.get().getDefaultValue();
				ClassInstance classInstance = semanticDefinitionService.getClassInstance(semanticType);
				if (org.apache.commons.lang3.StringUtils.isNotBlank(semanticType) && classInstance != null) {
					Stream<ClassInstance> superClasses = classInstance.getSuperClasses().stream();
					return Stream.concat(Stream.of(classInstance), superClasses);
				}
			}
			return Stream.empty();
		};
	}

	private static Predicate<ClassInstance> searchableSemanticDefinition() {
		return classInstance -> classInstance.type().isSearchable();
	}

	private static Function<ClassInstance, Stream<String>> toURI() {
		return classInstance -> Stream.of(classInstance.getId().toString());
	}

	private Comparator<? super ClassInstance> sortByLabel() {
		Collator collator = Collator.getInstance(Locale.forLanguageTag(userPreferences.getLanguage()));
		return (m1, m2) -> collator.compare(m1.getLabel(), m2.getLabel());
	}

	private void addHeadersToModels(Instance instance, JSONObject instanceModels) {
		JSONObject headers = new JSONObject();
		JsonUtil.addToJson(headers, DefaultProperties.HEADER_DEFAULT,
				headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_DEFAULT));
		JsonUtil.addToJson(headers, DefaultProperties.HEADER_COMPACT,
				headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_COMPACT));
		JsonUtil.addToJson(headers, DefaultProperties.HEADER_BREADCRUMB,
				headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_BREADCRUMB));
		JsonUtil.addToJson(instanceModels, HEADERS, headers);
	}

	/**
	 * Retrieve an instance definition model using the actual instance to get the property values.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param instanceId
	 *            the instance id
	 * @param parentInstanceId
	 *            the parent instance id
	 * @param mandatory
	 *            if only field with attribute mandatory=true should be returned
	 * @param operation
	 *            the instance operation is needed because the required fields defined in state transitions can be
	 *            matched using the operation id
	 * @return the definition model
	 */
	@GET
	@Path("{definitionId}/properties")
	public Response propertiesValues(@PathParam(DEFINITION_ID) String definitionId,
			@QueryParam(INSTANCE_ID) String instanceId, @QueryParam(PARENT_INSTANCE_ID) String parentInstanceId,
			@QueryParam(MANDATORY) boolean mandatory, @QueryParam(OPERATION) String operation) {

		DefinitionModel definitionModel;
		Instance instance;
		Instance parentInstance = null;

		boolean isNewInstance = StringUtils.isBlank(instanceId);
		if (isNewInstance) {
			definitionModel = definitionService.find(definitionId);
			if (parentInstanceId != null) {
				parentInstance = fetchInstance(parentInstanceId);
			}
			instance = instanceService.createInstance(definitionModel, parentInstance);
		} else {
			instance = fetchInstance(instanceId);
			definitionModel = definitionService.getInstanceDefinition(instance);
		}

		Map<String, ?> externalModel = propertyConverter.convertToExternalModel(instance, definitionModel);
		Set<String> mandatoryFieldIds = definitionHelperOld.getMandatoryFieldIds(definitionModel, instance, operation);
		List<Ordinal> sortedFields;
		if (mandatory) {
			sortedFields = definitionHelperOld.collectMandatoryFields(definitionModel, mandatoryFieldIds);
		} else {
			sortedFields = definitionHelperOld.collectAllFields(definitionModel);
		}

		JSONObject models = definitionHelperOld.toJsonModel(externalModel, sortedFields, labelProvider,
				mandatoryFieldIds);
		addHeadersToModels(instance, models);
		JsonUtil.addToJson(models, "path", getInstancePath(instance));
		JsonUtil.addToJson(models, "definitionId", definitionModel.getIdentifier());
		JsonUtil.addToJson(models, "definitionLabel", definitionHelper.getDefinitionLabel(definitionModel));
		JsonUtil.addToJson(models, "instanceType", instance.type().getCategory());

		return RestUtil.buildResponse(Status.OK, models.toString());
	}

	private static JSONArray getInstancePath(Instance instance) {
		JSONArray result = new JSONArray();

		for (Instance current : InstanceUtil.getParentPath(instance, true)) {

			JSONObject element = new JSONObject();
			JsonUtil.addToJson(element, "id", current.getId());
			JsonUtil.addToJson(element, "type", instance.type().getCategory());
			JsonUtil.addToJson(element, "compactHeader", current.get(DefaultProperties.HEADER_COMPACT));

			result.put(element);
		}

		return result;
	}

}
