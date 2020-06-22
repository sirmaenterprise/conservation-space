package com.sirma.sep.content;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.event.ContentAddEvent;
import com.sirma.sep.content.event.ContentUpdatedEvent;
import com.sirma.sep.content.event.InstanceViewAddedEvent;
import com.sirma.sep.content.event.InstanceViewUpdatedEvent;

/**
 * Provider for {@link ContentPersister} instance that handle actual or fictional content save and retrieve. This is low
 * level implementation class and should not be used outside the module.
 *
 * @author BBonev
 */
class ContentPersistProvider {

	private final ContentPersister contentPersister;
	private final ContentPersister viewPersister;
	private final ContentPersister noContentPersister;
	private final ContentPersister noViewPersister;

	/**
	 * Initialize a content persister provider instance
	 *
	 * @param contentStoreProvider
	 *            provider used to get a content store instances
	 * @param viewPreProcessor
	 *            pre processor for instance views.
	 * @param eventService
	 *            event service instance
	 * @param entityDao
	 *            entity dao responsible for entity persistence and loading
	 */
	@Inject
	protected ContentPersistProvider(ContentStoreProvider contentStoreProvider,
			InstanceViewPreProcessor viewPreProcessor, EventService eventService, ContentEntityDao entityDao) {

		contentPersister = new ContentPersister(contentStoreProvider, eventService, entityDao);
		viewPersister = new ViewPersister(contentStoreProvider, eventService, entityDao, viewPreProcessor);
		noContentPersister = new NoContentPersister(contentStoreProvider, eventService, entityDao);
		noViewPersister = new NoViewPersister(contentStoreProvider, eventService, entityDao, viewPreProcessor);
	}

	/**
	 * Get a persister instance that can persist instance views or regular content.
	 *
	 * @param content
	 *            the content that will be persisted by the returned persister.
	 * @return a persister that can handle the content persisting
	 */
	ContentPersister getPersister(Content content) {
		return getPersister(content.isView());
	}

	/**
	 * Get a persister instance that can persist instance views or regular content.
	 *
	 * @param isView
	 *            if the expected content is for new or persist.
	 * @return a persister that can handle the content persisting
	 */
	ContentPersister getPersister(boolean isView) {
		return isView ? viewPersister : contentPersister;
	}

	/**
	 * Get content persister that do not do actual content persisting but only saves the given data to the database.
	 * This is used to prepare valid entity for content that will be uploaded asynchronously. The changed functionality
	 * is only for the persist methods. All other methods are not affected.
	 *
	 * @param isView
	 *            if the expected content is for view or regular content
	 * @return a persister that can handle the given requests
	 */
	ContentPersister getNoContentPersister(boolean isView) {
		return isView ? noViewPersister : noContentPersister;
	}

	/**
	 * Processes and persists generic content
	 *
	 * @author BBonev
	 */
	static class ContentPersister {

		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		protected final ContentStoreProvider contentStoreProvider;
		protected final EventService eventService;
		protected final ContentEntityDao entityDao;

		/**
		 * Instantiates a new content persister.
		 *
		 * @param contentStoreProvider
		 *            the content store provider
		 * @param eventService
		 *            the event service
		 * @param entityDao
		 *            the entity dao
		 */
		private ContentPersister(ContentStoreProvider contentStoreProvider, EventService eventService,
				ContentEntityDao entityDao) {
			this.contentStoreProvider = contentStoreProvider;
			this.eventService = eventService;
			this.entityDao = entityDao;
		}

		/**
		 * Update and persist a specific content entity. If the entity does not exist the method does nothing.
		 * Versioning option is ignored by this method.
		 *
		 * @param contentId
		 *            the content id
		 * @param instance
		 *            the instance
		 * @param content
		 *            the content
		 * @return the content info
		 */
		public ContentInfo persist(String contentId, Serializable instance, Content content) {
			// this should fetch only entity that has the given id. This way if instance id is passed as first argument
			// it will not mess it up as the purpose will not match any used.
			ContentEntity entity = entityDao.getEntity(contentId, "any");
			if (entity == null) {
				return ContentInfo.DO_NOT_EXIST;
			}
			entityDao.fillEntityMetadata(entity, content, instance);
			return persistEntity(instance, content, entity);
		}

		/**
		 * Persist content for instance by creating new entity if one for the given instance and and purpose or update
		 * existing. If versioning is enabled new entity will be created.
		 *
		 * @param instance
		 *            the instance
		 * @param content
		 *            the content
		 * @return the content info to the stored file
		 */
		public ContentInfo persist(Serializable instance, Content content) {
			if (content.isReuseAllowed()) {
				ContentEntity entity = entityDao.getUniqueContent(instance, content);
				if (entityDao.isNewEntity(entity)) {
					return persistEntity(instance, content, entity);
				}
				LOGGER.debug("Reusing content {}", entity.getId());
				return toContentInfo(entity);
			}
			ContentEntity entity = entityDao.getOrCreateEntity(instance, content);
			return persistEntity(instance, content, entity);
		}

		protected ContentInfo persistEntity(Serializable instance, Content content, ContentEntity entity) {
			PreviousVersion previousVersion = PreviousVersion.NO_PREVIOUS_VERSION;

			StoreItemInfo previousInfo = entity.toStoreInfo();
			ContentStore contentStore = contentStoreProvider.getStore(previousInfo);
			if (contentStore == null) {
				contentStore = getContentStore(instance, content);
			}
			boolean isNew = entity.isNew();
			boolean isNewVersion = entity.getVersion() > 0;
			// for non new views create a local copy that will be used for accessing
			if (!isNew || isNewVersion) {
				previousVersion = getPreviousVersion(contentStore, instance, entity);
			}

			return uploadContentAndPersist(instance, content, entity, previousVersion, previousInfo, contentStore,
					isNew, isNewVersion);
		}

		/**
		 * Low level operation. Notify for before/after content save. Calls the given {@link ContentStore} to perform
		 * the add/update of the given content (based on the {@code isNew} parameter). If this is for update the
		 * arguments {@code previousVersion}, {@code previousInfo} should have valid non <code>null</code> values
		 *
		 * @param instance
		 *            the target instance that owns the content
		 * @param content
		 *            the content to save
		 * @param entity
		 *            the content entity to update after the upload
		 * @param previousVersion
		 *            the previous version info. Required if {@code isNew=true}
		 * @param previousInfo
		 *            the previous store item info. Required if {@code isNew=true}
		 * @param contentStore
		 *            the content store to use for the add/upload operation
		 * @param isNew
		 *            if <code>true</code> the content will be considered new and
		 *            {@link ContentStore#add(Serializable, Content)} method will be called, otherwise
		 *            {@link ContentStore#update(Serializable, Content, StoreItemInfo)} will be called
		 * @param isNewVersion
		 *            if <code>true</code> the content will be considered new version of existing content
		 * @return the content info of the uploaded content
		 */
		@SuppressWarnings("squid:S00107")
		protected ContentInfo uploadContentAndPersist(Serializable instance, Content content, ContentEntity entity,
				PreviousVersion previousVersion, StoreItemInfo previousInfo, ContentStore contentStore, boolean isNew,
				boolean isNewVersion) {
			try {
				Content updatedView = beforeContentSave(instance, content, previousVersion.getPreviousVersion());

				StoreItemInfo storeItemInfo;
				if (isNew) {
					storeItemInfo = contentStore.add(instance, updatedView);
				} else {
					storeItemInfo = contentStore.update(instance, updatedView, previousInfo);
				}

				entity.copyFrom(storeItemInfo);

				entityDao.persistEntity(entity, isNew);

				afterContentSave(instance, previousVersion.getPreviousVersion(), toContentInfo(entity), updatedView,
						isNew && !isNewVersion);
			} finally {
				previousVersion.delete();
			}
			return toContentInfo(entity);
		}

		/**
		 * Provide previous version of the content associated with the given instance and located in the given store. If
		 * applicable the returned instance should return a copy of the content or <code>null</code>.
		 *
		 * @param contentStore
		 *            the content store where the original content is located
		 * @param instance
		 *            the instance that is related to the content
		 * @param entity
		 *            the content entity that represents the old content instance.
		 * @return the previous version instance. Should not be <code>null</code>.
		 */
		@SuppressWarnings("static-method")
		protected PreviousVersion getPreviousVersion(ContentStore contentStore, Serializable instance,
				ContentEntity entity) {
			return PreviousVersion.NO_PREVIOUS_VERSION;
		}

		/**
		 * Called after content save was saved. The default implementation fires events that the
		 * content was added/updated
		 *
		 * @param instance
		 *            the target instance
		 * @param previousVersion
		 *            the previous version of the content
		 * @param newVersion
		 *            the new version of the content
		 * @param content
		 *            the content being saved
		 * @param isNew
		 *            if is new content or not
		 */
		protected void afterContentSave(Serializable instance, ContentInfo previousVersion, ContentInfo newVersion,
				Content content, boolean isNew) {
			if (isNew) {
				eventService.fire(new ContentAddEvent(instance, content));
			} else {
				eventService.fire(new ContentUpdatedEvent(instance, content, previousVersion, newVersion));
			}
		}

		/**
		 * Called before content save is saved to the store. The method may update/change the content.
		 *
		 * @param instance
		 *            the instance
		 * @param content
		 *            the content
		 * @param previousVersion
		 *            the previous version
		 * @return the content
		 */
		@SuppressWarnings("static-method")
		protected Content beforeContentSave(Serializable instance, Content content, ContentInfo previousVersion) {
			return content;
		}

		/**
		 * Gets the content store responsible for the content persist
		 *
		 * @param instance
		 *            the instance
		 * @param content
		 *            the content
		 * @return the content store
		 */
		protected ContentStore getContentStore(Serializable instance, Content content) {
			return contentStoreProvider.getStore(instance, content);
		}

		/**
		 * Converts the given list of entities to {@link ContentInfo}s
		 *
		 * @param entities
		 *            the list of entities to convert
		 * @return the collection of converted infos
		 */
		protected Collection<ContentInfo> toContentInfo(List<ContentEntity> entities) {
			return entities.stream().map(this::toContentInfo).collect(Collectors.toList());
		}

		/**
		 * Converts the given content entity to content info using information from content store that can handle it. If
		 * no store is found or it could not load the content <code>null</code> will be returned.
		 *
		 * @param info
		 *            the info to convert
		 * @return the content info or <code>null</code> if store not found or could not read the file from the store
		 */
		protected ContentInfo toContentInfo(ContentEntity info) {
			return toContentInfo(info, ContentStore::getReadChannel);
		}

		/**
		 * Converts the given content entity to content info using information from content store that can handle it. If
		 * no store is found or it could not load the content <code>null</code> will be returned.
		 *
		 * @param info
		 *            the info to convert
		 * @param contentProvider
		 *            the content provider that calls the concrete method of the content store to provide the file
		 *            descriptor
		 * @return the content info or <code>null</code> if store not found or could not read the file from the store
		 */
		protected ContentInfo toContentInfo(ContentEntity info,
				BiFunction<ContentStore, StoreItemInfo, FileDescriptor> contentProvider) {
			StoreItemInfo storeInfo = info.toStoreInfo();
			ContentStore contentStore = contentStoreProvider.getStore(storeInfo);
			return new ContentInfoProxy(() -> readContent(info, contentProvider, storeInfo, contentStore), info,
					getContentMetadataProvider(info));
		}

		private static FileDescriptor readContent(ContentEntity info,
				BiFunction<ContentStore, StoreItemInfo, FileDescriptor> contentProvider, StoreItemInfo storeInfo,
				ContentStore contentStore) {
			FileDescriptor descriptor = null;
			if (contentStore != null) {
				try {
					descriptor = contentProvider.apply(contentStore, storeInfo);
				} catch (StoreException e) {
					LOGGER.warn("Could not read content from store {} and id {} due to: {}",
							storeInfo.getProviderType(), storeInfo.getRemoteId(), e.getMessage());
					LOGGER.trace("", e);
				}
			} else {
				LOGGER.warn("No content store found with name '{}' and cannot load content {} for instance {}",
						storeInfo.getProviderType(), info.getPurpose(), info.getInstanceId());
			}
			return descriptor;
		}

		protected Supplier<ContentMetadata> getContentMetadataProvider(ContentEntity entity) {
			ContentStoreProvider storeProvider = contentStoreProvider;
			ContentEntityDao dao = entityDao;
			return () -> {
				StoreItemInfo storeInfo = entity.toStoreInfo();
				ContentMetadata metadata = storeProvider.getStore(storeInfo).getMetadata(storeInfo);
				if (storeInfo.isModified()) {
					entity.copyFrom(storeInfo);
					dao.updateEntityInTx(entity);
				}
				return metadata;
			};
		}
	}

	/**
	 * Extension of {@link ContentPersister} to handle instance views.
	 *
	 * @author BBonev
	 */
	static class ViewPersister extends ContentPersister {

		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private InstanceViewPreProcessor viewPreProcessor;

		/**
		 * Instantiates a new view persister.
		 *
		 * @param contentStoreProvider
		 *            the content store provider
		 * @param eventService
		 *            the event service
		 * @param entityDao
		 *            the entity dao
		 * @param viewPreProcessor
		 *            the view pre processor
		 */
		private ViewPersister(ContentStoreProvider contentStoreProvider, EventService eventService,
				ContentEntityDao entityDao, InstanceViewPreProcessor viewPreProcessor) {
			super(contentStoreProvider, eventService, entityDao);
			this.viewPreProcessor = viewPreProcessor;
		}

		@Override
		protected void afterContentSave(Serializable instance, ContentInfo previousVersion, ContentInfo newVersion,
				Content updatedView, boolean isNew) {
			if (isNew) {
				eventService.fire(new InstanceViewAddedEvent(instance, updatedView));
			} else {
				eventService.fire(new InstanceViewUpdatedEvent(instance, updatedView, previousVersion));
			}
		}

		@Override
		protected PreviousVersion getPreviousVersion(ContentStore contentStore, Serializable instance,
				ContentEntity entity) {
			// with versioning enabled the given entity as argument is no longer the previous version but it's the
			// entity with max version in the database so we get it
			ContentEntity previousEntity = entityDao.getEntity(entity.getInstanceId(), entity.getPurpose());
			if (previousEntity == null) {
				LOGGER.warn("Could not get the previous version of the content: {}", entity.getId());
				return super.getPreviousVersion(contentStore, instance, entity);
			}
			StoreItemInfo previousInfo = previousEntity.toStoreInfo();

			ContentInfo previousVersion = new ContentInfoProxy(() -> contentStore.getReadChannel(previousInfo),
					previousEntity, getContentMetadataProvider(previousEntity));
			if (previousVersion.exists()) {
				return new PreviousVersion(contentStoreProvider, instance, previousVersion);
			}
			LOGGER.warn("Could not get the previous version of the content: {}", previousEntity.getId());
			return super.getPreviousVersion(contentStore, instance, entity);
		}

		@Override
		protected Content beforeContentSave(Serializable instance, Content content, ContentInfo previousVersion) {
			ViewPreProcessorContext preProcessorContext = new ViewPreProcessorContext(instance, content,
					previousVersion);
			viewPreProcessor.process(preProcessorContext);

			// the argument descriptor is no longer needed we should work with this new updated descriptor
			return preProcessorContext.getView();
		}

		@Override
		protected ContentStore getContentStore(Serializable instance, Content content) {
			return contentStoreProvider.getViewStore(instance, content);
		}
	}

	/**
	 * Dummy content persister that do not do actual content persisting but only entity management. Used for async jobs
	 *
	 * @author BBonev
	 */
	static class NoContentPersister extends ContentPersister {

		/**
		 * Dummy content persister that do not do actual content persisting but only entity management. Used for async
		 * jobs
		 *
		 * @param contentStoreProvider
		 *            store provider to use
		 * @param eventService
		 *            event service to use
		 * @param entityDao
		 *            entity dao to use
		 */
		private NoContentPersister(ContentStoreProvider contentStoreProvider, EventService eventService,
				ContentEntityDao entityDao) {
			super(contentStoreProvider, eventService, entityDao);
		}

		@Override
		protected ContentInfo uploadContentAndPersist(Serializable instance, Content content, ContentEntity entity,
				PreviousVersion previousVersion, StoreItemInfo previousInfo, ContentStore contentStore, boolean isNew,
				boolean isNewVersion) {
			try {
				entity.setRemoteId("");
				entity.setRemoteSourceName(contentStore.getName());

				entityDao.persistEntity(entity, isNew);
			} finally {
				previousVersion.delete();
			}
			return toContentInfo(entity);
		}
	}

	/**
	 * Dummy view content persister that do not do actual content persisting but only entity management. Used for async
	 * jobs
	 *
	 * @author BBonev
	 */
	static class NoViewPersister extends ViewPersister {

		/**
		 * Dummy view content persister that do not do actual content persisting but only entity management. Used for
		 * async jobs
		 *
		 * @param contentStoreProvider
		 *            store provider to use
		 * @param eventService
		 *            event service to use
		 * @param entityDao
		 *            entity dao to use
		 * @param viewPreProcessor
		 *            view pre processor to use
		 */
		private NoViewPersister(ContentStoreProvider contentStoreProvider, EventService eventService,
				ContentEntityDao entityDao, InstanceViewPreProcessor viewPreProcessor) {
			super(contentStoreProvider, eventService, entityDao, viewPreProcessor);
		}

		@Override
		protected ContentInfo uploadContentAndPersist(Serializable instance, Content content, ContentEntity entity,
				PreviousVersion previousVersion, StoreItemInfo previousInfo, ContentStore contentStore, boolean isNew,
				boolean isNewVersion) {
			try {
				entity.setRemoteSourceName(contentStore.getName());

				entityDao.persistEntity(entity, isNew);
			} finally {
				previousVersion.delete();
			}
			return toContentInfo(entity);
		}
	}

	/**
	 * Content info implementation that acts as combining proxy for the data from the content store for a file and the
	 * information in the local database.
	 *
	 * @author BBonev
	 */
	private static class ContentInfoProxy implements ContentInfo {

		private static final long serialVersionUID = 8879852003578119698L;

		private final transient Supplier<FileDescriptor> descriptorSupplier;
		private FileDescriptor descriptor;
		private final String mimeType;
		private final String name;
		private final long contentSize;
		private final String contentId;
		private final Serializable instanceId;
		private final String purpose;
		private final boolean view;
		private final String charset;
		private final String remoteId;
		private final String remoteSourceName;
		private final String checksum;
		private final boolean indexable;
		private final boolean reused;


		private final transient Supplier<ContentMetadata> metadataResolver;

		/**
		 * Instantiates a new content info proxy.
		 *
		 * @param descriptorSupplier
		 *            the descriptor
		 * @param entity
		 *            the entity
		 * @param metadataResolver
		 *            the metadata resolver
		 */
		public ContentInfoProxy(Supplier<FileDescriptor> descriptorSupplier, ContentEntity entity,
				Supplier<ContentMetadata> metadataResolver) {
			this.descriptorSupplier = descriptorSupplier;
			this.metadataResolver = metadataResolver;
			mimeType = entity.getMimeType();
			name = entity.getName();
			contentSize = entity.getContentSize() == null ? -1L : entity.getContentSize().longValue();
			contentId = entity.getId();
			instanceId = entity.getInstanceId();
			purpose = entity.getPurpose();
			view = entity.getView() == null ? false : entity.getView().booleanValue();
			charset = entity.getCharset();
			remoteId = entity.getRemoteId();
			remoteSourceName = entity.getRemoteSourceName();
			indexable = Boolean.TRUE.equals(entity.getIndexable());
			reused = org.apache.commons.lang3.StringUtils.isNotBlank(entity.getChecksum());
			checksum = entity.getChecksum();
		}

		@Override
		public String getId() {
			return getName();
		}

		@Override
		public String getContainerId() {
			FileDescriptor fileDescriptor = getDescriptor();
			if (fileDescriptor != null) {
				return fileDescriptor.getContainerId();
			}
			return null;
		}

		@Override
		public InputStream getInputStream() {
			FileDescriptor fileDescriptor = getDescriptor();
			if (fileDescriptor != null) {
				return fileDescriptor.getInputStream();
			}
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean exists() {
			return getDescriptor() != null;
		}

		private FileDescriptor getDescriptor() {
			if (descriptor == null) {
				descriptor = descriptorSupplier.get();
			}
			return descriptor;
		}

		@Override
		public String getMimeType() {
			return mimeType;
		}

		@Override
		public long getLength() {
			return contentSize;
		}

		@Override
		public void close() {
			if (descriptor != null) {
				descriptor.close();
			}
		}

		@Override
		public String getContentId() {
			return contentId;
		}

		@Override
		public Serializable getInstanceId() {
			return instanceId;
		}

		@Override
		public String getContentPurpose() {
			return purpose;
		}

		@Override
		public boolean isView() {
			return view;
		}

		@Override
		public String getCharset() {
			return charset;
		}

		@Override
		public String getRemoteId() {
			return remoteId;
		}

		@Override
		public String getRemoteSourceName() {
			return remoteSourceName;
		}

		@Override
		public ContentMetadata getMetadata() {
			return metadataResolver.get();
		}

		@Override
		public boolean isIndexable() {
			return indexable;
		}

		@Override
		public boolean isReuseable() {
			return reused;
		}

		@Override
		public String getChecksum() {
			return checksum;
		}
	}

	/**
	 * Proxy class for {@link ContentInfo} to {@link Content}
	 *
	 * @author BBonev
	 */
	private static class ContentProxy implements Content {
		private static final long serialVersionUID = -1712136717903808450L;

		private final String name;
		private final String purpose;
		private final long length;
		private final String mimeType;
		private FileDescriptor descriptor;
		private final String charset;
		private final boolean view;
		private final boolean indexable;
		private final String contentId;

		/**
		 * Instantiates a new content proxy.
		 *
		 * @param info
		 *            the info
		 */
		public ContentProxy(ContentInfo info) {
			name = info.getName();
			purpose = info.getContentPurpose();
			length = info.getLength();
			mimeType = info.getMimeType();
			if (info.exists()) {
				descriptor = FileDescriptor.create(info::getName, info::getInputStream, length);
			}
			charset = info.getCharset();
			view = info.isView();
			indexable = info.isIndexable();
			contentId = info.getContentId();
		}

		@Override
		public FileDescriptor getContent() {
			return descriptor;
		}

		@Override
		public Content setContent(FileDescriptor content) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Content setContent(String content, String charset) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Content setContent(String content, Charset charset) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPurpose() {
			return purpose;
		}

		@Override
		public Content setPurpose(String purpose) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Content setName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getMimeType() {
			return mimeType;
		}

		@Override
		public Content setMimeType(String mimeType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long getContentLength() {
			return Long.valueOf(length);
		}

		@Override
		public Content setContentLength(Long length) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getCharset() {
			return charset;
		}

		@Override
		public Content setCharset(String charset) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, Serializable> getProperties() {
			return Collections.emptyMap();
		}

		@Override
		public Content setProperties(Map<String, Serializable> properties) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isView() {
			return view;
		}

		@Override
		public Content setView(boolean view) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isVersionable() {
			return false;
		}

		@Override
		public Content setVersionable(boolean versionable) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isIndexable() {
			return indexable;
		}

		@Override
		public Content setIndexable(boolean indexable) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean shouldDetectedMimeTypeFromContent() {
			return false;
		}

		@Override
		public Content setDetectedMimeTypeFromContent(boolean isDetectedFromContent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isReuseAllowed() {
			// we do not keep this information
			return false;
		}

		@Override
		public Content allowReuse() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getContentId() {
			return contentId;
		}

		@Override
		public Content setContentId(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isContentStoreEnforcedOnVersionUpdate() {
			return true;
		}

		@Override
		public Content disableContentStoreEnforcingOnVersionUpdate() {
			return this;
		}
	}

	/**
	 * Represents the previous content copy if any. The instance stores the content located in the given
	 * {@link ContentInfo} of the previous version in a temporary store.
	 *
	 * @author BBonev
	 */
	static class PreviousVersion {

		static final PreviousVersion NO_PREVIOUS_VERSION = new PreviousVersion();
		private final ContentInfo previousContentInfo;
		private final StoreItemInfo tempCopy;
		private final ContentStoreProvider storeProvider;

		/**
		 * Instantiates empty previous version instance
		 */
		private PreviousVersion() {
			previousContentInfo = null;
			tempCopy = null;
			storeProvider = null;
		}

		/**
		 * Instantiates a new previous version. The instance stores the content located in the given {@link ContentInfo}
		 * of the previous version in a temporary store.
		 *
		 * @param contentStoreProvider
		 *            the content store provider
		 * @param instance
		 *            the instance
		 * @param previousVersion
		 *            the previous version
		 */
		PreviousVersion(ContentStoreProvider contentStoreProvider, Serializable instance, ContentInfo previousVersion) {
			storeProvider = contentStoreProvider;
			this.previousContentInfo = previousVersion;
			tempCopy = storeProvider.getTempStore().add(instance, new ContentProxy(previousVersion));
		}

		/**
		 * Gets the previous version.
		 *
		 * @return the previous version
		 */
		ContentInfo getPreviousVersion() {
			return previousContentInfo;
		}

		/**
		 * Delete the stored content if any
		 */
		void delete() {
			if (tempCopy != null && storeProvider != null) {
				storeProvider.getTempStore().delete(tempCopy);
			}
		}
	}
}
