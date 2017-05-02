package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Collects the current request id.
 *
 * @author nvelkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 17)
public class AuditRequestIDCommand extends AuditAbstractCommand {

	@Inject
	private SecurityContext securityContext;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		activity.setRequestId(securityContext.getRequestId());
	}

}
