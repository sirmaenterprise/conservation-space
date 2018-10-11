package com.sirma.sep.email;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.tasks.SchedulerAction;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Scheduler action used to check if email address is eligible for share folder mounting, and mount the folder if
 * eligible.
 *
 * @author g.tsankov
 */
@ApplicationScoped
@Named(ShareFolderMountSchedulerAction.NAME)
public class ShareFolderMountSchedulerAction implements SchedulerAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShareFolderMountSchedulerAction.class);

	public static final String NAME = "userAuthenticationSchedulerAction";

	@Inject
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@Override
	public void beforeExecute(SchedulerContext context) throws Exception {
		// nothing to be done
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String emailAddress = (String) context.get(EMAIL_ADDRESS);
		if (!shareFolderAdministrationService.isShareFolderMounted(emailAddress)) {
			LOGGER.info("Mounting tenant share folder to user {}.", emailAddress);
			shareFolderAdministrationService.mountShareFolderToUser(emailAddress);
		}
	}

	@Override
	public void afterExecute(SchedulerContext context) throws Exception {
		// nothing to be done
	}

}
