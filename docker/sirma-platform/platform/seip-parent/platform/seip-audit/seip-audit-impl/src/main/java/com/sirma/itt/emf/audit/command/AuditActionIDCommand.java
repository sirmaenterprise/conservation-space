package com.sirma.itt.emf.audit.command;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.plugin.Extension;

import javax.inject.Inject;

/**
 * Retrieves the action/operation ID from {@link AuditablePayload} and stores it in {@link AuditActivity}.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 4)
public class AuditActionIDCommand implements AuditCommand {

	@Inject
	private LabelProvider labelProvider;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		activity.setActionID(payload.getOperationId());
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String actionId = activity.getActionID();
		activity.setAction(labelProvider.getLabel(actionId + ".label"));
	}
}
