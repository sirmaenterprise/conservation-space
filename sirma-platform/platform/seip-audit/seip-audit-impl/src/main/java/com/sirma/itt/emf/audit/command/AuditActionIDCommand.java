package com.sirma.itt.emf.audit.command;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Retrieves the action/operation ID from {@link AuditablePayload} and stores it in {@link AuditActivity}.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 4)
public class AuditActionIDCommand implements AuditCommand {

	@Inject
	private FieldValueRetrieverService retriever;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		activity.setActionID(payload.getOperationId());
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String actionId = activity.getActionID();
		activity.setAction(retriever.getLabel(FieldId.ACTION_ID, actionId, null));
	}
}
