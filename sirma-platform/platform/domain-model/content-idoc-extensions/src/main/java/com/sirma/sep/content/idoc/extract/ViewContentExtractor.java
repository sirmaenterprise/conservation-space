package com.sirma.sep.content.idoc.extract;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.content.event.InstanceViewEvent;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;

/**
 * Observer for {@link InstanceViewEvent} events ({@link com.sirma.itt.seip.content.event.InstanceViewAddedEvent} or
 * {@link com.sirma.itt.seip.content.event.InstanceViewUpdatedEvent}). When event occurred an action of view content
 * extraction will be scheduled by {@link SchedulerService}.
 *
 * @author Boyan Tonchev.
 */
@ApplicationScoped
public class ViewContentExtractor {

	private static final int MAX_RETRIES = 5;

	@Inject
	private SchedulerService schedulerService;

	/**
	 * Create an {@link ScheduleViewContentExtraction} action. Creation of the action will be skipped if owner of
	 * action is version instance.
	 *
	 * @param event
	 * 		the InstanceViewEvent.
	 */
	void onInstanceViewEvent(@Observes InstanceViewEvent event) {
		Serializable owner = event.getOwner();
		if (owner instanceof Entity) {
			Serializable instanceId = ((Entity) owner).getId();
			if (InstanceVersionService.isVersion(instanceId)) {
				return;
			}
			SchedulerContext context = ScheduleViewContentExtraction.createContext(instanceId);
			SchedulerConfiguration configuration = buildConfiguration();
			schedulerService.schedule(ScheduleViewContentExtraction.NAME, configuration, context);
		}
	}

	/**
	 * Build configuration for asynchronous action.
	 *
	 * @return builded action configuration.
	 */
	private static SchedulerConfiguration buildConfiguration() {
		return new DefaultSchedulerConfiguration().setType(SchedulerEntryType.TIMED)
				.setScheduleTime(new Date())
				.setSynchronous(false)
				.setRemoveOnSuccess(true)
				.setPersistent(true)
				.setTransactionMode(TransactionMode.NOT_SUPPORTED)
				.setMaxRetryCount(MAX_RETRIES)
				.setIncrementalDelay(true)
				.setMaxActivePerGroup(ScheduleViewContentExtraction.NAME, 5);
	}
}