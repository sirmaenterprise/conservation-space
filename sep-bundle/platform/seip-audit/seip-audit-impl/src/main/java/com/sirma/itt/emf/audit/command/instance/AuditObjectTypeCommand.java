package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Collects the type for given instance.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 14)
public class AuditObjectTypeCommand extends AuditAbstractCommand {

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	// TODO: execute this in a new transaction.
	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		Instance instance = getInstance(event);
		if (instance != null && activity != null) {
			// TODO: Workaround, the link instance doesn't return getFirstUri() correctly for some
			// reason.
			if (instance.getClass().equals(LinkInstance.class)) {
				activity.setObjectType("emf:Relation");
			} else {
				String type = instance.getClass().getSimpleName().toLowerCase();
				activity.setObjectType(namespaceRegistryService.getShortUri(dictionaryService
						.getDataTypeDefinition(type).getFirstUri()));
			}
		}
	}

}
