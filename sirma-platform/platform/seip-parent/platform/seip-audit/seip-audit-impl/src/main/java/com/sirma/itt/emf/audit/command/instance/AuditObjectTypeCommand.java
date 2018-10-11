package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Collects the type for given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 14)
public class AuditObjectTypeCommand extends AuditAbstractCommand {

	private static final String EMF_RELATION_TYPE = "emf:Relation";

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null) {
			// TODO: Workaround, the link instance doesn't return getFirstUri() correctly for some
			// reason.
			if (instance.getClass().equals(LinkInstance.class)) {
				activity.setObjectType(EMF_RELATION_TYPE);
			} else {
				String shortUri = namespaceRegistryService.getShortUri(instance.type().getId().toString());
				activity.setObjectType(shortUri);
			}
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String objectType = activity.getObjectType();
		if (StringUtils.isNotBlank(objectType)) {
			ClassInstance classInstance = semanticDefinitionService.getClassInstance(objectType);
			String property = classInstance.getProperty(DefaultProperties.HEADER_BREADCRUMB);
			activity.setObjectTypeLabel(property);
		}
	}

}
