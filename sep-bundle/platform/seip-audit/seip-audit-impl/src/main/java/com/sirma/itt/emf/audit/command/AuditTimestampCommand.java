package com.sirma.itt.emf.audit.command;

import java.util.Date;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;

/**
 * Collects the timestamp when a specific instance is modified and sets it in {@link AuditActivity}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 3)
public class AuditTimestampCommand implements AuditCommand {

	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		if (event instanceof PropertiesChangeEvent && activity != null) {
			long timestamp = ((PropertiesChangeEvent) event).getTimestamp();
			activity.setEventDate(new Date(timestamp));
		} else {
			activity.setEventDate(new Date());
		}
	}

}
