package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
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
 * Publish step that makes sure the thumbnail of the original instance is transferred to the revision
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
	private LinkService linkService;

	@Inject
	private ThumbnailService thumbnailService;

	@Inject
	private RenditionService renditionService;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public void execute(PublishContext publishContext) {
		InstanceReference source = publishContext.getRequest().getInstanceToPublish().toReference();
		List<LinkReference> links = linkService.getLinks(source, LinkConstants.HAS_THUMBNAIL);
		if (isEmpty(links)) {
			// the source instance does not have a custom thumbnail assigned
			return;
		}
		InstanceReference revision = publishContext.getRevision().toReference();
		InstanceReference thumbnailSource = links.get(0).getTo();

		String thumbnail = renditionService.getThumbnail(thumbnailSource.getId());
		if (isValidThumbnail(thumbnail)) {
			thumbnailService.addThumbnail(revision, thumbnail);
			LOGGER.debug("Assigned thumbnail {} to revision {}", thumbnailSource.getId(), revision.getId());
		} else {
			LOGGER.debug("The thumbnail of {} is not fetched, yet. Will try to fetch it later for {}",
					thumbnailSource.getId(), revision.getId());
			scheduleAsyncThumbnailCheck(thumbnailSource.getId(), revision.getId());
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
		// of revision building fails and the user retries we will use the same entry if it's still there
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
		String revisionid = context.getIfSameType(THUMBNAIL_TARGET, String.class);
		InstanceReference revision = instanceTypeResolver.resolveReference(revisionid).orElseThrow(
				() -> new SchedulerRetryException("Revision " + revisionid + " does not exist, yet"));
		thumbnailService.addThumbnail(revision, thumbnail);
		LOGGER.debug("Delayed fetch thumbnail {} for {}", sourceId, thumbnail);
	}

	@Override
	public String getName() {
		return Steps.SYNC_THUMBNAIL.getName();
	}

}
