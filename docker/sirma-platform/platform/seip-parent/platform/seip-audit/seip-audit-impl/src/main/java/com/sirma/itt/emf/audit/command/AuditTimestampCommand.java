package com.sirma.itt.emf.audit.command;

import java.util.Date;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Sets the timestamp when a specific instance is modified and sets it in {@link AuditActivity}.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 3)
public class AuditTimestampCommand extends AuditAbstractCommand {

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		activity.setEventDate(new Date());
	}
}
