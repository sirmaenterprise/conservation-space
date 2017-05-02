package com.sirma.itt.emf.audit.command.instance;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Add the relation status to the {@link AuditActivity}. Relation statuses are described in the {@link AuditActivity}.
 *
 * @author nvelkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 19)
public class AuditRelationStatusCommand extends AuditAbstractCommand {

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		activity.setRelationStatus(payload.getStatus());
	}

}
