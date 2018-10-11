package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Collects the title for given instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 15)
public class AuditObjectTitleCommand extends AuditAbstractCommand {

	private static final String RELATION_TYPE = "emf:Relation";

	@Inject
	private LabelProvider labelProvider;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null) {
			String title = getProperty(instance.getProperties(), DefaultProperties.TITLE);
			activity.setObjectTitle(title);
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		String objectType = activity.getObjectType();
		if (StringUtils.isNotBlank(objectType)) {
			if (RELATION_TYPE.equals(objectType)) {
				String relationType = labelProvider.getValue("emf.audit.relationoftype");
				String objectSubTypeLabel = activity.getObjectSubTypeLabel();
				activity.setObjectTitle(relationType + " \"" + objectSubTypeLabel + "\"");
			} else {
				String title = context.getObjectHeaders().get(activity.getObjectSystemID());
				if (StringUtils.isNotBlank(title)) {
					activity.setObjectTitle(title);
				}
			}
		}
	}

}
