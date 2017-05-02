package com.sirma.itt.emf.audit.command.instance;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Collects the business ID of given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 8)
public class AuditObjectIDCommand extends AuditAbstractCommand {

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null) {
			String objectID = getProperty(instance.getProperties(), DefaultProperties.UNIQUE_IDENTIFIER);
			activity.setObjectID(objectID);
		}
	}

}
