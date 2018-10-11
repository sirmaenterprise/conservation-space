package com.sirma.sep.content;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.event.ContentMovedEvent;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Default implementation for the {@link ContentStoreManagementService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/01/2018
 */
@ApplicationScoped
public class ContentStoreManagementServiceImpl implements ContentStoreManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.management.storeProcessingBatchSize", type = Integer.class,
			defaultValue = "10240", system = true, subSystem = "content",
			label = "The number of items to process in a single transaction.")
	private ConfigurationProperty<Integer> storeProcessingBatchSize;

	private static final Pattern INSTANCE_ID_PATTERN = Pattern.compile("(\\w+:[0-9a-f-]+[0-9a-f])[rv.0-9-]*");

	@Inject
	private ContentEntityDao contentEntityDao;

	@Inject
	private Instance<SenderService> senderService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private ContentStoreProvider contentStoreProvider;

	@Inject
	private EventService eventService;

	private SecureRandom random = new SecureRandom();

	@Override
	public Optional<StoreInfo> getInfo(String storeName) {
		Optional<ContentStore> store = contentStoreProvider.findStore(storeName);
		if (!store.isPresent()) {
			return Optional.empty();
		}
		AtomicLong occupiedSpace = new AtomicLong();
		AtomicInteger uniqueCount = new AtomicInteger();

		Consumer<ContentEntity> freeSpaceOp = contentEntity -> occupiedSpace.addAndGet(contentEntity.getContentSize());
		Consumer<ContentEntity> uniqueEntities = acceptOnlyUnique(freeSpaceOp.andThen(e -> uniqueCount.incrementAndGet()));

		int processed = processStoreContents(store.get(), uniqueEntities, new TxCallback(), 0);

		String freeSpaceString = FileUtil.humanReadableByteCount(occupiedSpace.get());
		LOGGER.info(
				"Fetched content store info for '{}'. Found {} files (out of {} entities), occupying {} of storage",
				storeName, uniqueCount.get(), processed, freeSpaceString);
		return Optional.of(new StoreInfo(storeName, uniqueCount.get(), occupiedSpace.get()));
	}

	@Override
	public void emptyContentStore(String storeName) {
		ContentStore contentStore = findStore(storeName);

		// currently used only for logging, but could be used to implement parallel
		// content wipe if non async delete is supported
		boolean asyncDeleteSupported = contentStore.isTwoPhaseDeleteSupported();

		String message = asyncDeleteSupported ? "supports" : "does not support";
		LOGGER.info("Triggered full '{}' content store wipe. The store {} async deletion", storeName, message);

		AtomicLong freeSpace = new AtomicLong();

		// used to store a sender service instance
		JmsTxStore senderServiceProvider = createDeleteJmsTxStore();

		Consumer<ContentEntity> deleteEntityOp = deleteEntity(contentStore, senderServiceProvider, false);
		Consumer<ContentEntity> freeSpaceOp = contentEntity -> freeSpace.addAndGet(contentEntity.getContentSize());
		Consumer<ContentEntity> uniqueEntities = acceptOnlyUnique(deleteEntityOp.andThen(freeSpaceOp));

		int processed = processStoreContents(contentStore, uniqueEntities, senderServiceProvider, storeProcessingBatchSize.get());

		String freeSpaceString = FileUtil.humanReadableByteCount(freeSpace.get());
		LOGGER.info("'{}' content store wipe complete. Deleted {} files occupying {} of storage", storeName, processed,
				freeSpaceString);
	}

	private JmsTxStore createDeleteJmsTxStore() {
		Function<SenderService, MessageSender> senderBuilder = service -> service.createSender(
				ContentDestinations.DELETE_CONTENT_QUEUE, SendOptions.create().asSystem());
		return new JmsTxStore(senderService::get, senderBuilder);
	}

	private Consumer<ContentEntity> acceptOnlyUnique(Consumer<ContentEntity> toWrap) {
		Set<String> uniqueRemoteIds = new HashSet<>(10240);
		return entity -> {
			if (uniqueRemoteIds.contains(entity.getRemoteId())) {
				// skip entity as it's already processed
				return;
			}
			uniqueRemoteIds.add(entity.getRemoteId());
			toWrap.accept(entity);
		};
	}

	private int processStoreContents(ContentStore contentStore, Consumer<ContentEntity> processor,
			TxCallback txCallback, int processingStepSize) {
		int offset = 0;

		int processed;
		do {
			processed = processStoreFragment(contentStore, processor, txCallback, offset, processingStepSize);
			offset += processed;
		} while (processed > 0);

		return offset;
	}

	private int processStoreFragment(ContentStore contentStore, Consumer<ContentEntity> onEntity,
			TxCallback txCallback, int offset, int limit) {
		return transactionSupport.invokeInNewTx(() -> {
			txCallback.beginTx();
			try {
				List<ContentEntity> entities = contentEntityDao.getStoreContents(contentStore.getName(), offset, limit);
				entities.forEach(onEntity);
				return entities.size();
			} finally {
				txCallback.endTx();
			}
		});
	}

	private Consumer<ContentEntity> deleteEntity(ContentStore contentStore, JmsTxStore jmsTxStore, boolean contentOnly) {
		return contentEntity -> {
			StoreItemInfo storeInfo = contentEntity.toStoreInfo();
			Optional<DeleteContentData> deleteContentData = contentStore.prepareForDelete(storeInfo);
			if (deleteContentData.isPresent()) {
				String data = deleteContentData.get()
						.setContentId(contentEntity.getId())
						.setContentOnly(contentOnly)
						.setTenantId(securityContext.getCurrentTenantId()).asJsonString();
				jmsTxStore.getSender().sendText(data);
			} else {
				// if delayed delete is not supported will try immediate
				contentStore.delete(storeInfo);
			}
		};
	}

	@Override
	public StoreInfo moveAllContent(String sourceStoreName, String targetStoreName) {
		ContentStore contentStore = findStore(sourceStoreName);
		findStore(targetStoreName);

		LOGGER.info("Triggered full '{}' content store move to '{}'", sourceStoreName, targetStoreName);

		AtomicLong occupiedSpace = new AtomicLong();
		AtomicInteger uniqueCount = new AtomicInteger();

		// used to store a sender service instance
		Function<SenderService, MessageSender> senderBuilder = service -> service.createSender(
				ContentDestinations.MOVE_CONTENT_QUEUE, SendOptions.create().asTenantAdmin());
		JmsTxStore senderServiceProvider = new JmsTxStore(senderService::get, senderBuilder);

		Consumer<ContentEntity> sendMoveOp = sendMessageForContentMove(targetStoreName, senderServiceProvider);
		Consumer<ContentEntity> freeSpaceOp = contentEntity -> occupiedSpace.addAndGet(contentEntity.getContentSize());
		Consumer<ContentEntity> uniqueEntities = acceptOnlyUnique(sendMoveOp.andThen(freeSpaceOp).andThen(e -> uniqueCount.incrementAndGet()));

		int processed = processStoreContents(contentStore, uniqueEntities, senderServiceProvider, storeProcessingBatchSize.get());

		String freeSpaceString = FileUtil.humanReadableByteCount(occupiedSpace.get());
		LOGGER.info(
				"Preparation for content store move from '{}' to '{}' complete. Moving {} files (out of {} entities), occupying {} of storage",
				sourceStoreName, targetStoreName, uniqueCount.get(), processed, freeSpaceString);
		return new StoreInfo(sourceStoreName, uniqueCount.get(), occupiedSpace.get());
	}

	private Consumer<ContentEntity> sendMessageForContentMove(String targetStoreName,
			JmsTxStore senderServiceProvider) {
		return entity -> {
			String message = Json.createObjectBuilder()
					.add("contentId", entity.getId())
					.add("targetStore", targetStoreName)
					.build().toString();
			SendOptions sendOptions = SendOptions.from(senderServiceProvider.getSender().getDefaultSendOptions());
			String instanceId = entity.getInstanceId();
			int selector = getInstanceSelector(instanceId);
			sendOptions.withProperty("index", "" + selector);
			senderServiceProvider.getSender().sendText(message, sendOptions);
		};
	}

	private int getInstanceSelector(String instanceId) {
		if (instanceId == null) {
			// entry not assign to an instance, use one of the threads
			return random.nextBoolean() ? 0 : 1;
		}
		Matcher matcher = INSTANCE_ID_PATTERN.matcher(instanceId);
		if (!matcher.find()) {
			// not instance id just assign to one of the threads
			return random.nextBoolean() ? 0 : 1;
		}
		String id = matcher.group(1);
		int lastDigit = Integer.parseInt("" + id.charAt(id.length() - 1), 16);
		return lastDigit % 2;
	}

	protected ContentStore findStore(String storeName) {
		return contentStoreProvider.findStore(storeName)
				.orElseThrow(() -> new IllegalArgumentException("No such store:" + storeName));
	}

	@Override
	public void moveContent(String contentId, String targetStoreName) {
		ContentEntity contentEntity = contentEntityDao.getEntity(contentId, "any");
		if (contentEntity == null) {
			LOGGER.warn("Tried to move non existing content with id {} to '{}'. Skipping it",contentId, targetStoreName);
			return;
		}
		if (nullSafeEquals(contentEntity.getRemoteSourceName(), targetStoreName)) {
			LOGGER.info("Content with id {} already moved to '{}'. Skipping it", contentId, targetStoreName);
			return;
		}
		ContentStore targetStore = findStore(targetStoreName);
		ContentStore contentStore = findStore(contentEntity.getRemoteSourceName());
		StoreItemInfo itemInfo = contentEntity.toStoreInfo();
		FileDescriptor readChannel = contentStore.getReadChannel(itemInfo);
		if (readChannel == null) {
			// this can happen if move is called before the content to reach fully the target store
			// when using async transfer to the store. We can just retry after some time
			throw new ContentNotFoundRuntimeException(
					"Content " + contentId + " not found in '" + contentEntity.getRemoteSourceName() + "' store");
		}
		Content content = Content.create(contentEntity.getName(), readChannel)
				.setContentLength(contentEntity.getContentSize());

		StoreItemInfo movedFileInfo = targetStore.add(new EmfInstance(contentEntity.getInstanceId()), content);
		// at this point the content should be stored in the new store
		// we should check if all of it got there
		if (movedFileInfo.getContentLength() != contentEntity.getContentSize()) {
			throw new RollbackedRuntimeException(
					String.format("Failed to transfer content %s to '%s'. File size does not match %d!=%d", contentId,
							targetStoreName, contentEntity.getContentSize(), movedFileInfo.getContentLength()));
		}

		eventService.fire(new ContentMovedEvent(contentId, targetStoreName));

		// sending message for content deletion should be before updating the content entity properties
		// this should delete the content from the original store when current transaction complete successfully
		// this will not affect the database in any way
		doSingleEntityDelete(contentEntity, contentStore);

		// because one content is used in multiple places we need to updated all that entities
		// it's done this was in order to be easy for rollback
		List<ContentEntity> referencedEntities = contentEntityDao.getEntityByRemoteId(contentEntity.getRemoteSourceName(),
				contentEntity.getRemoteId());
		LOGGER.trace("Moved content {}:{} to {}:{}. Affected {} entities", contentEntity.getRemoteSourceName(),
				contentEntity.getRemoteId(), movedFileInfo.getProviderType(), movedFileInfo.getRemoteId(),
				referencedEntities.size());
		for (ContentEntity entity : referencedEntities) {
			entity.setRemoteId(movedFileInfo.getRemoteId());
			entity.setRemoteSourceName(movedFileInfo.getProviderType());
			contentEntityDao.persistEntity(entity, false);
		}

		// update content data so that it now points to the new content store
		contentEntity.setRemoteId(movedFileInfo.getRemoteId());
		contentEntity.setRemoteSourceName(movedFileInfo.getProviderType());

		// at this point the content is copied to it's new place, the old content is scheduled for deletion, and the
		// entry in the database should be updated with the new store information
		// but if transaction fails: the message will not be send, and store information not updated but the new content
		// will be left as is if not deleted
		transactionSupport.invokeOnFailedTransactionInTx(() -> {
			LOGGER.warn(
					"Failed content move for {} to '{}'. Will try to remove the content from the store located at: {}",
					contentEntity.getId(), contentEntity.getRemoteSourceName(), contentEntity.getRemoteId());
			// sending message for content deletion for the new content in case the parent transaction fails
			doSingleEntityDelete(contentEntity, targetStore);
		});
	}

	private void doSingleEntityDelete(ContentEntity entity, ContentStore contentStore) {
		JmsTxStore jmsTxStore = createDeleteJmsTxStore();
		try {
			jmsTxStore.beginTx();
			deleteEntity(contentStore, jmsTxStore, true).accept(entity);
		} finally {
			jmsTxStore.endTx();
		}
	}

	/**
	 * Callback notified for transaction begin and transaction end during processing of the store contents. New
	 * transaction is created for each batch of items. It can be used for initializing and storing state for the
	 * transaction duration
	 */
	private static class TxCallback {

		void beginTx() {
			// nothing to do as default impl
		}

		void endTx() {
			// nothing to do as default impl
		}
	}

	/**
	 * Stores a Sender service instance created for the transaction scope and also a MessageSender instance created via
	 * the provided builder. Both of them are cleared at transaction end.
	 */
	private static class JmsTxStore extends TxCallback {

		final Supplier<SenderService> senderServiceSupplier;
		final Function<SenderService, MessageSender> senderBuilder;
		Supplier<MessageSender> messageSenderSupplier;

		private JmsTxStore(Supplier<SenderService> senderServiceSupplier,
				Function<SenderService, MessageSender> senderBuilder) {
			this.senderServiceSupplier = new CachingSupplier<>(senderServiceSupplier);
			this.senderBuilder = senderBuilder;
		}

		@Override
		public void beginTx() {
			SenderService senderService = senderServiceSupplier.get();
			messageSenderSupplier = new CachingSupplier<>(() -> senderBuilder.apply(senderService));
		}

		MessageSender getSender() {
			return messageSenderSupplier.get();
		}

		@Override
		public void endTx() {
			Resettable.reset(senderServiceSupplier);
			Resettable.reset(messageSenderSupplier);
		}
	}
}
