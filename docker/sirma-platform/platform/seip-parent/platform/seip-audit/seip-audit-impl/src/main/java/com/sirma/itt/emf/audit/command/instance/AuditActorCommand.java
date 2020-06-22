package com.sirma.itt.emf.audit.command.instance;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.emf.audit.observer.AuditObserverHelper;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.User;

import javax.inject.Inject;

/**
 * Collects the username of the current user performing the audited operation.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 5)
public class AuditActorCommand implements AuditCommand {

	@Inject
	private AuditObserverHelper auditObserverHelper;

	@Inject
	private ResourceService resourceService;

	@Inject
	private HeadersService headersService;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		User currentUser = auditObserverHelper.getCurrentUser();
		activity.setUserName(currentUser.getIdentityId());
		activity.setUserId(currentUser.getSystemId().toString());
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String username = activity.getUserName();
		Resource resource = resourceService.findResource(username);
		String instanceHeader = headersService.generateInstanceHeader(resource, DefaultProperties.HEADER_BREADCRUMB);
		activity.setUserDisplayName(instanceHeader);
	}
}
