package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Collects the sub type for given instance.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 15)
public class AuditObjectSubTypeCommand extends AuditAbstractCommand {

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	// TODO: execute this in a new transaction.
	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		Instance instance = getInstance(event);
		if (instance != null && activity != null) {
			String subType = getProperty(instance.getProperties(), DefaultProperties.TYPE);
			if ("savedFilter".equals(subType)) {
				activity.setObjectSubType(namespaceRegistryService.getShortUri(dictionaryService
						.getDataTypeDefinition(subType.toLowerCase()).getFirstUri()));
			} else {
				activity.setObjectSubType(subType);
			}
		}
	}

}
