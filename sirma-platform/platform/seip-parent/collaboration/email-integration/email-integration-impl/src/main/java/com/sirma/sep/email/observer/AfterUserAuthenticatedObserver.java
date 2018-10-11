package com.sirma.sep.email.observer;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.USER_FULL_URI;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.email.ShareFolderMountSchedulerAction;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.MailboxSupportableService;

/**
 * Observer class for the {@link UserAuthenticatedEvent} event, which checks if the user is eligible to be mounted to
 * the share folder and mounts it to his contacts folder.
 *
 * @author g.tsankov
 */
@Singleton
public class AfterUserAuthenticatedObserver {

	@Inject
	private MailboxSupportableService mailboxSupportableService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SchedulerService schedulerService;

	/**
	 * Mounts share folder to mailbox after user is authenticated and is eligible.
	 *
	 * @param event
	 *            {@link UserAuthenticatedEvent} after user is logged.
	 * @throws EmailIntegrationException
	 *             thrown if share folder couldn't be mounted.
	 */
	public void mountShareFolder(@Observes UserAuthenticatedEvent event) throws EmailIntegrationException {
		if (!securityContext.isSystemTenant() && event.isInitiatedByUser() && mailboxSupportableService.isMailboxSupportable(USER_FULL_URI)) {
			String emailAddress = (String) event.getAuthenticatedUser().getProperties().get(EMAIL_ADDRESS);

			schedulerService.schedule(ShareFolderMountSchedulerAction.NAME, buildImmediateConfiguration(),
					buildScheduleContext(emailAddress));
		}
	}

	private static SchedulerContext buildScheduleContext(String emailAddress) {
		SchedulerContext context = new SchedulerContext(1);
		context.put(EMAIL_ADDRESS, emailAddress);
		return context;
	}

	private static SchedulerConfiguration buildImmediateConfiguration() {
		return new DefaultSchedulerConfiguration().setType(SchedulerEntryType.IMMEDIATE)
				.setRemoveOnSuccess(true)
				.setMaxRetryCount(5)
				.setIncrementalDelay(true)
				.setRunAs(RunAs.USER);
	}
}
