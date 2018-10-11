package com.sirma.itt.emf.audit.command.instance;

import java.util.HashMap;

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
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Collects the primary state of given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 11)
public class AuditObjectStateCommand extends AuditAbstractCommand {

	@Inject
	private FieldValueRetrieverService retriever;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null) {
			String state = getProperty(instance.getProperties(), DefaultProperties.STATUS);
			activity.setObjectState(state);
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String objectType = activity.getObjectType();
		if (StringUtils.isNotBlank(objectType)) {
			String objectTypeFullUri = namespaceRegistryService.buildFullUri(objectType);
			SearchRequest objectStateParams = new SearchRequest(new HashMap<>(1));
			objectStateParams.add(FieldValueRetrieverParameters.OBJECTTYPE, objectTypeFullUri);

			String objectState = activity.getObjectState();
			String objectStateLabel = retriever.getLabel(FieldId.OBJECT_STATE, objectState, objectStateParams);

			activity.setObjectStateLabel(objectStateLabel);
		}
	}
}
