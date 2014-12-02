package com.sirma.itt.emf.audit.command.instance;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;

/**
 * Collects the previous state of given instance.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 10)
public class AuditPreviousStateCommand implements AuditCommand {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		if (event instanceof PropertiesChangeEvent && activity != null) {
			PropertiesChangeEvent changeEvent = (PropertiesChangeEvent) event;
			Map<String, Serializable> removedProps = changeEvent.getRemoved();
			if (removedProps != null) {
				Serializable prevState = removedProps.get(DefaultProperties.STATUS);
				if (prevState != null) {
					activity.setObjectPreviousState(prevState.toString());
				}
			}
		}
	}

}
