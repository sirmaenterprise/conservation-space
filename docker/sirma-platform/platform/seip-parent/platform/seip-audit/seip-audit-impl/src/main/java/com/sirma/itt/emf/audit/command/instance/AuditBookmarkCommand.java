package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Creates a bookmark to specific instance.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 6)
public class AuditBookmarkCommand extends AuditAbstractCommand {

	@Inject
	private ExpressionsManager expressionsManager;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null && instance.getId() != null) {
			String link = createBookmarkLink(instance);
			activity.setObjectURL(link);
		}
	}

	/**
	 * Creates an URL bookmark of an EMF object. The host and port are not included to the bookmark.
	 *
	 * @param instance
	 *            the object
	 * @return the URL bookmark
	 */
	// TODO: Handle when instances cannot be bookmarked.
	private String createBookmarkLink(Instance instance) {
		ExpressionContext context = expressionsManager.createDefaultContext(instance, null, null);
		return expressionsManager.evaluateRule("${link(currentInstance)}", String.class, context, instance);
	}

}
