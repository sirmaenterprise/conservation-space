package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerRetryException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import org.apache.commons.lang.RandomStringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Handles content for instances we want to be shared and be publicly visible.
 * <br />
 * In order to do that we need to execute an import for that content. This way we'll reuse the content, instead of
 * creating new one.
 * <br />
 * The action will extract the primary contents of the passed instances, then from the returned content it will create
 * {@link ContentImport} objects and import them, which will create new record in the content table which will point to
 * the primary content of the instances.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
@Named(ShareContentUploadedInstancesAction.ACTION_NAME)
public class ShareContentUploadedInstancesAction extends BaseShareInstanceContentAction {

	@SuppressWarnings("WeakerAccess")
	protected static final String ACTION_NAME = "shareContentUploadedInstancesAction";

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		ContentShareData data = context.getIfSameType(DATA, ContentShareData.class);

		if (data.getInstanceId().isEmpty()) {
			throw new IllegalArgumentException("The Scheduled context was incorrectly initialized with empty data.");
		}

		ContentInfo content = instanceContentService.getContent(data.getInstanceId(), Content.PRIMARY_CONTENT);
		ContentImport toImport = ContentImport.copyFrom(content)
				.setPurpose(SHARED_CONTENT_PURPOSE_PREFIX + RandomStringUtils.randomAlphanumeric(4))
				.setInstanceId(content.getInstanceId())
				.setContentId(data.getContentId());

		String importedContent = instanceContentService.importContent(toImport);
		if (importedContent == null) {
			throw new SchedulerRetryException(
					"Failed to import share content for the uploaded objects. The task will be rescheduled.");
		}
	}
}