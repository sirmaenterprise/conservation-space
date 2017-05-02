package com.sirma.itt.emf.audit.command.instance;

import java.io.Serializable;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Collects the system ID of given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 9)
public class AuditObjectSystemIDCommand extends AuditAbstractCommand {

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null) {
			Serializable id = instance.getId();
			if (id != null) {
				activity.setObjectSystemID(id.toString());
			}
		}
	}

}
