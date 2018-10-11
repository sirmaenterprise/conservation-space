package com.sirma.itt.seip.instance.version;

import javax.inject.Inject;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;

/**
 * Fire {@link CreateVersionContentEvent} that will be intercepted by {@link ScheduleVersionContentCreate}, which will
 * process and store {@link Content#PRIMARY_VIEW} content for the current version instance.
 *
 * @author A. Kunchev
 */
@Extension(target = VersionStep.TARGET_NAME, enabled = true, order = 25)
public class VersionViewContentProcessingStep implements VersionStep {

	@Inject
	private EventService eventService;

	@Override
	public String getName() {
		return "versionViewContentProcessing";
	}

	@Override
	public void execute(VersionContext context) {
		eventService.fire(new CreateVersionContentEvent(context));
	}

}
