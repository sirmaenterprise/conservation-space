package com.sirma.itt.seip.instance.revision.steps;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerRetryException;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Publish step that makes sure the thumbnail of the original instance is transferred to the revision. The steps takes
 * the currently active thumbnail for the source instance and adds it as assigned thumbnail. This way if the revision
 * has it's own template it will not override this from the source instance.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(SyncThumbnailPublishStep.NAME)
@Extension(target = PublishStep.EXTENSION_NAME, order = 115)
public class SyncThumbnailPublishStep extends SchedulerActionAdapter implements PublishStep {

	public static final String NAME = "SyncThumbnailPublishStep";

	private static final String THUMBNAIL_SOURCE = "source";
	private static final String THUMBNAIL_TARGET = "target";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstancePropertyNameResolver nameResolver;

	@Inject
	private ThumbnailService thumbnailService;

	@Inject
	private RenditionService renditionService;

	@Inject
	private SchedulerService schedulerService;

	@Override
	public void execute(PublishContext publishContext) {
		Instance source = publishContext.getRequest().getInstanceToPublish();
		// for thumbnail source use the current assigned instance template or the current instance
		Serializable thumbnailSource = source.get(LinkConstants.HAS_THUMBNAIL, source.getId(), nameResolver);
		Serializable revisionId = publishContext.getRevision().getId();

		String thumbnail = renditionService.getThumbnail(thumbnailSource);
		if (isValidThumbnail(thumbnail)) {
			thumbnailService.addAssignedThumbnail(revisionId, thumbnail);
			LOGGER.debug("Assigned thumbnail {} to revisionId {}", thumbnailSource, revisionId);
		} else {
			LOGGER.debug("The thumbnail of {} is not fetched, yet. Will try to fetch it later for {}",
					thumbnailSource, revisionId);
			scheduleAsyncThumbnailCheck(thumbnailSource.toString(), revisionId.toString());
		}
	}

	private static boolean isValidThumbnail(String thumbnail) {
		return thumbnail != null && !ThumbnailService.MAX_RETRIES.equals(thumbnail);
	}

	private void scheduleAsyncThumbnailCheck(String thumbnailSource, String thumbnailTarget) {
		SchedulerContext context = new SchedulerContext();
		context.put(THUMBNAIL_SOURCE, thumbnailSource);
		context.put(THUMBNAIL_TARGET, thumbnailTarget);

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.add(Calendar.MINUTE, 1);

		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.TIMED)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setPersistent(true)
					.setMaxRetryCount(50)
					.setRetryDelay(60L)
					.setScheduleTime(calendar.getTime());
		// if revision building fails and the user retries we will use the same entry if it's still there
		configuration.setIdentifier(thumbnailTarget);

		schedulerService.schedule(NAME, configuration, context);
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String sourceId = context.getIfSameType(THUMBNAIL_SOURCE, String.class);
		String thumbnail = renditionService.getThumbnail(sourceId);
		if (!isValidThumbnail(thumbnail)) {
			throw new SchedulerRetryException("Thumbnail not available, yet");
		}
		String revisionId = context.getIfSameType(THUMBNAIL_TARGET, String.class);
		thumbnailService.addAssignedThumbnail(revisionId, thumbnail);
		LOGGER.debug("Delayed fetch thumbnail {} for {}", sourceId, thumbnail);
	}

	@Override
	public String getName() {
		return Steps.SYNC_THUMBNAIL.getName();
	}

}
