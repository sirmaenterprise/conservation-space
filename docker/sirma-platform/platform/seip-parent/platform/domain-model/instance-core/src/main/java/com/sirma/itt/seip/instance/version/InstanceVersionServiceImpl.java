package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.instance.version.revert.RevertContext;
import com.sirma.itt.seip.instance.version.revert.RevertStep;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Contains the logic for the instances versioning. Prepares the information for the versions creation and executes
 * formal save on the instances so that they could be returned without any delay. The other operations that should be
 * done in order to make the complete versioning are executed asynchronous.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class InstanceVersionServiceImpl implements InstanceVersionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String SELECTED_OBJECT_KEY = "selectedObject";

	private static final String SELECTED_OBJECTS_KEY = "selectedObjects";

	private static final Date DEFAULT_CONTENT_HANDLING_DATE = new Date();

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "initial.version", type = String.class, defaultValue = "1.0", label = "Configuration for initial versions of the created instances. This version will be set when the new instances are created. The value should be two digits separated with dot. See default value format.")
	private ConfigurationProperty<String> initialVersion;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "version.content.handling.enabled.date", system = true, type = Date.class, label = "Temporary configuration for the date when the new content processing for the version is added to the system.")
	private ConfigurationProperty<Date> contentHandlingEnabledDate;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "version.revert.operation.enabled.date", system = true, type = Date.class, label = "Configuration for the date when the revert functionality for the version instance is enabled. Revert operation will not be allowed for the versions created before that date.")
	private ConfigurationProperty<Date> revertVersionOperationEnabled;

	@Inject
	private VersionStepsExecutor versionStepsExecutor;

	@Inject
	private VersionDao versionDao;

	@Inject
	@com.sirma.itt.seip.instance.dao.InstanceType(type = ObjectTypes.ARCHIVED)
	private InstanceDao archivedInstanceDao;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private InstanceTypes instanceTypes;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	@ExtensionPoint(RevertStep.EXTENSION_NAME)
	private Plugins<RevertStep> revertSteps;

	@Inject
	private LockService lockService;

	@Inject
	private StateTransitionManager stateTransitionManager;

	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public void saveVersion(VersionContext context) {
		TimeTracker tracker = TimeTracker.createAndStart();
		InstanceType type = context.getTargetInstance().type();
		if (type == null || !type.isVersionable()) {
			LOGGER.debug("Instance with id - {} is not versionable.", context.getTargetInstanceId());
			return;
		}

		versionStepsExecutor.execute(context);
		LOGGER.info("Version [{}] was created for {} ms.", context.getVersionInstanceId(), tracker.stop());
	}

	@Override
	public Instance loadVersion(Serializable id) {
		String versionId = Objects.toString(id, null);
		if (StringUtils.isBlank(versionId)) {
			throw new IllegalArgumentException("Version id shouldn't be blank.");
		}

		ArchivedInstance version = loadVersionOrThrowError(id);

		// if the version is created after the date from the configuration the runtime logic for widgets versioning
		// should not be executed, because the content is already processed when the version was created
		if (version.getCreatedOn().after(getContentHandlingDate())) {
			return loadVersionProperties().andThen(convertToOriginalInstance()).apply(version);
		}

		return loadVersionProperties()
				.andThen(loadQueryResultsIfAny())
					.andThen(loadSelectedObjectsInWidgets())
					.andThen(convertToOriginalInstance())
					.apply(version);
	}

	private ArchivedInstance loadVersionOrThrowError(Serializable id) {
		return versionDao.findVersionById(id).orElseThrow(() -> new InstanceNotFoundException(id));
	}

	private Date getContentHandlingDate() {
		if (contentHandlingEnabledDate.isNotSet()) {
			LOGGER.error("Configuration for new versions content handling is not set! "
					+ "Default date will be used instead.");
			// not sure if I should use ConfigurationProperty#getOrFail or this
			return DEFAULT_CONTENT_HANDLING_DATE;
		}

		return contentHandlingEnabledDate.get();
	}

	@Override
	public <I extends Instance> boolean populateVersion(I instance) {
		if (!shouldPopulate(instance)) {
			return false;
		}

		Iterator<ArchivedInstance> versions = versionDao.findVersionsByTargetId(instance.getId(), 0, 1).iterator();
		if (versions.hasNext()) {
			instance.add(DefaultProperties.VERSION, versions.next().getVersion());
			return false;
		}

		instance.add(DefaultProperties.VERSION, getInitialInstanceVersion());
		return true;
	}

	private static <I extends Instance> boolean shouldPopulate(I instance) {
		if (instance == null) {
			LOGGER.debug("Cannot set version property for null instance.");
			return false;
		}

		Serializable id = instance.getId();
		if (InstanceVersionService.isVersion(id)) {
			LOGGER.debug("The passed instance is already version. Instance id: {}", id);
			return false;
		}

		return instance.type() != null && instance.type().isVersionable()
				&& StringUtils.isBlank(instance.getString(DefaultProperties.VERSION));
	}

	@Override
	public VersionsResponse getInstanceVersions(String targetId, int offset, int limit) {
		if (StringUtils.isBlank(targetId)) {
			return VersionsResponse.emptyResponse();
		}

		int versionsCount = versionDao.getVersionsCount(targetId);
		if (versionsCount == 0) {
			return VersionsResponse.emptyResponse();
		}

		Collection<ArchivedInstance> results = versionDao.findVersionsByTargetId(targetId, offset, limit);
		VersionsResponse versionsResponse = new VersionsResponse();
		versionsResponse.setTotalCount(versionsCount);
		versionsResponse.setResults(convert(results));
		return versionsResponse;
	}

	@Override
	public String getInitialInstanceVersion() {
		return initialVersion.get();
	}

	@Override
	public boolean hasInitialVersion(Instance target) {
		return target != null && target.isValueNotNull(VERSION)
				&& target.getString(VERSION).equals(getInitialInstanceVersion());
	}

	@Override
	public  <S extends Serializable> Collection<Instance> loadVersionsById(Collection<S> ids) {
		if (isEmpty(ids)) {
			return Collections.emptyList();
		}

		Collection<ArchivedInstance> foundInstances = versionDao.findVersionsById(ids);
		return convert(foundInstances);
	}

	private Collection<Instance> convert(Collection<ArchivedInstance> archivedInstances) {
		if (isEmpty(archivedInstances)) {
			return Collections.emptyList();
		}

		Map<Serializable, Boolean> viewContentMapping = getViewContentMapping(archivedInstances);
		Map<Serializable, String> primaryContentMapping = getPrimaryContentMapping(archivedInstances);

		return archivedInstances
				.stream()
					.map(loadVersionProperties())
					.map(addContentProperties(viewContentMapping, primaryContentMapping))
					.map(convertToOriginalInstance())
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
	}

	/**
	 * This logic will be removed as soon as the patch for the version content retrieving is ready.
	 * <p>
	 * Creates map with key instance id and value that shows if this instance has {@link Content#PRIMARY_VIEW} content.
	 * It will be used to add property to the extracted version instances that shows, if they have view or not. This is
	 * done, because there was a problem with the content of the older versions and they should be retrieved with
	 * additional patching of content table. Until then we will use this property to deactivate headers of the versions,
	 * which content is lost and should be retrieved.
	 */
	private Map<Serializable, Boolean> getViewContentMapping(Collection<ArchivedInstance> archivedInstances) {
		return instanceContentService.getContent(archivedInstances, Content.PRIMARY_VIEW).stream().collect(
				Collectors.toMap(ContentInfo::getInstanceId, ContentInfo::exists));
	}

	/**
	 * Creates map with key instance id and value that shows if the mime type of the primary content that is mapped to
	 * it. This will be used as additional property to the version instances when they are requested and it is required,
	 * when comparing versions contents, so that we could filter the only the contents that could be compared(only those
	 * which have generated preview), because there are few contents that could not be compared like audio, media, etc.,
	 * with the current compare, because it handles only pdf contents.
	 */
	private Map<Serializable, String> getPrimaryContentMapping(Collection<ArchivedInstance> archivedInstances) {
		return instanceContentService.getContent(archivedInstances, Content.PRIMARY_CONTENT).stream().collect(
				Collectors.toMap(ContentInfo::getInstanceId, ContentInfo::getMimeType));
	}

	// see the methods above
	private static Function<ArchivedInstance, ArchivedInstance> addContentProperties(
			Map<Serializable, Boolean> viewContentsExsistMap, Map<Serializable, String> primaryContentsMimetypesMap) {
		return instance -> {
			Serializable id = instance.getId();
			instance.add(VersionProperties.HAS_VIEW_CONTENT, viewContentsExsistMap.get(id));
			instance.addIfNotNull(VersionProperties.PRIMARY_CONTENT_MIMETYPE, primaryContentsMimetypesMap.get(id));
			return instance;
		};
	}

	private Function<ArchivedInstance, ArchivedInstance> loadVersionProperties() {
		return instance -> {
			archivedInstanceDao.loadProperties(instance);
			instance.add(VersionProperties.IS_VERSION, Boolean.TRUE);
			instance.add(VersionProperties.ORIGINAL_INSTANCE_ID, instance.getTargetId());
			instance.add(VersionProperties.VERSION_CREATION_DATE, instance.getCreatedOn());
			instance.add(VersionProperties.DEFINITION_ID, instance.getIdentifier());
			setType(instance);
			return instance;
		};
	}

	private void setType(ArchivedInstance instance) {
		if (instance.isValueNotNull(SEMANTIC_TYPE)) {
			instanceTypes.from(instance).ifPresent(instance::setType);
		} else {
			// if the version does not have semantic type, pass the target id without the version to prevent stack overflow
			instanceTypes.from(instance.getTargetId()).ifPresent(instance::setType);
		}

		if (instance.type() == null) {
			LOGGER.warn("No instance type resolved for archived instance: {}", instance.getId());
		}
	}

	/**
	 * tl dr - black magic.
	 * <p>
	 * Retrieves the content with the search queries results, saved when the version is created. This content represents
	 * search criteria id and results for them. For every criteria are extracted the exact version of the instances that
	 * were shown in the widgets when the version was created. They are retrieved by DB query based on the instances ids
	 * and the date when the version was created. The results are stored in JSON object, which is stored as instance
	 * property under {@link VersionProperties#QUERIES_RESULTS} key.
	 */
	// TODO will be removed when the queries results content is migrated. At the moment this content contains only ids
	// of original(target) instances. This method and all of the logic related to this ids transformation to versions
	// ids will be moved as patch with which this contents will be migrated so that we don't need to execute it runtime
	// on every version loading and work directly with the content and results in it.
	private Function<ArchivedInstance, ArchivedInstance> loadQueryResultsIfAny() {
		return instance -> {
			Serializable queryContentId = instance.get(VersionProperties.QUERIES_RESULT_CONTENT_ID);
			if (queryContentId == null) {
				LOGGER.trace("Query results couldn't be resolved, because of a missing results content id - {}",
						queryContentId);
				return instance;
			}

			ContentInfo content = instanceContentService.getContent(queryContentId, null);
			if (!content.exists()) {
				LOGGER.debug("There is no content for content id - ", queryContentId);
				return instance;
			}

			Serializable creationDate = instance.getCreatedOn();
			JsonObject json = JSON.readObject(content.getInputStream(), toVersionIds(creationDate));
			if (!json.isEmpty()) {
				instance.add(VersionProperties.QUERIES_RESULTS, json.toString());
			}

			return instance;
		};
	}

	private Function<JsonObject, JsonObject> toVersionIds(Serializable date) {
		return source -> {
			if (source.isEmpty()) {
				return source;
			}

			JsonObjectBuilder builder = Json.createObjectBuilder();
			searchForVersions(date, source, builder);
			return builder.build();
		};
	}

	private void searchForVersions(Serializable date, JsonObject source, JsonObjectBuilder builder) {
		for (String key : source.keySet()) {
			JsonArray idArray = source.getJsonArray(key);
			List<Serializable> allIds = JSON.jsonToList(idArray);
			Map<Serializable, Serializable> found = versionDao.findVersionIdsByTargetIdAndDate(allIds, date);
			// remove the ids for found versions and put back the ids of the actual versions
			allIds.removeAll(found.keySet());
			allIds.addAll(found.values());
			builder.add(key, JSON.convertToJsonArray(allIds));
		}
	}

	private Function<ArchivedInstance, Instance> convertToOriginalInstance() {
		return instance -> {
			// in case we got archived instance without reference
			InstanceReference reference = instance.toReference();
			if (reference == null) {
				return null;
			}

			Class<?> javaClass = reference.getReferenceType().getJavaClass();
			return (Instance) objectMapper.map(instance, javaClass);
		};
	}

	/**
	 * This and the other workaround will be removed soon(end of this week).<br />
	 * This is workaround for manually selected objects in the widgets. It parses the view content for the version and
	 * extracts all the widgets, then retrieves all manually selected object ids from them and executes version instance
	 * search for this ids and versions creation date. The results are stored in map, which is send to the web as
	 * another instance property.
	 */
	private Function<ArchivedInstance, ArchivedInstance> loadSelectedObjectsInWidgets() {
		return instance -> {
			ContentInfo content = instanceContentService.getContent(instance.getId(), Content.PRIMARY_VIEW);
			if (!content.exists()) {
				return instance;
			}

			return processContent(instance, content);
		};
	}

	private ArchivedInstance processContent(ArchivedInstance instance, ContentInfo content) {
		try {
			Idoc idoc = Idoc.parse(content.getInputStream());
			Map<Serializable, Serializable> versionIdsMap = getSelectedObjectsVersionIds(instance, idoc.widgets());
			if (versionIdsMap.isEmpty()) {
				return instance;
			}

			JsonObjectBuilder builder = Json.createObjectBuilder();
			versionIdsMap.entrySet().forEach(e -> builder.add(e.getKey().toString(), e.getValue().toString()));
			instance.add(VersionProperties.MANUALLY_SELECTED, builder.build().toString());
		} catch (IOException e) {
			LOGGER.debug("There was a problem with the ids retrieving from widgets with manually selected objects,"
					+ " not going to return correct data.", e);
		}
		return instance;
	}

	private Map<Serializable, Serializable> getSelectedObjectsVersionIds(ArchivedInstance instance,
			Stream<Widget> widgets) {
		Set<Serializable> selectedInstanceIds = new HashSet<>();
		widgets
				.map(Widget::getConfiguration)
					.map(WidgetConfiguration.class::cast)
					.map(WidgetConfiguration::getConfiguration)
					.map(addManuallySelectedObjects())
					.forEach(selectedInstanceIds::addAll);

		return versionDao.findVersionIdsByTargetIdAndDate(selectedInstanceIds, instance.getCreatedOn());
	}

	/**
	 * Retrieves manually selected instances ids from widgets.
	 */
	static Function<com.google.gson.JsonObject, Set<String>> addManuallySelectedObjects() {
		return json -> {
			if (json.isJsonNull() || json.entrySet().isEmpty()) {
				return Collections.emptySet();
			}

			Set<String> ids = new HashSet<>();
			if (json.has(SELECTED_OBJECT_KEY)) {
				ids.add(json.get(SELECTED_OBJECT_KEY).getAsString());
			}

			if (json.has(SELECTED_OBJECTS_KEY)) {
				json.getAsJsonArray(SELECTED_OBJECTS_KEY).forEach(id -> ids.add(id.getAsString()));
			}

			return ids;
		};
	}

	@Override
	public void deleteVersion(Serializable versionId) {
		String id = Objects.toString(versionId, null);
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("Version id shouldn't be blank.");
		}

		ArchivedInstance version = versionDao.findVersionById(versionId).orElse(null);
		if (version == null) {
			LOGGER.info("Version instance with id - [{}] not found. There is nothing to delete.", id);
			return;
		}

		archivedInstanceDao.delete(version);
	}

	@Override
	public Instance revertVersion(RevertContext context) {
		Objects.requireNonNull(context);
		TimeTracker tracker = TimeTracker.createAndStart();

		InstanceReference currentInstanceReference = checkCurrentInstanceLockStatus(context);
		// TODO: rethink lock exception handling. One of try/catch, forceLock or tryLock
		lockService.lock(currentInstanceReference, "revert");

		final List<RevertStep> executedSteps = new ArrayList<>(revertSteps.count());
		transactionSupport.invokeOnFailedTransactionInTx(() -> {
			Collections.reverse(executedSteps);
			executedSteps.forEach(step -> step.rollback(context));
		});

		try {
			callSteps(step -> step.invoke(context), executedSteps::add);
			return context.getRevertResultInstance();
		} catch (RuntimeException e) {
			LOGGER.error("Problem occurred while reverting version - {}", context.getVersionId(), e);
			throw e;
		} finally {
			lockService.tryUnlock(currentInstanceReference);
			LOGGER.debug("Revert on version [{}] was executed for {} ms.", context.getVersionId(), tracker.stop());
		}
	}

	private InstanceReference checkCurrentInstanceLockStatus(RevertContext context) {
		Serializable currentInstanceId = context.getCurrentInstanceId();
		InstanceReference currentInstanceReference = instanceTypeResolver
				.resolveReference(currentInstanceId)
					.orElseThrow(() -> new InstanceNotFoundException(currentInstanceId));

		LockInfo status = lockService.lockStatus(currentInstanceReference);
		if (status.isLocked()) {
			throw new LockException(status, "The object which should be reverted is currently locked.");
		}

		return currentInstanceReference;
	}

	private void callSteps(Consumer<RevertStep> invokeStep, Consumer<RevertStep> onSuccess) {
		revertSteps.forEach(invokeStep.andThen(onSuccess));
	}

	@Override
	public boolean isRevertOperationAllowed(Instance target) {
		Objects.requireNonNull(target, "Instance is required!");
		Serializable id = target.getId();
		if (!InstanceVersionService.isVersion(id)) {
			return false;
		}

		// the supplier should provide date of creation of the version or date of creation of the instance, if the first
		// is missing. There are cases where the creation date of the version is missing for old versions, currently
		// it can't happen, because of the other workaround for the CMF-26198. Both will be removed.. eventually...
		Date versionCreation = target.get(VersionProperties.VERSION_CREATION_DATE, Date.class, () -> {
			ArchivedInstance aInstance = loadVersionOrThrowError(id);
			archivedInstanceDao.loadProperties(aInstance);
			return EqualsHelper.getOrDefault(aInstance.getCreatedOn(), aInstance.get(CREATED_ON, Date.class));
		});
		boolean isAfterRevert = versionCreation.after(revertVersionOperationEnabled.get());
		return isAfterRevert && isRevertAllowedForOriginal(id);
	}

	@Override
	public <S extends Serializable> Map<S, Boolean> exist(Collection<S> ids) {
		return versionDao.exits(ids);
	}

	private boolean isRevertAllowedForOriginal(Serializable id) {
		Optional<InstanceReference> originalReference = instanceTypeResolver
				.resolveReference(InstanceVersionService.getIdFromVersionId(id));
		if (!originalReference.isPresent()) {
			return false;
		}

		Instance originalInstane = originalReference.get().toInstance();
		return stateTransitionManager
				.getAllowedOperations(originalInstane, originalInstane.getAsString(STATUS))
					.contains(REVERT_VERSION_SERVER_OPERATION);
	}

}
