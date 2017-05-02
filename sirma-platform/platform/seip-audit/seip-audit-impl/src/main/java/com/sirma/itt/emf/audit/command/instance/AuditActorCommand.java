package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.emf.audit.observer.AuditObserverHelper;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;

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
	private FieldValueRetrieverService retriever;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		User currentUser = auditObserverHelper.getCurrentUser();
		activity.setUserName(currentUser.getIdentityId());
		activity.setUserId(currentUser.getSystemId().toString());
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String username = activity.getUserName();
		activity.setUserDisplayName(retriever.getLabel(FieldId.USERNAME, username, null));
	}
}
