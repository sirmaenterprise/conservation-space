package com.sirma.sep.content;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tasks.SchedulerAction;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.ContentPersistProvider.ContentPersister;
import com.sirma.sep.content.ContentPersistProvider.PreviousVersion;

/**
 * {@link SchedulerAction} that copy content from one instance to other. The action performs the actual content copy
 * with retry.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(AsyncContentCopyAction.NAME)
public class AsyncContentCopyAction extends SchedulerActionAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String NAME = "asyncContentCopyAction";
	static final String SOURCE_CONTENT_ID_KEY = "sourceContentId";
	static final String TARGET_CONTENT_ID_KEY = "targetContentId";

	private static final List<Pair<String, Class<?>>> ARGUMENTS = Arrays
			.asList(new Pair<>(SOURCE_CONTENT_ID_KEY, String.class), new Pair<>(TARGET_CONTENT_ID_KEY, String.class));

	@Inject
	private ContentStoreProvider storeProvider;
	@Inject
	private InstanceContentService contentService;
	@Inject
	private ContentEntityDao entityDao;
	@Inject
	private ContentPersistProvider contentPersistProvider;

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS;
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String sourceContentId = context.getIfSameType(SOURCE_CONTENT_ID_KEY, String.class);
		String targetContentId = context.getIfSameType(TARGET_CONTENT_ID_KEY, String.class);

		TimeTracker tracker = TimeTracker.createAndStart();

		ContentInfo sourceInfo = contentService.getContent(sourceContentId, null);
		if (!sourceInfo.exists()) {
			// this should trigger a retry
			throw new EmfRuntimeException(
					MessageFormat.format("Could not copy content. Source content {0} not found!", sourceContentId));
		}

		String storeName = sourceInfo.getRemoteSourceName();
		ContentStore contentStore = storeProvider.findStore(storeName).orElseThrow(() -> new EmfRuntimeException(
				MessageFormat.format("Target content store {0} not found!", storeName)));
		ContentEntity contentEntity = entityDao.getEntity(targetContentId, "any");
		if (contentEntity == null) {
			// this will benefit from a retry probably a problem with the persist.
			throw new EmfRuntimeException(
					MessageFormat.format("Content entity for content id {0} not found!", targetContentId));
		}

		LOGGER.debug("Start async content copy for instance {}", contentEntity.getInstanceId());

		Content content = Content.createFrom(sourceInfo).setContent(sourceInfo);

		ContentPersister persister = contentPersistProvider.getPersister(content);

		// pass dummy instance. It's not used for something important
		// use isNew=true to call the add method of the content store
		persister.uploadContentAndPersist(new EmfInstance(), content, contentEntity,
				PreviousVersion.NO_PREVIOUS_VERSION, null, contentStore, true, false);

		LOGGER.debug("Completed async content copy for instance {} in {} ms", contentEntity.getInstanceId(),
				tracker.stop());
	}
}
