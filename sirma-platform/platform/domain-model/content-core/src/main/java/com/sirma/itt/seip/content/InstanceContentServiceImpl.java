package com.sirma.itt.seip.content;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.content.event.ContentAssignedEvent;
import com.sirma.itt.seip.content.type.MimeTypeResolver;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Content management service that bridges the instances and their content via persistent {@link ContentEntity}
 *
 * @author BBonev
 */
@ApplicationScoped
class InstanceContentServiceImpl implements InstanceContentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ContentStoreProvider contentStoreProvider;
	@Inject
	private InstanceViewPreProcessor viewPreProcessor;
	@Inject
	private EventService eventService;
	@Inject
	private DbDao dbDao;
	@Inject
	private DatabaseIdManager idManager;
	@Inject
	private MimeTypeResolver mimeTypeResolver;
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private IdResolver idResolver;
	@Inject
	private ContentDigestProvider digestProvider;

	private ContentPersistProvider contentPersistProvider;
	private ContentEntityDao entityDao;

	/**
	 * Initialize the internal structures
	 */
	@PostConstruct
	protected void init() {
		entityDao = new ContentEntityDao(idManager, mimeTypeResolver, dbDao, idResolver, digestProvider);
		contentPersistProvider = new ContentPersistProvider(contentStoreProvider, viewPreProcessor, eventService,
				entityDao);
	}

	@Override
	public ContentInfo saveContent(Serializable instance, Content content) {
		if (instance == null || content == null) {
			return ContentInfo.DO_NOT_EXIST;
		}
		return saveContentInternal(instance, content);
	}

	@Override
	public List<ContentInfo> saveContent(Serializable instance, List<Content> contents) {
		if (instance == null || isEmpty(contents)) {
			return Collections.emptyList();
		}
		return contents.stream().map(content -> saveContentInternal(instance, content)).collect(Collectors.toList());
	}

	@Override
	public ContentInfo copyContent(Serializable instance, String contentId) {
		ContentInfo info = getContent(contentId, "any");
		if (!info.exists()) {
			return ContentInfo.DO_NOT_EXIST;
		}
		Content content = Content.createFrom(info).setContent(info);
		return saveContent(instance, content);
	}

	@Override
	public ContentInfo copyContentAsync(Serializable instance, String contentId) {

		ContentInfo sourceInfo = getContent(contentId, "any");
		if (!sourceInfo.exists()) {
			return ContentInfo.DO_NOT_EXIST;
		}
		Content content = Content.createFrom(sourceInfo).setContent(sourceInfo);
		// create a content entity without an actual content to be uploaded/copied
		// the returned info will be returned to the user for reference. After transaction end the content will be made
		// available
		ContentInfo tagretInfo = contentPersistProvider.getNoContentPersister(sourceInfo.isView()).persist(instance,
				content);

		SchedulerContext context = new SchedulerContext(2);
		context.put(AsyncContentCopyAction.SOURCE_CONTENT_ID_KEY, contentId);
		context.put(AsyncContentCopyAction.TARGET_CONTENT_ID_KEY, tagretInfo.getContentId());

		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.TIMED)
					.setTransactionMode(TransactionMode.NOT_SUPPORTED)
					.setMaxRetryCount(50)
					.setRetryDelay(Long.valueOf(60))
					.setMaxActivePerGroup(AsyncContentCopyAction.NAME, 10)
					.setPersistent(true)
					.setRemoveOnSuccess(true)
					.setScheduleTime(new Date());
		schedulerService.schedule(AsyncContentCopyAction.NAME, configuration, context);
		return tagretInfo;
	}

	@Override
	public ContentInfo updateContent(String contentId, Serializable instance, Content content) {
		Objects.requireNonNull(contentId, "Cannot update content without the content identifier");
		if (content == null) {
			return ContentInfo.DO_NOT_EXIST;
		}
		return contentPersistProvider.getPersister(content).persist(contentId, instance, content);
	}

	private ContentInfo saveContentInternal(Serializable instance, Content content) {
		return contentPersistProvider.getPersister(content).persist(instance, content);
	}

	@Override
	public String importContent(ContentImport contentImport) {
		return importInternal(contentImport);
	}

	@Override
	public List<String> importContent(List<ContentImport> contentImports) {
		if (isEmpty(contentImports)) {
			return Collections.emptyList();
		}
		return contentImports.stream().map(content -> importInternal(content)).collect(Collectors.toList());
	}

	private String importInternal(ContentImport contentImport) {
		if (contentImport == null || contentImport.getInstanceId() == null) {
			return null;
		}
		ContentStore contentStore = contentStoreProvider.getStore(contentImport.getRemoteSourceName());
		if (contentStore == null) {
			LOGGER.warn("No ContentStore found for name {}. The content {} will not be imported",
					contentImport.getRemoteSourceName(), contentImport.getRemoteId());
			return null;
		}
		return entityDao.importEntity(contentImport).getId();
	}

	@Override
	public ContentInfo getContent(Serializable instanceId, String type) {
		ContentEntity entity = entityDao.getEntity(extractId(instanceId), type);
		if (entity == null) {
			return ContentInfo.DO_NOT_EXIST;
		}
		return contentPersistProvider.getPersister(false).toContentInfo(entity);
	}

	@Override
	public ContentInfo getContentPreview(Serializable instanceId, String type) {
		ContentEntity entity = entityDao.getEntity(extractId(instanceId), type);
		if (entity == null) {
			return ContentInfo.DO_NOT_EXIST;
		}
		return contentPersistProvider.getPersister(false).toContentInfo(entity,
				(store, info) -> store.getPreviewChannel(info));
	}

	@Override
	public Collection<ContentInfo> getContent(Collection<? extends Serializable> identifiers, String type) {
		if (CollectionUtils.isEmpty(identifiers)) {
			return Collections.emptyList();
		}
		Set<Serializable> ids = convertToSupportedIds(identifiers);
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		List<ContentEntity> found = entityDao.getContentForIds(ids, type);

		return contentPersistProvider.getPersister(false).toContentInfo(found);
	}

	@Override
	public Collection<ContentInfo> getContentsForInstance(Serializable identifier, Collection<String> contentsToSkip) {
		Serializable id = extractId(identifier);
		if (id == null) {
			return Collections.emptyList();
		}

		List<ContentEntity> found = entityDao.getContentsForInstance(identifier, contentsToSkip);
		return contentPersistProvider.getPersister(false).toContentInfo(found);
	}

	@Override
	public boolean deleteAllContentForInstance(Serializable identifier) {
		Serializable id = extractId(identifier);
		if (id == null) {
			return false;
		}

		return entityDao
				.getContentsForInstance(id, emptySet())
					.stream()
					.map(this::deleteEntity)
					.reduce(Boolean.TRUE, Boolean::logicalAnd)
					.booleanValue();
	}

	@Override
	public boolean deleteContent(Serializable identifier, String purpose) {
		Serializable id = extractId(identifier);
		if (id == null) {
			return false;
		}

		return deleteEntity(entityDao.getEntity(id, purpose));
	}

	@Override
	public void deleteContent(Serializable identifier, String purpose, int delay, TimeUnit timeUnit) {
		if (identifier == null || StringUtils.isNullOrEmpty(purpose) || timeUnit == null) {
			throw new IllegalArgumentException();
		}

		SchedulerConfiguration configuration = ScheduleContentDelete.buildConfiguration(delay, timeUnit);
		SchedulerContext context = ScheduleContentDelete.createContext(extractId(identifier), purpose);
		schedulerService.schedule(ScheduleContentDelete.NAME, configuration, context);
	}

	@Override
	public boolean assignContentToInstance(String contentId, Serializable instanceId) {
		if (contentId == null || instanceId == null) {
			return false;
		}
		boolean contentAssigned = entityDao.assignContentToInstance(contentId, instanceId);
		if (contentAssigned) {
			eventService.fire(new ContentAssignedEvent(instanceId, contentId));
		}
		return contentAssigned;
	}

	private boolean deleteEntity(ContentEntity contentEntity) {
		if (contentEntity != null) {
			StoreItemInfo storeInfo = contentEntity.toStoreInfo();
			ContentStore contentStore = contentStoreProvider.getStore(storeInfo);
			if (contentStore != null) {
				// remove entity only if successfully removed from the store
				return contentStore.delete(storeInfo) && entityDao.delete(contentEntity);
			}
		}
		return false;
	}

	private Set<Serializable> convertToSupportedIds(Collection<? extends Serializable> identifiers) {
		Set<Serializable> ids = CollectionUtils.createHashSet(identifiers.size());
		for (Serializable serializable : identifiers) {
			Serializable id = extractId(serializable);
			if (!CollectionUtils.addNonNullValue(ids, id)) {
				LOGGER.warn("Ignoring id for fetching content. Could not convert id to string: {}", serializable);
			}
		}
		return ids;
	}

	private Serializable extractId(Serializable serializable) {
		return idResolver.resolve(serializable).orElse(null);
	}

}
