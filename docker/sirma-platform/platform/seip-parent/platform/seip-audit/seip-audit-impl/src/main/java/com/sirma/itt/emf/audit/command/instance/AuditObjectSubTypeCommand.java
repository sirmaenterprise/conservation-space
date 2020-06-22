package com.sirma.itt.emf.audit.command.instance;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Collects the sub type for given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 13)
public class AuditObjectSubTypeCommand extends AuditAbstractCommand {

	@Inject
	private FieldValueRetrieverService retriever;

	// TODO: execute this in a new transaction.
	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null) {
			activity.setObjectSubType(instance.getIdentifier());
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String objectType = activity.getObjectType();
		if (StringUtils.isNotBlank(objectType)) {
			SearchRequest objectSubTypeParams = new SearchRequest(new HashMap<String, List<String>>(1));
			objectSubTypeParams.add(FieldValueRetrieverParameters.OBJECTTYPE, objectType);
			String objectSubType = activity.getObjectSubType();
			String objectSubTypeLabel = retriever.getLabel(FieldId.OBJECT_SUBTYPE, objectSubType, objectSubTypeParams);
			activity.setObjectSubTypeLabel(objectSubTypeLabel);
		}
	}

}
