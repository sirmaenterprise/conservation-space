package com.sirma.itt.emf.audit.command;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Retrieves the action/operation ID from {@link EmfEvent} that extends {@link OperationEvent} and
 * stores it in {@link AuditActivity}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 4)
public class AuditActionIDCommand implements AuditCommand {

	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		if (event instanceof OperationEvent && activity != null) {
			OperationEvent operationEvent = (OperationEvent) event;
			activity.setActionID(operationEvent.getOperationId());
		}
	}
}
