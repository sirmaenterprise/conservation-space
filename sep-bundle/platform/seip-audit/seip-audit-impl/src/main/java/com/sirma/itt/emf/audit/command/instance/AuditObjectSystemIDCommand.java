package com.sirma.itt.emf.audit.command.instance;

import java.io.Serializable;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Collects the system ID of given instance.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 9)
public class AuditObjectSystemIDCommand extends AuditAbstractCommand {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		Instance instance = getInstance(event);
		if (instance != null && activity != null) {
			Serializable id = instance.getId();
			if (id != null) {
				activity.setObjectSystemID(id.toString());
			}
		}
	}

}
