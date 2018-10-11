package com.sirmaenterprise.sep.models;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyConsumer;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validator.ExistingInContext;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.content.type.MimeTypeResolver;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.definition.DefinitionInfo;
import com.sirma.sep.model.ClassInfo;
import com.sirma.sep.model.ModelExportRequest;
import com.sirma.sep.model.ModelImportService;
import com.sirma.sep.model.ModelService;
import com.sirma.sep.model.Ontology;

/**
 * Service end point to provide access to the data model structure and meads to modify it
 *
 * @author BBonev
 * @author NikolayCh
 */
@Path("models")
@ApplicationScoped
@Produces(Versions.V2_JSON)
public class ModelsResource {

	private static final String USER_ID = "emf:User";
	private static final String GROUP_ID = "emf:Group";
	private static final String EMAIL_TEMPLATE_TYPE = "emailTemplate";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceService instanceService;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private MimeTypeResolver mimeTypeResolver;
	@Inject
	private InstanceTypeResolver instanceResolver;
	@Inject
	private UserPreferences userPreferences;
	@Inject
	private CodelistService codelistService;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private SearchService searchService;
	@Inject
	private ModelService modelService;
	@Inject
	private ModelImportService modelImportService;
	@Inject
	private InstanceValidationService contextValidationHelper;
	@Inject
	private InstanceAccessEvaluator accessEvaluator;
	@Inject
	private TemplateService templateService;
	@Inject
	private DefinitionImportService definitionImportService;
	@Inject
	private ResourceService resourceService;

	/**
	 * Gets the available models basic information. The returned models could be filtered by different selectors like:
	 * <ul>
	 * <li>purpose of the model (create, upload etc.)
	 * <li>uploading a file with concrete mimetype
	 * <li>uploading file extension
	 * <li>custom class filter
	 * <li>context instance
	 * <li>context instance type
	 * </ul>
	 * Format of the response:
	 *
	 * <pre>
	 * <code>
	 * [{
	 *     "id": "class or definition id",
	 *     "parent": "id of the parent element",
	 *     "label": "Display label of the element",
	 *     "type": "class/definition",
	 *     "default": true/false
	 * }]
	 * </code>
	 * </pre>
	 *
	 * @param classFilter
	 *            optional parameter specifying a set of classes to return only if applicable
	 * @param purposes
	 *            optional parameter specifying the purposes of the operation. Possible values are: create, upload,
	 *            search
	 * @param mimetypeFilter
	 *            optional parameter specifying a mimetype filter to be used when filtering the classes
	 * @param fileExtensionFilter
	 *            optional parameter specifying a file name or file extension to be used for additional mimetype
	 *            filtering of the classes. Could be used with {@code mimetypeFilter}
	 * @param contextIdFilter
	 *            optional parameter specifying the id of context instance to be used for allowed children resolving
	 *            (applicable for create and upload purposes).
	 * @param definitionFilter
	 *            optional parameter specifying a ids of definitions  to be used when filtering allowed definitions.
	 * @return the collected models information
	 */
	@GET
	public ModelsInfo getModelsInfo(@QueryParam("classFilter") Set<String> classFilter,
			@QueryParam("purpose") Set<String> purposes, @QueryParam("mimetype") String mimetypeFilter,
			@QueryParam("extension") String fileExtensionFilter, @QueryParam("contextId") String contextIdFilter,
			@QueryParam("definitionFilter") Set<String> definitionFilter) {

		EnumSet<Purpose> parsedPurposes = EnumSet.noneOf(Purpose.class);
		if (CollectionUtils.isNotEmpty(purposes)) {
			purposes.stream()
					.map(Purpose::parse)
					.forEach(parsedPurposes::add);
		}
		Set<String> accessibleLibrariesIds = getAccessibleLibrariesIds(parsedPurposes);

		//classFilter and definitionFilter are aggregated with OR. If definition's class is present in classFilter then definition is ignored.
		if(!isEmpty(classFilter) && !isEmpty(definitionFilter)) {
			definitionFilter = definitionFilter.stream()
						.map(toDefinitionModel())
						.flatMap(toDefinitionEntry())
						.filter(byNotIncludedInClassFilter(classFilter))
						.filter(definitionEntry -> definitionEntry.getModel() != null)
						.map(definitionEntry -> definitionEntry.getModel().getIdentifier())
						.collect(Collectors.toSet());
		}

		//classNames contain only classes which will be filtered by definition filter.
	    Set<String> classNames = null;
	    Set<String> allClassFilters;
	    if (classFilter != null){
	    	allClassFilters = new HashSet<>(classFilter);
	    } else {
	    	allClassFilters = new HashSet<>();
	    }
		if(!isEmpty(definitionFilter)) {
		   classNames = definitionFilter.stream()
						.map(toDefinitionModel())
						.flatMap(toDefinitionEntry())
						.map(DefinitionEntry::getSemanticClass)
						.collect(Collectors.toSet());
		   allClassFilters.addAll(classNames);
		}

		boolean purposeSearch = parsedPurposes.size() == 1 && parsedPurposes.contains(Purpose.SEARCH);
		ModelsInfo modelsInfo = getAllowedDefinitions(contextIdFilter)
					.filter(filterByExistingInContextDefinitionProperty(contextIdFilter, purposeSearch))
					.filter(validDefinition())
					.flatMap(toDefinitionEntry())
					.map(markAccessible(accessibleLibrariesIds))
					.filter(onlyClasses(allClassFilters))
					.filter(classesForPurpose(parsedPurposes))
					.filter(byDefinition(classNames, classFilter, definitionFilter))
					.peek(byMimeType(mimetypeFilter, fileExtensionFilter))
					.flatMap(toModelInfo())
					.filter(withLabels())
					.distinct()
					.sorted(sortByLabel())
					.reduce(new ModelsInfo(), ModelsInfo::add, ModelsInfo::merge);

		// Validation needs to occur only when the user wants to upload or create an instance in the context.
		if (contextIdFilter != null) {
			canCreateOrUpload(parsedPurposes, contextIdFilter).ifPresent(modelsInfo::setErrorMessage);
		}

		if (parsedPurposes.contains(Purpose.SEARCH) && !isEmpty(classFilter)) {
			return modelsInfo.validateAndCleanUpForSearch();
		}
		return modelsInfo.validateAndCleanUp();
	}
    private Optional<String> canCreateOrUpload(EnumSet<Purpose> parsedPurposes, String contextIdFilter) {
	    return Optional.of(parsedPurposes.stream()
                            .filter(purpose -> Purpose.SEARCH != purpose)
                            .map(purpose -> contextValidationHelper.canCreateOrUploadIn(contextIdFilter, purpose.toString()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.joining(System.lineSeparator())));
    }

	private Set<String> getAccessibleLibrariesIds(EnumSet<Purpose> purposes) {
		Set<String> accessibleLibraries = getAccessibleLibraries(purposes);
		// Added here because they don't have libraries.
		accessibleLibraries.add(USER_ID);
		accessibleLibraries.add(GROUP_ID);

		String actionId = ActionTypeConstants.CREATE;
		if (purposes.contains(Purpose.SEARCH)) {
			actionId = ActionTypeConstants.READ;
		}

		return filterSubclassesByPermissions(accessibleLibraries.stream(), actionId);
	}

	private Set<String> filterSubclassesByPermissions(Stream<String> accessibleLibraries, String actionId) {
		return accessibleLibraries
				.flatMap(uri -> semanticDefinitionService.collectSubclasses(uri).stream())
				.filter(library -> accessEvaluator.actionAllowed(library.getId(), actionId))
				.map(instance -> toFullUri(instance.getId()))
				.collect(Collectors.toSet());
	}

	private Set<String> getAccessibleLibraries(EnumSet<Purpose> purposes) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setMaxSize(-1);
		arguments.getArguments().put("emf:isPartOfObjectLibrary", Boolean.TRUE);
		arguments.getArguments().put("rdf:type", "emf:ClassDescription");

		if (purposes.contains(Purpose.SEARCH)) {
			arguments.setPermissionsType(QueryResultPermissionFilter.READ);
		} else {
			arguments.setPermissionsType(QueryResultPermissionFilter.WRITE);
		}
		searchService.search(Instance.class, arguments);

		return arguments
				.getResult()
					.stream()
					.map(Instance::getId)
					.map(Serializable::toString)
					.collect(Collectors.toSet());
	}

	/**
	 * Gets all ontologies available in the system. </br>
	 * Format of the response:
	 * <pre>
	 * <code>
	 * [{
	 *   	"id": "http://www.ontotext.com/proton/protontop",
	 *	 	"title": "Proton"
	 *	},
	 *  {
	 *		"id": "http://www.w3.org/ns/oa#",
	 *		"title": "Open Annotation Data Model"
	 *	}]
	 * </code>
	 * </pre>
	 *
	 * @return the list of {@link Ontology} objects
	 */
	@GET
	@Path("/ontologies")
	public List<Ontology> getOntologies() {
		return modelService.getOntologies();
	}

	/**
	 * Downloads (exports) the requested models.
	 *
	 * @param request is the export request
	 * @return a response containing the file as application/octet-stream and a header for the file name
	 */
	@POST
	@Path("/download")
	@Consumes(Versions.V2_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportModels(ModelExportRequest request) {
		File file = modelImportService.exportModel(request);

		return Response
					.ok(file, MediaType.APPLICATION_OCTET_STREAM)
					.header("X-File-Name", file.getName())
					.build();
	}

	/**
	 * Gets all models available (imported) in the system.
	 *
	 * @return a {@link ImportedModelsInfo} containing the collections of imported models
	 */
	@GET
	@Path("/imported")
	public ImportedModelsInfo getImportedModels() {
		Map<String, String> headersCache = new HashMap<>();

		List<ImportedDefinition> definitions = definitionImportService
					.getImportedDefinitions()
					.stream()
					.map(definition -> toImportedDefinition(definition, headersCache))
					.collect(Collectors.toList());


		List<ImportedTemplate> templates = templateService
					.getAllTemplates()
					.stream()
					.map(template -> toImportedTemplate(template, headersCache))
					.collect(Collectors.toList());

		return new ImportedModelsInfo(definitions, templates);
	}

	private ImportedTemplate toImportedTemplate(Template template, Map<String, String> headersCache) {
		String title = template.getTitle();
		String forType = template.getForType();
		String modifiedBy = template.getModifiedBy();

		boolean mailTemplate = EMAIL_TEMPLATE_TYPE.equals(template.getForType());
		if (!mailTemplate) {
			Instance instance = fetchInstance(template.getCorrespondingInstance());
			if (instance != null) {
				title = instance.getString(DefaultProperties.HEADER_COMPACT);
				forType = instance.getString(TemplateProperties.FOR_OBJECT_TYPE_LABEL);
			} else {
				LOGGER.warn("Unable to fetch instance for id {}. IDs will be used instead of headers and labels",
						template.getCorrespondingInstance());
			}
		}

		ImportedTemplate imported = new ImportedTemplate();
		imported.setId(template.getId());
		imported.setTitle(title);
		imported.setPrimary(template.getPrimary());
		imported.setPurpose(template.getPurpose());
		imported.setModifiedBy(getUserHeader(modifiedBy, headersCache));
		imported.setModifiedOn(template.getModifiedOn());
		imported.setForObjectType(forType);
		return imported;
	}

	private ImportedDefinition toImportedDefinition(DefinitionInfo definitionInfo, Map<String, String> headersCache) {
		ImportedDefinition model = new ImportedDefinition();

		model.setId(definitionInfo.getId());
		model.setFileName(definitionInfo.getFileName());
		model.setModifiedOn(definitionInfo.getModifiedOn());
		model.setAbstract(definitionInfo.isAbstract());
		model.setModifiedBy(getUserHeader(definitionInfo.getModifiedBy(), headersCache));

		DefinitionModel definition = definitionService.find(definitionInfo.getId());
		Optional<PropertyDefinition> typeOptional = definition.getField("type");

		if (typeOptional.isPresent()) {
			PropertyDefinition type = typeOptional.get();

			if (type.getCodelist() != null && StringUtils.isNotBlank(type.getDefaultValue())) {
				model.setTitle(codelistService.getDescription(type.getCodelist(), type.getDefaultValue()));
			}
		}

		return model;
	}

	private String getUserHeader(String username, Map<String, String> headersCache) {
		if (StringUtils.isBlank(username)) {
			return null;
		}

		String userHeader = headersCache.get(username);

		if (userHeader == null) {
			userHeader = getUserCompactHeader(username);

			if (!StringUtils.isBlank(userHeader)) {
				headersCache.put(username, userHeader);
			} else {
				LOGGER.warn("Unable to fetch user header for {}. User ID will be used instead", username);
			}
		}

		return userHeader;
	}

	private String getUserCompactHeader(String userId) {
		if (StringUtils.isBlank(userId)) {
			return null;
		}
		EmfUser user = resourceService.findResource(userId);
		if (user == null) {
			throw new EmfApplicationException("Unable to retrieve user '" + userId + "'");
		}
		Instance instance = fetchInstance((String) user.getId());
		if (instance == null) {
			return userId;
		}
		return instance.getString(DefaultProperties.HEADER_COMPACT);
	}

	private Instance fetchInstance(String instanceId) {
		if (StringUtils.isBlank(instanceId)) {
			return null;
		}
		Optional<InstanceReference> reference = instanceResolver.resolveReference(instanceId);
		if (!reference.isPresent()) {
			return null;
		}
		return reference.get().toInstance();
	}

	/**
	 * Accepts files as multipart request and imports them using {@link ModelImportService}.
	 *
	 * @param request http request
	 */
	@POST
	@Path("/import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional
	public void importModels(@Context HttpServletRequest request) {
		Map<String, InputStream> fileStreams = new HashMap<>();

		try {
			ServletFileUpload servletUpload = new ServletFileUpload(new DiskFileItemFactory());
			servletUpload.setHeaderEncoding(StandardCharsets.UTF_8.name());

			List<FileItem> fileItems = extractUploadedFiles(request, servletUpload);

			if (fileItems.isEmpty()) {
				throw new ModelImportException(Arrays.asList("No files are provided"));
			}

			List<String> errors = new ArrayList<>();

			for (FileItem item : fileItems) {
				if (fileStreams.containsKey(item.getName())) {
					errors.add("Duplicate files with name '" + item.getName() + "' are provided");
				}

				fileStreams.put(item.getName(), item.getInputStream());
			}

			if (!errors.isEmpty()) {
				throw new ModelImportException(errors);
			}

			errors = modelImportService.importModel(fileStreams);

			if (!errors.isEmpty()) {
				throw new ModelImportException(errors);
			}
		} catch (IOException | FileUploadException e) {
			throw new IllegalStateException(e);
		} finally {
			fileStreams.values().forEach(IOUtils::closeQuietly);
		}
	}

	List<FileItem> extractUploadedFiles(HttpServletRequest request, ServletFileUpload servletUpload)
			throws FileUploadException {
		return servletUpload.parseRequest(request);
	}

	/**
	 * Gets all classes belonging to the given ontology. </br>
	 * Format of the response:
	 *
	 * <pre>
	 * <code>
	 * [{
	 *		"id": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalEvent",
	 *		"label": "Cultural Event",
	 *		"ontology": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain",
	 *		"superClasses": [ "http://www.ontotext.com/proton/protontop#Event" ]
	 *	}]
	 * </code>
	 * </pre>
	 *
	 * @param id
	 *            is the ontology id (URI)
	 * @return the list of {@link ClassInfo} objects
	 */
	@GET
	@Path("ontology/{id}/classes")
	public List<ClassInfo> getClasses(@PathParam(value = "id") String id) {
		return modelService.getClassesForOntology(id);
	}

	/**
	 * Gets the data for the given class ID.
	 *
	 * @param classId
	 *            the Id (URI) of the class
	 * @return the class info
	 */
	@GET
	@Path("/info")
	public ClassInstance getClassInfo(@QueryParam("id") String classId) {
		return semanticDefinitionService.getClassInstance(classId);
	}

	private Stream<DefinitionModel> getAllowedDefinitions(String contextIdFilter) {
		if (StringUtils.isBlank(contextIdFilter)) {
			return definitionService.getAllDefinitions();
		}
		Optional<InstanceReference> contextReference = instanceResolver.resolveReference(contextIdFilter);
		if (!contextReference.isPresent()) {
			// this should probably not happen but you never know
			return definitionService.getAllDefinitions();
		}

		List<DefinitionModel> collect = instanceService.getAllowedChildren(contextReference.get().toInstance())
				.values()
				.stream()
				.flatMap(List::stream)
				.filter(filterOutForbiddenDefinitions())
				.collect(Collectors.toList());
		if (collect.isEmpty()) {
			return definitionService.getAllDefinitions().filter(filterOutForbiddenDefinitions());
		}

		// NOTE BBonev: if this is called for search operation will probably produce some unexpected results
		return collect.stream();
	}

	/**
	 * Fetch value of field {@link DefaultProperties#EXISTING_IN_CONTEXT} from definition with id <code>definitionId</code>.
	 * @param definitionId - the definition id.
	 * @return - value of {@link DefaultProperties#EXISTING_IN_CONTEXT} configuration.
	 */
	@GET
	@Path("/existing-in-context")
	public String getExistingInContext(@QueryParam("definitionId") String definitionId) {
		DefinitionModel model = definitionService.find(definitionId);
		if (model instanceof GenericDefinition) {
			return ((GenericDefinition) model).getConfiguration(DefaultProperties.EXISTING_IN_CONTEXT)
					.map(PropertyDefinition::getDefaultValue)
					.filter(existingInContextValue -> EnumUtils.isValidEnum(ExistingInContext.class,
																			existingInContextValue))
					.orElse(ExistingInContext.BOTH.toString());
		}
		return null;
	}

	/*
	 * This should be called only for create operation and when we have context then users are forbidden for creation.
	 */
	private Predicate<DefinitionModel> filterOutForbiddenDefinitions() {
		Predicate<DefinitionModel> isUserDefinition = model -> model.getField(SEMANTIC_TYPE)
				.map(PropertyDefinition::getDefaultValue)
				.map(value -> typeConverter.tryConvert(Uri.class, value))
				.filter(Objects::nonNull)
				.filter(semanticType -> EMF.USER.toString().equals(semanticType.toString())
						|| EMF.GROUP.toString().equals(semanticType.toString()))
				.isPresent();
		return isUserDefinition.negate();
	}

	/**
	 * Valid definition is one that have a type and rdf:type fields. And also points to a valid semantic class that is
	 * defined in the field rdf:type.
	 *
	 * @return the predicate
	 */
	private Predicate<DefinitionModel> validDefinition() {
		return model -> model.getField(SEMANTIC_TYPE)
				.map(PropertyDefinition::getDefaultValue)
				.filter(StringUtils::isNotBlank)
				.filter(semanticType -> semanticDefinitionService.getClassInstance(semanticType) != null)
				.isPresent();
	}

	/**
	 * Filters definitions based on <code>contextId</code> and <code>purposeSearch</code>.
	 * <pre>
	 *     If <code>purposeSearch</code> is true then filtering will be discarded.
	 *     Otherwise:
	 *        1. If contextId is null all definition marked as exist in context will be filtered.
	 *        2. If contextId is not null all definition marked as exist without context will be filtered.
	 *
	 * </pre>
	 *
	 * @param contextId - requested id.
	 * @param purposeSearch mark filtering to be processed for search purpose;
	 * @return filtered definition.
	 */
	private Predicate<DefinitionModel> filterByExistingInContextDefinitionProperty(String contextId, boolean purposeSearch) {
		return model -> {
			if (purposeSearch) {
				return true;
			}
			if (contextId != null) {
				return contextValidationHelper.canExistInContext(model);
			}
			return contextValidationHelper.canExistWithoutContext(model);
		};
	}

	private Function<DefinitionEntry, DefinitionEntry> markAccessible(Set<String> libraryFilter) {
		return entry -> {
			entry.markAsAccessible(libraryFilter.contains(entry.getSemanticClass()));
			return entry;
		};
	}

	private Function<DefinitionModel, Stream<DefinitionEntry>> toDefinitionEntry() {
		SemanticDefinitionService service = semanticDefinitionService;
		return model -> DefinitionEntry.buildEntries(model, service::getClassInstance).peek(
				entry -> entry.toFullSemanticClass(this::toFullUri));
	}

	private Function<String, DefinitionModel> toDefinitionModel() {
		return definitionId -> definitionService.find(definitionId);
	}


	private Predicate<DefinitionEntry> onlyClasses(Set<String> classFilter) {
		if (isEmpty(classFilter)) {
			return model -> true;
		}

		Set<String> instances = classFilter
				.stream()
					.flatMap(uri -> semanticDefinitionService.collectSubclasses(uri).stream())
					.map(instance -> toFullUri(instance.getId()))
					.collect(Collectors.toSet());

		return model -> instances.contains(model.getSemanticClass());
	}

	private static Predicate<DefinitionEntry> classesForPurpose(EnumSet<Purpose> purposes) {
		if (CollectionUtils.isEmpty(purposes)) {
			return model -> true;
		}

		// Filter out the purposes that are not applicable for the current model. If at least one purpose is left
		// unfiltered, then the model is allowed for at least one of the purposes and must be returned. This will
		// ensure model retrieval based on OR clauses between the different purposes.
		return model -> purposes.stream().anyMatch(model::isClassAllowedFor);
	}

	private Predicate<DefinitionEntry> byDefinition(Set<String> classNames, Set<String> classFilter,
			Set<String> definitionFilter) {
		if (isEmpty(definitionFilter)) {
			return definitionEntry -> true;
		}

		return definitionEntry -> {
			DefinitionModel model = definitionEntry.getModel();
			Set<String> semanticClasses;
			if (model != null) {
				semanticClasses = this.getParentClasses(definitionEntry.getSemanticInstance());
				semanticClasses.add(definitionEntry.getSemanticClass());
			} else {
				semanticClasses = Collections.singleton(definitionEntry.getSemanticClass());
			}

			if (classNames != null && Collections.disjoint(classNames, semanticClasses)) {
				return true;
			}
			if (model != null) {
				return definitionFilter.contains(model.getIdentifier()) || (CollectionUtils.isNotEmpty(classFilter) && !Collections.disjoint(classFilter, semanticClasses));
			}
			return false;
		};
	}

	private static Predicate<DefinitionEntry> byNotIncludedInClassFilter(Set<String> classFilter) {
		return definitionEntry -> !classFilter.contains(definitionEntry.getSemanticClass());
	}


	private Consumer<DefinitionEntry> byMimeType(String mimetypeFilter, String fileExtensionFilter) {
		// we need at least one of the for do a filtering
		if (StringUtils.isBlank(mimetypeFilter) && StringUtils.isBlank(fileExtensionFilter)) {
			return emptyConsumer();
		}
		String mimetype = resolveMimetype(mimetypeFilter, fileExtensionFilter);
		// could not resolve to a valid mimetype
		if (StringUtils.isBlank(mimetype)) {
			return emptyConsumer();
		}
		return model -> model.isAllowedForMimetype(mimetype);
	}

	/**
	 * Resolve to non application/octet-stream mimetype.
	 *
	 * @param mimetypeFilter
	 *            the mimetype filter
	 * @param fileExtensionFilter
	 *            the file extension filter
	 * @return the string
	 */
	private String resolveMimetype(String mimetypeFilter, String fileExtensionFilter) {
		if (StringUtils.isBlank(mimetypeFilter) || MediaType.APPLICATION_OCTET_STREAM.equals(mimetypeFilter)
				&& StringUtils.isNotBlank(fileExtensionFilter)) {
			return mimeTypeResolver.resolveFromName("testName." + fileExtensionFilter);
		}
		return mimetypeFilter;
	}

	private Function<DefinitionEntry, Stream<ModelInfo>> toModelInfo() {
		return entry -> {
			ModelInfo classInfo = new ModelInfo();
			classInfo.setId(entry.getSemanticClass());
			classInfo.setLabel(entry.getSemanticInstance().getLabel(userPreferences.getLanguage()));
			classInfo.setAsClass();
			classInfo.setDefault(entry.isApplicableForDefault());
			classInfo.setIsAccessible(entry.isAccesible);
			classInfo.setCreatable(entry.getSemanticInstance().isCreatable());
			classInfo.setUploadable(entry.getSemanticInstance().isUploadable());
			// get the first searchable class and use it as a parent id, but before that convert the class id to full
			classInfo.setParentId(getParentClass(entry.getSemanticInstance()));

			if (entry.getModel() != null) {
				ModelInfo definitionInfo = new ModelInfo();
				definitionInfo.setId(entry.getModel().getIdentifier());
				definitionInfo.setLabel(getDefinitionLabel(entry.getModel()));
				definitionInfo.setAsDefinition();
				definitionInfo.setParentId(entry.getSemanticClass());
				definitionInfo.setIsAccessible(entry.isAccesible);
				return Stream.of(classInfo, definitionInfo);
			}
			return Stream.of(classInfo);
		};
	}

	private String getParentClass(ClassInstance semanticInstance) {
		return semanticInstance
				.getSuperClasses()
					.stream()
					.filter(sc -> sc.type().isSearchable())
					.findAny()
					.map(ClassInstance::getId)
					.map(this::toFullUri)
					.orElse(null);
	}

	private Set<String> getParentClasses(ClassInstance semanticInstance) {
		return semanticInstance
				.getSuperClasses()
					.stream()
					.filter(sc -> sc.type().isSearchable())
					.map(ClassInstance::getId)
					.map(this::toFullUri)
					.collect(Collectors.toSet());
	}

	private String toFullUri(Object uri) {
		return typeConverter.convert(Uri.class, uri).toString();
	}

	private String getDefinitionLabel(DefinitionModel model) {
		// this will allow definitions without type
		// if more than one definition per class will result in displaying invalid labels if any
		Optional<PropertyDefinition> type = model.getField(DefaultProperties.TYPE);
		if (!type.isPresent()) {
			return model.getIdentifier();
		}
		PropertyDefinition typeField = type.get();
		Integer codelist = typeField.getCodelist();

		String label = null;
		if (codelist != null && typeField.getDefaultValue() != null) {
			label = codelistService.getDescription(codelist, typeField.getDefaultValue());
			// after this like the label could be null if the codelist service does no know for the codelist/value, yet.
		}
		if (label == null) {
			label = model.getIdentifier();
		}
		return label;
	}

	private static Predicate<ModelInfo> withLabels() {
		return model -> StringUtils.isNotBlank(model.getLabel());
	}

	private Comparator<? super ModelInfo> sortByLabel() {
		Collator collator = Collator.getInstance(Locale.forLanguageTag(userPreferences.getLanguage()));
		return (m1, m2) -> collator.compare(m1.getLabel(), m2.getLabel());
	}

	/**
	 * The purpose of the operation models get operation
	 *
	 * @author BBonev
	 */
	private enum Purpose {
		CREATE, UPLOAD, SEARCH;

		/**
		 * Parses the input argument to enum value by converting it to upper case first
		 *
		 * @param value
		 *            the value
		 * @return the purpose
		 */
		static Purpose parse(String value) {
			if (value == null) {
				return null;
			}
			return valueOf(value.toUpperCase());
		}
	}

	/**
	 * Internal wrapper object during models information retrieval
	 *
	 * @author BBonev
	 */
	private static class DefinitionEntry {
		private DefinitionModel model;
		private String semanticClass;
		private ClassInstance semanticInstance;
		private boolean applicableForDefault;
		private boolean isAccesible;

		/**
		 * Instantiates a new definition entry.
		 *
		 * @param model
		 *            the model
		 * @param instanceProvider
		 *            the instance provider
		 */
		DefinitionEntry(DefinitionModel model, Function<String, ClassInstance> instanceProvider) {
			this.model = model;
			semanticClass = model.getField(SEMANTIC_TYPE).orElseThrow(IllegalStateException::new).getDefaultValue();
			semanticInstance = instanceProvider.apply(semanticClass);
		}

		/**
		 * Instantiates a new definition entry.
		 *
		 * @param classInstance
		 *            the class instance
		 */
		DefinitionEntry(ClassInstance classInstance) {
			semanticClass = classInstance.getId().toString();
			semanticInstance = classInstance;
		}

		/**
		 * Instantiates a new definition entry.
		 *
		 * @param model
		 *            the model
		 * @param instanceProvider
		 *            the instance provider
		 * @return the stream of entry for the given definition and the super classes of the associated semantic class
		 */
		static Stream<DefinitionEntry> buildEntries(DefinitionModel model,
				Function<String, ClassInstance> instanceProvider) {
			List<DefinitionEntry> entries = new LinkedList<>();
			DefinitionEntry base = new DefinitionEntry(model, instanceProvider);
			entries.add(base);
			Stream<DefinitionEntry> superClasses = Stream.empty();
			if (base.semanticInstance != null) {
				// return that super classes that are at least searchable/createable or uploadable
				superClasses = base.semanticInstance
						.getSuperClasses()
							.stream()
							.filter(sc -> sc.type().isSearchable() || sc.type().isCreatable()
									|| sc.type().isUploadable())
							.map(DefinitionEntry::new);
			}

			return Stream.concat(Stream.of(base), superClasses);
		}

		DefinitionModel getModel() {
			return model;
		}

		String getSemanticClass() {
			return semanticClass;
		}

		void toFullSemanticClass(Function<Object, String> toFullUri) {
			semanticClass = toFullUri.apply(semanticClass);
		}

		ClassInstance getSemanticInstance() {
			return semanticInstance;
		}

		boolean isApplicableForDefault() {
			return applicableForDefault;
		}

		void markAsAccessible(boolean isAccessible) {
			isAccesible = isAccessible;
		}

		/**
		 * Checks if is class allowed for the given purpose
		 *
		 * @param purpose
		 *            the purpose
		 * @return true, if is class allowed for
		 */
		boolean isClassAllowedFor(Purpose purpose) {
			switch (purpose) {
			case CREATE:
				return semanticInstance.type().isCreatable();
			case UPLOAD:
				return semanticInstance.type().isUploadable();
			case SEARCH:
				return semanticInstance.type().isSearchable();
			default:
				break;
			}
			return false;
		}

		/**
		 * Checks if is allowed for mimetype.
		 *
		 * @param mimetype
		 *            the mimetype
		 * @return true, if is allowed for mimetype
		 */
		boolean isAllowedForMimetype(String mimetype) {
			boolean matchPattern = semanticInstance.type().isAllowedForMimetype(mimetype);
			// if the pattern matching is supported and we have positive match we may consider the model for default
			applicableForDefault = semanticInstance.type().isDataTypePatternSupported() && matchPattern;
			return matchPattern;
		}
	}
}