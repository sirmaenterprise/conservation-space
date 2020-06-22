package com.sirma.sep.content;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.PurposeQualifier;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.event.ContentAssignedEvent;
import com.sirma.sep.content.event.ContentImportedEvent;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirma.sep.content.type.MimeTypeResolver;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

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
	@Inject
	private SenderService senderService;
	@Inject
	private SecurityContext securityContext;
	@Inject
	private TransactionSupport transactionSupport;

	private ContentPersistProvider contentPersistProvider;
	private ContentEntityDao entityDao;

	/**
	 * Initialize the internal structures
	 */
	@PostConstruct
	protected void init() {
		entityDao = new ContentEntityDao(idManager, mimeTypeResolver, dbDao, idResolver, digestProvider, transactionSupport);
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
				.setMaxRetryCount(50)
				.setRetryDelay(60L)
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
		return contentImports.stream().map(this::importInternal).collect(Collectors.toList());
	}

	private String importInternal(ContentImport contentImport) {
		if (contentImport == null || contentImport.getInstanceId() == null) {
			return null;
		}
		if (!contentStoreProvider.findStore(contentImport.getRemoteSourceName()).isPresent()) {
			LOGGER.warn("No ContentStore found for name {}. The content {} will not be imported",
					contentImport.getRemoteSourceName(), contentImport.getRemoteId());
			return null;
		}

		String contentId = entityDao.importEntity(contentImport).getId();
		eventService.fire(new ContentImportedEvent(contentId, contentImport),
				new PurposeQualifier(contentImport.getPurpose()));
		return contentId;
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
		ContentInfo primaryContentPreview = ContentInfo.DO_NOT_EXIST;
		if (Content.PRIMARY_CONTENT.equals(type)) {
			primaryContentPreview = getContent(instanceId, Content.PRIMARY_CONTENT_PREVIEW);
		}

		if (primaryContentPreview.exists()) {
			return primaryContentPreview;
		}

		// Fallback to content store's preview channel
		ContentEntity entity = entityDao.getEntity(extractId(instanceId), type);
		if (entity == null) {
			return ContentInfo.DO_NOT_EXIST;
		}
		return contentPersistProvider.getPersister(false).toContentInfo(entity,
				ContentStore::getPreviewChannel);
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

		List<ContentEntity> found = entityDao.getContentsForInstance(id, contentsToSkip);
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
				.reduce(Boolean.TRUE, Boolean::logicalAnd);
	}

	@Override
	public boolean deleteContent(Serializable identifier, String purpose) {
		Serializable id = extractId(identifier);
		return id != null && deleteEntity(entityDao.getEntity(id, purpose));
	}

	@Override
	public void deleteContent(Serializable identifier, String purpose, int delay, TimeUnit timeUnit) {
		if (identifier == null || StringUtils.isBlank(purpose) || timeUnit == null) {
			throw new IllegalArgumentException();
		}

		ContentEntity entity = entityDao.getEntity(extractId(identifier), purpose);
		if (entity != null) {
			deleteEntity(entity, delay, timeUnit);
		}
	}

	@Override
	public boolean assignContentToInstance(String contentId, Serializable instanceId, String purpose) {
		if (contentId == null || instanceId == null) {
			return false;
		}

		// when there is a content mapped to that instance with the same purpose the new assignment should be with
		// incremented version
		ContentEntity currentContent = entityDao.getEntity(instanceId, purpose);
		int version = currentContent == null ? 0 : currentContent.getVersion() + 1;
		boolean contentAssigned = entityDao.assignContentToInstance(contentId, instanceId, version);
		if (contentAssigned) {
			eventService.fire(new ContentAssignedEvent(instanceId, contentId));
		}
		return contentAssigned;
	}

	private boolean deleteEntity(ContentEntity contentEntity) {
		return deleteEntity(contentEntity, 0, TimeUnit.MILLISECONDS);
	}

	private boolean deleteEntity(ContentEntity contentEntity, int delay, TimeUnit unit) {
		if (contentEntity == null) {
			return false;
		}

		StoreItemInfo storeInfo = contentEntity.toStoreInfo();
		ContentStore contentStore = contentStoreProvider.getStore(storeInfo);
		if (contentStore != null) {
			Optional<DeleteContentData> deleteContentData = contentStore.prepareForDelete(storeInfo);
			// if the current store supports async delete and there is configured
			boolean isReferenced = isReferenced(contentEntity);
			boolean asyncPossible = deleteContentData.isPresent();
			return asyncPossible
					? delayedDelete(deleteContentData.get(), contentEntity, delay, unit, isReferenced)
					: immediateDelete(contentEntity, contentStore, storeInfo, isReferenced);
		}
		return false;
	}

	private boolean isReferenced(ContentEntity contentEntity) {
		return entityDao
				.getEntityByRemoteId(contentEntity.getRemoteSourceName(), contentEntity.getRemoteId())
					.size() > 1;
	}

	/**
	 * Sends message to content delete queue. This will ensure that the content is deleted eventually.
	 */
	private boolean delayedDelete(DeleteContentData contentData, ContentEntity entity, int delay, TimeUnit unit,
			boolean isReferenced) {
		contentData
				.setContentId(entity.getId())
				.setTenantId(securityContext.getCurrentTenantId());
		if (isReferenced) {
			contentData.doNotDeleteContent();
		}
		String data = contentData.asJsonString();
		senderService.sendText(ContentDestinations.DELETE_CONTENT_QUEUE, data,
				SendOptions.create().delayWith(delay, unit).asSystem());
		return true;
	}

	/**
	 * Tries to delete the content immediately, but it doesn't ensure that the file and the entity will be removed. It
	 * is executed, when delayed delete is not specified. <br />
	 * If the content(actual file) is referenced by another entity, we delete only passed entity, but not the actual
	 * file.
	 */
	private boolean immediateDelete(ContentEntity contentEntity, ContentStore contentStore, StoreItemInfo storeInfo,
			boolean isReferenced) {
		if (isReferenced) {
			return entityDao.delete(contentEntity);
		}
		return contentStore.delete(storeInfo) && entityDao.delete(contentEntity);
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
