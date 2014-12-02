package com.sirma.itt.emf.audit.command.instance;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Collects the business ID of given instance.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 8)
public class AuditObjectIDCommand extends AuditAbstractCommand {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		Instance instance = getInstance(event);
		if (instance != null && activity != null) {
			String objectID = getProperty(instance.getProperties(),
					DefaultProperties.UNIQUE_IDENTIFIER);
			activity.setObjectID(objectID);
		}
	}
}
