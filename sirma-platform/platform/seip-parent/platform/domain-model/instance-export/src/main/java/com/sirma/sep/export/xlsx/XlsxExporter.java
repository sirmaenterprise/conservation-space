package com.sirma.sep.export.xlsx;

import static java.util.Collections.singletonList;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesDao;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.FileExporter;
import com.sirma.sep.export.SupportedExportFormats;
import com.sirma.sep.export.renders.BaseRenderer;
import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.xlsx.components.ExportExcelCell;
import com.sirma.sep.export.xlsx.components.ExportExcelDocument;
import com.sirma.sep.export.xlsx.components.ExportExcelLink;
import com.sirma.sep.export.xlsx.components.ExportExcelRow;
import com.sirma.sep.export.xlsx.components.HSSFExportExcelDocument;
import com.sirma.sep.export.xlsx.components.SXSSExportExcelDocument;

/**
 * Provides means for exporting specified objects/instances to MS Excel document. The implementation uses third party
 * library (apache.poi) to create the document and insert the data in it. <br>
 * This implementation is extension to {@link FileExporter} plug-in and should be used through specified service for the
 * export - {@link ExportService}. <br>
 * The export process will retrieve and load the objects/instances, which data should be exported in the Excel. Then for
 * every object/instance will be build table row with the retrieved data for that object. After that the from the rows
 * will be build table in HTML format, which is passed to the external library, which creates Excel document from
 * it.<br>
 * For performance reasons the extracting of the instance data and converting it to table row is done on batches. The
 * minimal batch size is 256 and the maximal 1024. The exact batch size is configurable, but it is bound to that range,
 * otherwise we risk to request for loading to much data at once, which may slow the performance of our data bases.<br>
 * For additional performance the headers of the objects will be stored in local cache and reused, where they are
 * required, until the export process is done.
 *
 * Annotation {@link RequestScoped} is needed to ensure that {@link XlsxExporter#objectHeadersInfoCache} will be
 * initialized every time when {@link XlsxExporter#export} method is called.
 *
 * @author gshefkedov
 * @author A. Kunchev
 */
@Extension(target = FileExporter.PLUGIN_NAME, order = 30)
@RequestScoped
public class XlsxExporter implements FileExporter<XlsxExportRequest> {

	private static final String EXPORT = "export-xlsx";
	private static final String HEADER_KEY_SYSTEM_ID = "exportXlsx.header.system.id.label";
	private static final String PROPERTY_IDENTIFIER_SYSTEM_ID = "systemId";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "export.excel.batch.size", system = true, type = Integer.class, defaultValue = "500", label = "Export to excel processing batch size.")
	private ConfigurationProperty<Integer> exportExcelBatchSize;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private HeadersService headersService;

	@Inject
	private SearchService searchService;

	@Inject
	private JsonToConditionConverter jsonToConditionConverter;

	@Inject
	private LockService lockService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private RichtextPropertiesDao richtextPropertiesDao;

	@Inject
	@SemanticDb
	private DbDao dbDao;

	/**
	 * Cache hold information needed for link creation. Key is instance id. For example:
	 * "emf:fb4e9a5f-1224-45e0-a164-d9e85a974ef6" Value is ExportExcelLink contains address and label of link to
	 * instance with id the key of map.
	 */
	private Map<Serializable, ExportExcelLink> objectHeadersInfoCache = new HashMap<>();

	/**
	 * List contains instance property converters. InstancePropertyToExcelCellConverter is function which convert
	 * instance property to excel cell.
	 */
	private List<InstancePropertyToExcelCellConverter> instancePropertyConverter;

	private int batchSize = 1024;

	/**
	 * Initialize property converters.
	 */
	@PostConstruct
	public void init() {
		// if you add new converter be carefully. Order of converters is very important because first found convertor
		// will be used.
		instancePropertyConverter = new LinkedList<>();
		instancePropertyConverter.add(lockedByPropertyToExcelCellConverter());
		instancePropertyConverter.add(multiValuedCodeListValueToExcelCellConverter());
		instancePropertyConverter.add(singleCodeListValueToExcelCellConverter());
		instancePropertyConverter.add(codeDatetimeToExcelCellConverter());
		instancePropertyConverter.add(codeDateToExcelCellConverter());
		instancePropertyConverter.add(codeObjectPropertyToExcelCellConverter());
		instancePropertyConverter.add(codeRichtextPropertyToExcelCellConverter());
		instancePropertyConverter.add(codeControlDefinitionPropertyToExcelCellConverter());
		batchSize = Math.min(1024, Math.max(256, exportExcelBatchSize.get()));
	}

	@Override
	public Optional<File> export(XlsxExportRequest request) throws ContentExportException {
		Map<String, String> headersInfo = request.getTableConfiguration().getHeadersInfo();
		if (request.getTableConfiguration().showInstanceId()) {
			headersInfo.put(PROPERTY_IDENTIFIER_SYSTEM_ID, labelProvider.getLabel(HEADER_KEY_SYSTEM_ID));
		}
		List<String> instanceIds = getDisplayedObjects(request);
		ExportExcelDocument excelDocument = initDocumentExporter(instanceIds, headersInfo);
		populateInstancePropertiesToExcelDocument(instanceIds, excelDocument, request);
		File exportDir = tempFileProvider.createLongLifeTempDir(EXPORT);
		String fileName = EqualsHelper.getOrDefault(request.getFileName(), UUID.randomUUID().toString());
		File exportedFile = new File(exportDir, fileName + excelDocument.getFileExtension());
		excelDocument.writeToFile(exportedFile);
		return Optional.of(exportedFile);
	}

	/**
	 * Retrieves the ids of the objects that should be populated in the file.
	 *
	 * @param request
	 *            the request containing the required data for the export
	 * @return the ids of the object that should be populated in the rows
	 */
	private List<String> getDisplayedObjects(XlsxExportRequest request) {
		if (request.getTableConfiguration().isManuallySelected()) {
			return request.getObjectsData().getManuallySelectedObjects();
		}

		return getAutomaticallySelected(request.getSearchData());
	}

	private ExportExcelDocument initDocumentExporter(List<String> instanceIds, Map<String, String> headersInfo) {
		if (richtextPropertiesDao.fetchByInstanceIds(instanceIds).isEmpty()) {
			return new SXSSExportExcelDocument(headersInfo, batchSize);
		}
		return new HSSFExportExcelDocument(headersInfo);
	}

	/**
	 * Execute search to fetch selected instance.
	 *
	 * @return list with ids of instances returned from the search
	 */
	private List<String> getAutomaticallySelected(SearchData searchData) {
		Condition tree = jsonToConditionConverter.parseCondition(searchData.getSearchCriteria());
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(0));
		searchRequest.setSearchTree(tree);
		SearchArguments<Instance> searchArgs = searchService.parseRequest(searchRequest);
		searchArgs.setPageSize(searchArgs.getMaxSize());
		searchArgs.setPageNumber(1);
		String orderBy = searchData.getOrderBy();
		String orderDirection = searchData.getOrderDirection();
		if (!orderBy.isEmpty() && !orderDirection.isEmpty()) {
			// clear default modifiedOn sorter
			searchArgs.getSorters().clear();
			searchArgs.addSorter(new Sorter(orderBy, orderDirection));
		}

		searchService.search(Instance.class, searchArgs);
		return searchArgs
				.getResult()
					.stream()
					.map(instance -> instance.getId().toString())
					.collect(Collectors.toList());
	}

	/**
	 * Extract properties of <code>instances</code> and populate it in <code>excelDocument</code>.
	 */
	private void populateInstancePropertiesToExcelDocument(Collection<String> instances,
			ExportExcelDocument excelDocument, XlsxExportRequest request) {
		FragmentedWork.doWork(instances, batchSize, batchedInstances -> {
			Collection<Instance> loadedInstance = resolveInstances(batchedInstances, true);
			updateObjectHeadersCache(loadedInstance, request.getObjectsData().getInstanceHeaderType());
			loadHeaderOfObjectProperties(loadedInstance, request);
			loadedInstance
					.stream()
						.map(instance -> processInstance(instance, request))
						.forEach(excelDocument::populateRow);
		});
	}

	/**
	 * Resolve instances for the given instance ids. The result collection will contain only instances that are found.
	 *
	 * @param instancesIds
	 *            the list with instancesIds ids to be resolved
	 * @param loadObjectProperties
	 *            if true object and data properties will be loaded. If false data properties only
	 * @return loaded instancesIds
	 */
	private Collection<Instance> resolveInstances(Collection<String> instancesIds, boolean loadObjectProperties) {
		String query = loadObjectProperties ? NamedQueries.SELECT_BY_IDS : NamedQueries.SELECT_DATA_PROPERTIES_BY_IDS;
		Collection<Instance> loadedInstances = dbDao.fetchWithNamed(query,
				singletonList(new Pair<>(NamedQueries.Params.URIS, instancesIds)));
		Map<String, Map<String, Serializable>> instanceProperties = richtextPropertiesDao
				.fetchByInstanceIds((List<String>) instancesIds);
		if (!instanceProperties.isEmpty()) {
			loadedInstances.forEach(instance -> instance.addAllProperties(instanceProperties.get(instance.getId())));
		}
		return loadedInstances;
	}

	/**
	 * Generate links of <code>instances</code> and add it to <code>objectHeadersInfoCache</code>.
	 */
	private void updateObjectHeadersCache(Collection<Instance> instances, String instanceHeaderType) {
		instances.stream().filter(instance -> !objectHeadersInfoCache.containsKey(instance.getId())).forEach(
				instance -> objectHeadersInfoCache.put(instance.getId(),
						sanitizeHeader(headersService.generateInstanceHeader(instance, instanceHeaderType))));
	}

	private void populateSubProperties(ExportExcelRow row, Instance instance, String propertyName,
			Set<String> selectedSubProperties, String instanceHeaderType) {
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		selectedSubProperties.forEach(subPropertyName -> {
			String key = propertyName + IdocRenderer.SEPARATOR + subPropertyName;
			instanceDefinition.getField(subPropertyName).ifPresent(propertyDefinition -> {
				Serializable subPropertyValue = instance.get(subPropertyName);
				// If processed sub property is object property we will load headers of all its related objects.
				if (PropertyDefinition.isObjectProperty().test(propertyDefinition)) {
					// We will load them on batch, because there can be many.
					FragmentedWork.doWork(getObjectPropertyObjectsIds(subPropertyValue).collect(Collectors.toList()), batchSize, batchedInstances -> {
						Collection<Instance> loadedInstances = resolveInstances(
								getObjectPropertyObjectsIds(instance.get(subPropertyName)).collect(Collectors.toList()),
								true);
						updateObjectHeadersCache(loadedInstances, instanceHeaderType);
					});
				}
				row.addProperty(key, processSubProperty(instance, instanceDefinition, subPropertyName, subPropertyValue));
			});
		});
	}

	/**
	 * Removes html tags from header and extracts url from href if exist.
	 *
	 * @param header
	 *            the header type of the widget
	 * @return header of instance
	 */
	private ExportExcelLink sanitizeHeader(String header) {
		if (header == null) {
			return new ExportExcelLink("", "");
		}

		Document heading = Jsoup.parse(header);
		// getting <a> element from header
		Elements links = heading.select("a");
		String path = links.attr("href");
		return new ExportExcelLink(getInstanceUrl(path), heading.text());
	}

	private String getInstanceUrl(String path) {
		String ui2url = systemConfiguration.getUi2Url().get();
		return path.contains(ui2url) ? path : ui2url + path;
	}

	/**
	 * Iterate over <code>instances</code> and fetch all object property ids. Load them and update header cache.
	 */
	private void loadHeaderOfObjectProperties(Collection<Instance> instances, XlsxExportRequest request) {
		final DefinitionService localDefinitionService = definitionService;
		final LockService localLockService = lockService;
		String instanceHeaderType = request.getObjectsData().getInstanceHeaderType();
		Map<String, List<String>> selectedProperties = request.getObjectsData().getSelectedProperties();
		// iterate over instances and calculate all ids of objects which have not header info.
		List<String> objectsIds = instances.stream().flatMap(instance -> {
			DefinitionModel instanceDefinition = localDefinitionService.getInstanceDefinition(instance);
			return getInstanceSelectedProperties(instance, selectedProperties)
					.stream()
						.filter(isOblectProperty(instanceDefinition))
						.map(checkLockStatus(instance, localLockService))
						.filter(Objects::nonNull)
						.flatMap(XlsxExporter::getObjectPropertyObjectsIds);
		}).distinct().filter(objectId -> !objectHeadersInfoCache.containsKey(objectId)).collect(Collectors.toList());

		FragmentedWork.doWork(objectsIds, batchSize, batchedInstances -> {
			Collection<Instance> loadedInstance = resolveInstances(batchedInstances, false);
			updateObjectHeadersCache(loadedInstance, instanceHeaderType);
		});
	}

	/**
	 * Fetch selected properties for <code>instance</code>.
	 *
	 * @param instance
	 *            to be processed
	 * @return list with properties of instance which have to be processed
	 */
	private static List<String> getInstanceSelectedProperties(Instance instance,
			Map<String, List<String>> selectedProperties) {
		return getInstanceSelectedPropertiesByInstanceIdentifier(instance, selectedProperties)
				.orElseGet(() -> getInstanceSelectedPropertiesByInstanceTypeId(instance, selectedProperties)
						.orElseGet(() -> getInstanceSelectedPropertiesByInstanceParentType(instance, selectedProperties)
								.orElseGet(Collections::emptyList)));
	}

	/**
	 * Fetch selected properties for <code>instance</code> by instance identifier.
	 *
	 * @param instance
	 *            the processed instance
	 * @return list with selected properties or optional empty
	 */
	private static Optional<List<String>> getInstanceSelectedPropertiesByInstanceIdentifier(Instance instance,
			Map<String, List<String>> selectedProperties) {
		return Optional.ofNullable(selectedProperties.get(instance.getIdentifier()));
	}

	/**
	 * Fetch selected properties for <code>instance</code> by instance type id.
	 *
	 * @param instance
	 *            the processed instance
	 * @return list with selected properties or optional empty
	 */
	private static Optional<List<String>> getInstanceSelectedPropertiesByInstanceTypeId(Instance instance,
			Map<String, List<String>> selectedProperties) {
		return Optional.ofNullable(selectedProperties.get(instance.type().getId().toString()));
	}

	/**
	 * Fetch selected properties for <code>instance</code> by parent type.
	 *
	 * @param instance
	 *            the processed instance
	 * @return list with selected properties or optional empty
	 */
	private static Optional<List<String>> getInstanceSelectedPropertiesByInstanceParentType(Instance instance,
			Map<String, List<String>> selectedProperties) {
		Set<InstanceType> allSuperTypes = new HashSet<>();
		fetchAllSuperTypes(instance.type(), allSuperTypes);
		return allSuperTypes
				.stream()
					.map(superType -> selectedProperties.get(superType.getId().toString()))
					.filter(Objects::nonNull)
					.findFirst();
	}

	/**
	 * Fetch recursively all super types of <code>instanceType</code>.
	 *
	 * @param instanceType
	 *            the instance type which super types have to be fetched
	 * @param allSuperTypes
	 *            set containing all super types
	 */
	private static void fetchAllSuperTypes(InstanceType instanceType, Set<InstanceType> allSuperTypes) {
		Set<InstanceType> superTypes = instanceType.getSuperTypes();
		if (superTypes.isEmpty()) {
			return;
		}

		for (InstanceType supertype : superTypes) {
			allSuperTypes.add(supertype);
			fetchAllSuperTypes(supertype, allSuperTypes);
		}
	}

	private static Predicate<String> isOblectProperty(DefinitionModel definition) {
		return property -> definition.getField(property).filter(PropertyDefinition.isObjectProperty()).isPresent();
	}

	private static Function<String, Serializable> checkLockStatus(Instance instance, LockService lockService) {
		return propertyName -> {
			if (DefaultProperties.LOCKED_BY.equals(propertyName)) {
				LockInfo lockInfo = lockService.lockStatus(instance.toReference());
				return lockInfo != null ? lockInfo.getLockedBy() : null;
			}

			return instance.get(propertyName);
		};
	}

	/**
	 * Create stream of objects ids from <code>propertyValue</code>.
	 *
	 * @param propertyValue
	 *            to be parsed
	 * @return stream of objects ids
	 */
	@SuppressWarnings("unchecked")
	private static Stream<String> getObjectPropertyObjectsIds(Serializable propertyValue) {
		if (propertyValue instanceof Collection<?>) {
			return ((Collection<String>) propertyValue).stream();
		} else if (propertyValue == null || StringUtils.EMPTY.equals(propertyValue)) {
			return Stream.empty();
		}

		return Stream.of(String.valueOf(propertyValue));
	}

	/**
	 * Create a excel row and populate cells of it with <code>instance</code> properties.
	 *
	 * @return the excel row with populated properties of instance
	 */
	private ExportExcelRow processInstance(Instance instance, XlsxExportRequest request) {
		ExportExcelRow row = new ExportExcelRow();
		// Add link to instance.
		String instanceHeaderType = request.getObjectsData().getInstanceHeaderType();
		row.addProperty(instanceHeaderType, new ExportExcelCell(instanceHeaderType,
				objectHeadersInfoCache.get(instance.getId()), ExportExcelCell.Type.LINK));
		// process properties of instance
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		Map<String, List<String>> selectedProperties = request.getObjectsData().getSelectedProperties();
		List<String> instanceSelectedProperties = getInstanceSelectedProperties(instance, selectedProperties);
		instanceSelectedProperties.forEach(propertyName -> {
			row.addProperty(propertyName, processProperty(instance, instanceDefinition, propertyName));
			processSubProperties(instance, instanceDefinition, propertyName, request, row);
			// process sub properties
		});
		if (request.getTableConfiguration().showInstanceId()) {
			row.addProperty(PROPERTY_IDENTIFIER_SYSTEM_ID, new ExportExcelCell("systemIds", instance.getId(), ExportExcelCell.Type.OBJECT));
		}
		return row;
	}

	private void processSubProperties(Instance instance, DefinitionModel instanceDefinition, String propertyName,
			XlsxExportRequest request, ExportExcelRow row) {

		Set<String> selectedSubProperties = request.getObjectsData().getSelectedSubProperties().get(propertyName);
		if (selectedSubProperties == null) {
			return;
		}
		instanceDefinition.getField(propertyName).ifPresent(propertyDefinition -> {
			List<String> relatedObjectIds = getObjectPropertyObjectsIds(
					instance.get(propertyDefinition.getIdentifier())).collect(Collectors.toList());
			if (relatedObjectIds.isEmpty()) {
				// There is not selected sub properties. Nothing to do.
				return;
			}
			if (relatedObjectIds.size() > 1) {
				//We do not process property with more than one value. This will be future requirement.
				//For now we just populate those excel cells with label "widget.export.multiple.objects"
				selectedSubProperties.forEach(subPropertyName -> {
					String key = propertyName + IdocRenderer.SEPARATOR + subPropertyName;
					row.addProperty(key,
									new ExportExcelCell(key, labelProvider.getLabel(IdocRenderer.KEY_LABEL_MULTIPLE_OBJECTS),
														ExportExcelCell.Type.OBJECT));
				});
				return;
			}
			Collection<Instance> loadedInstances = resolveInstances(relatedObjectIds, true);
			populateSubProperties(row, loadedInstances.iterator().next(), propertyName, selectedSubProperties,
								  request.getObjectsData().getInstanceHeaderType());
		});
	}

	/**
	 * Extract value of property with <code>propertyName</code> of <code>instance</code> and convert to excel cell.
	 *
	 * @param instance
	 *            the instance
	 * @param instanceDefinition
	 *            definition model of instance
	 * @param propertyName
	 *            the property name which will be processed
	 * @return excel cell with populated value of property
	 */
	private ExportExcelCell processProperty(Instance instance, DefinitionModel instanceDefinition,
			String propertyName) {
		Optional<PropertyDefinition> definitionProperty = instanceDefinition.getField(propertyName);
		if (definitionProperty.isPresent()) {
			Serializable propertyValue = instance.get(definitionProperty.get().getIdentifier());
			return convertPropertyValue(instance, definitionProperty.get(), propertyName, propertyValue);
		}

		return new ExportExcelCell(propertyName, "", ExportExcelCell.Type.OBJECT);
	}

	private ExportExcelCell processSubProperty(Instance instance, DefinitionModel instanceDefinition,
			String propertyName, Serializable propertyValue) {
		Optional<PropertyDefinition> definitionProperty = instanceDefinition.getField(propertyName);
		if (definitionProperty.isPresent()) {
			return convertPropertyValue(instance, definitionProperty.get(), propertyName, propertyValue);
		}

		return new ExportExcelCell(propertyName, "", ExportExcelCell.Type.OBJECT);
	}

	/**
	 * Convert <code>propertyValue</code> to excel cell.
	 */
	private ExportExcelCell convertPropertyValue(Instance instance, PropertyDefinition definitionProperty,
			String propertyName, Serializable propertyValue) {
		return instancePropertyConverter
				.stream()
					.map(converter -> converter.convert(instance, definitionProperty, propertyName, propertyValue))
					.filter(Objects::nonNull)
					.findFirst()
					.orElseGet(() -> new ExportExcelCell(propertyName, propertyValue, ExportExcelCell.Type.OBJECT));
	}

	/**
	 * Functional interface for property converters.
	 */
	@FunctionalInterface
	private interface InstancePropertyToExcelCellConverter {

		/**
		 * Convert instance property to excel cell.
		 *
		 * @param instance
		 *            processed instance
		 * @param propertyDefinition
		 *            definition of property
		 * @param propertyName
		 *            the name of property
		 * @param propertyValue
		 *            the value of property
		 * @return converted instance property
		 */
		ExportExcelCell convert(Instance instance, PropertyDefinition propertyDefinition, String propertyName,
				Serializable propertyValue);
	}

	/**
	 * Create code list value to excel cell converter.
	 *
	 * @return single value code list converter
	 */
	private InstancePropertyToExcelCellConverter singleCodeListValueToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || !isSingleCodeListProperty().test(definitionProperty)) {
				return null;
			}

			Integer codelist = definitionProperty.getCodelist();
			String value = Optional
					.ofNullable(codelistService.getDescription(codelist, propertyValue.toString()))
						.orElse(propertyValue.toString());
			return new ExportExcelCell(propertyName, value, ExportExcelCell.Type.OBJECT);
		};
	}

	private static Predicate<PropertyDefinition> isSingleCodeListProperty() {
		return definitionProperty -> PropertyDefinition.hasCodelist().test(definitionProperty)
				&& !definitionProperty.isMultiValued();
	}

	/**
	 * Create code list value to excel cell converter.
	 *
	 * @return multivalue code list converter
	 */
	@SuppressWarnings("unchecked")
	private InstancePropertyToExcelCellConverter multiValuedCodeListValueToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || !isMultiValuedCodeListProperty().test(definitionProperty)) {
				return null;
			}

			Integer codelist = definitionProperty.getCodelist();
			String codeListValue = ((Collection<String>) propertyValue)
					.stream()
						.map(value -> codelistService.getDescription(codelist, value))
						.filter(StringUtils::isNotBlank)
						.collect(Collectors.joining("\n"));
			return new ExportExcelCell(propertyName, codeListValue, ExportExcelCell.Type.OBJECT);
		};
	}

	private static Predicate<PropertyDefinition> isMultiValuedCodeListProperty() {
		return definitionProperty -> PropertyDefinition.hasCodelist().test(definitionProperty)
				&& definitionProperty.isMultiValued();
	}

	/**
	 * Create datetime value to excel cell converter.
	 *
	 * @return datetime converter
	 */
	private InstancePropertyToExcelCellConverter codeDatetimeToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || !isDateTimeProperty().test(definitionProperty)) {
				return null;
			}

			FormattedDateTime formattedDateTime = typeConverter.convert(FormattedDateTime.class, propertyValue);
			return new ExportExcelCell(propertyName, formattedDateTime.getFormatted(), ExportExcelCell.Type.OBJECT);
		};
	}

	private static Predicate<PropertyDefinition> isDateTimeProperty() {
		return definitionProperty -> DataTypeDefinition.DATETIME.equals(definitionProperty.getType());
	}

	/**
	 * Create date value to excel cell converter.
	 *
	 * @return date converter
	 */
	private InstancePropertyToExcelCellConverter codeDateToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || !isDateProperty().test(definitionProperty)) {
				return null;
			}

			FormattedDate formattedDateTime = typeConverter.convert(FormattedDate.class, propertyValue);
			return new ExportExcelCell(propertyName, formattedDateTime.getFormatted(), ExportExcelCell.Type.OBJECT);
		};
	}

	private static Predicate<PropertyDefinition> isDateProperty() {
		return definitionProperty -> DataTypeDefinition.DATE.equals(definitionProperty.getType());
	}

	/**
	 * Create object property value to excel cell converter.
	 *
	 * @return object property converter
	 */
	private InstancePropertyToExcelCellConverter codeObjectPropertyToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || !isObjectProperty().test(definitionProperty)) {
				return null;
			}

			ArrayList<ExportExcelLink> exportExcelLinks = resolveObjectHeaderInfo(propertyValue);
			if (exportExcelLinks.size() == 1) {
				return new ExportExcelCell(propertyName, exportExcelLinks.get(0), ExportExcelCell.Type.LINK);
			}

			// if we have more than one link we will list headers only
			Serializable linkLabel = exportExcelLinks
					.stream()
						.map(ExportExcelLink::getLabel)
						.filter(StringUtils::isNotBlank)
						.collect(Collectors.joining("\n"));
			return new ExportExcelCell(propertyName, linkLabel, ExportExcelCell.Type.OBJECT);
		};
	}

	private static Predicate<PropertyDefinition> isObjectProperty() {
		return definitionProperty -> PropertyDefinition.isObjectProperty().test(definitionProperty);
	}

	private ArrayList<ExportExcelLink> resolveObjectHeaderInfo(Serializable propertyValue) {
		return getObjectPropertyObjectsIds(propertyValue)
				.map(objectHeadersInfoCache::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Create control definition value to excel cell converter.
	 *
	 * @return control definition property converter
	 */
	private static InstancePropertyToExcelCellConverter codeControlDefinitionPropertyToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || !isControlDefinitionProperty().test(definitionProperty)) {
				return null;
			}

			Serializable label = BaseRenderer.getControlDefinitionLabel(propertyValue,
					definitionProperty.getControlDefinition());
			return new ExportExcelCell(propertyName, label, ExportExcelCell.Type.OBJECT);
		};
	}

	/**
	 * Create control definition value to excel cell converter.
	 *
	 * @return control definition property converter
	 */
	private static InstancePropertyToExcelCellConverter codeRichtextPropertyToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (propertyValue == null || definitionProperty.getControlDefinition() == null || !IdocRenderer.RICHTEXT
					.equalsIgnoreCase(definitionProperty.getControlDefinition().getIdentifier())) {
				return null;
			}
			Serializable label = BaseRenderer.getControlDefinitionLabel(propertyValue,
					definitionProperty.getControlDefinition());
			return new ExportExcelCell(propertyName, label, ExportExcelCell.Type.RICHTEXT);
		};
	}

	private static Predicate<PropertyDefinition> isControlDefinitionProperty() {
		return definitionProperty -> definitionProperty.getControlDefinition() != null;
	}

	/**
	 * "lockedBy" to excel cell converter
	 *
	 * @return lockedBy property converter
	 */
	private InstancePropertyToExcelCellConverter lockedByPropertyToExcelCellConverter() {
		return (instance, definitionProperty, propertyName, propertyValue) -> {
			if (!DefaultProperties.LOCKED_BY.equals(propertyName)) {
				return null;
			}

			LockInfo lockInfo = lockService.lockStatus(instance.toReference());
			String lockedBy = lockInfo != null ? (String) lockInfo.getLockedBy() : null;
			if (lockedBy != null) {
				return new ExportExcelCell(propertyName, objectHeadersInfoCache.get(lockedBy),
						ExportExcelCell.Type.LINK);
			}

			return new ExportExcelCell(propertyName, "", ExportExcelCell.Type.OBJECT);
		};
	}

	@Override
	public String getName() {
		return SupportedExportFormats.XLS.getFormat();
	}
}