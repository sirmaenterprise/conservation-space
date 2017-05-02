package com.sirma.itt.emf.audit.command.instance;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Add the target properties to the {@link AuditActivity}. Target properties contain ids of added or removed instances
 * (Usually when adding and removing relations).
 *
 * @author nvelkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 18)
public class AuditTargetPropertiesCommand extends AuditAbstractCommand {

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		activity.setTargetProperties(payload.getTargetProperties());
	}

}
