package com.sirma.itt.emf.audit.command.instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Collects the previous state of given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 10)
public class AuditPreviousStateCommand implements AuditCommand {

	@Inject
	private FieldValueRetrieverService retriever;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		if (payload.getTriggeredBy() instanceof PropertiesChangeEvent) {
			PropertiesChangeEvent changeEvent = (PropertiesChangeEvent) payload.getTriggeredBy();
			Map<String, Serializable> removedProps = changeEvent.getRemoved();
			if (removedProps != null) {
				Serializable prevState = removedProps.get(DefaultProperties.STATUS);
				if (prevState != null) {
					activity.setObjectPreviousState(prevState.toString());
				}
			}
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String objectType = activity.getObjectType();
		if (StringUtils.isNotBlank(objectType)) {
			String objectTypeFullUri = namespaceRegistryService.buildFullUri(objectType);
			SearchRequest objectStateParams = new SearchRequest(new HashMap<>(1));
			objectStateParams.add(FieldValueRetrieverParameters.OBJECTTYPE, objectTypeFullUri);

			String objectPreviousState = activity.getObjectPreviousState();
			if (StringUtils.isNotBlank(objectPreviousState)) {
				String previousStateLabel = retriever.getLabel(FieldId.OBJECT_STATE, activity.getObjectPreviousState(),
						objectStateParams);
				activity.setObjectPreviousStateLabel(previousStateLabel);
			}
		}
	}
}
