package com.sirmaenterprise.sep.models;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyConsumer;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.content.type.MimeTypeResolver;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirmaenterprise.sep.model.ClassInfo;
import com.sirmaenterprise.sep.model.ModelService;
import com.sirmaenterprise.sep.model.Ontology;

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

	@Inject
	private InstanceService instanceService;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private DictionaryService definitionService;
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
	private InstanceValidationService contextValidationHelper;

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
			purposes
				.stream()
					.map(purpose -> Purpose.parse(purpose))
					.forEach(parsedPurposes::add);
		}
		Set<String> accessibleLibrariesIds = getAccessibleLibrariesIds(parsedPurposes);

		/**
		 * TODO: Filtering by context id doesn't work at all because the <allowedChildren> tag in the // definitions
		 * hasn't been implemented for ui2 for now. Once it is implemented correctly, the contextIdFilter should be
		 * passed to the getAllowedDefinitions method.
		 */
		ModelsInfo modelsInfo = getAllowedDefinitions(null)
				.filter(validDefinition())
					.flatMap(toDefinitionEntry())
					.map(markAccessible(accessibleLibrariesIds))
					.filter(onlyClasses(classFilter))
					.filter(classesForPurpose(parsedPurposes))
					.filter(byDefinition(definitionFilter))
					.peek(byMimeType(mimetypeFilter, fileExtensionFilter))
					.flatMap(toModelInfo())
					.filter(withLabels())
					.distinct()
					.sorted(sortByLabel())
					.reduce(new ModelsInfo(), ModelsInfo::add, ModelsInfo::merge);

		// Validation needs to occur only when the user wants to upload or create an instance in the context.
		if (contextIdFilter != null
				&& (parsedPurposes.contains(Purpose.UPLOAD) || parsedPurposes.contains(Purpose.CREATE))) {
			Optional<String> errorMessage;
			if (parsedPurposes.contains(Purpose.CREATE)) {
				errorMessage = contextValidationHelper.canCreateOrUploadIn(contextIdFilter, Purpose.CREATE.toString());
			} else {
				errorMessage = contextValidationHelper.canCreateOrUploadIn(contextIdFilter, Purpose.UPLOAD.toString());
			}
			if (errorMessage.isPresent()) {
				modelsInfo.setErrorMessage(errorMessage.get());
			}
		}

		if (parsedPurposes.contains(Purpose.SEARCH) && !isEmpty(classFilter)) {
			return modelsInfo.validateAndCleanUpForSearch();
		}
		return modelsInfo.validateAndCleanUp();
	}

	private Set<String> getAccessibleLibrariesIds(EnumSet<Purpose> purposes) {
		Set<String> accessibleLibraries = getAccessibleLibraries(purposes);
		// Added here because they don't have libraries.
		accessibleLibraries.add(USER_ID);
		accessibleLibraries.add(GROUP_ID);

		return accessibleLibraries
				.stream()
					.flatMap(uri -> semanticDefinitionService.collectSubclasses(uri).stream())
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

		// NOTE BBonev: if this is called for search operation will probably produce some unexpected results
		return instanceService
				.getAllowedChildren(contextReference.get().toInstance())
					.values()
					.stream()
					.flatMap(List::stream);
	}

	/**
	 * Valid definition is one that have a type and rdf:type fields. And also points to a valid semantic class that is
	 * defined in the field rdf:type.
	 *
	 * @return the predicate
	 */
	private Predicate<DefinitionModel> validDefinition() {
		return model -> {
			Optional<PropertyDefinition> field = model.getField(SEMANTIC_TYPE);
			if (field.isPresent()) {
				String semanticType = field.get().getDefaultValue();
				return StringUtils.isNotBlank(semanticType)
						&& semanticDefinitionService.getClassInstance(semanticType) != null;
			}
			return false;
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
		return model -> purposes
				.stream()
					.filter(purpose -> model.isClassAllowedFor(purpose))
					.findFirst()
					.isPresent();
	}

	private static Predicate<DefinitionEntry> byDefinition(Set<String> definitionFilter) {
		if (isEmpty(definitionFilter)) {
			return definitionEntry -> true;
		}

		return definitionEntry -> {
			DefinitionModel model = definitionEntry.getModel();
			if (model != null) {
				return definitionFilter.contains(model.getIdentifier());
			}
			return false;
		};
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
			semanticClass = model.getField(SEMANTIC_TYPE).get().getDefaultValue();
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
