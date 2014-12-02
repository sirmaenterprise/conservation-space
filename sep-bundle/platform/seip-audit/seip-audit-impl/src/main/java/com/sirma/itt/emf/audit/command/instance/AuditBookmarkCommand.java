package com.sirma.itt.emf.audit.command.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Creates a bookmark to specific instance.
 * 
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 6)
public class AuditBookmarkCommand extends AuditAbstractCommand {

	/** Manager used when creating URLs to EMF instances. */
	@Inject
	private ExpressionsManager expressionsManager;

	@Override
	public void execute(EmfEvent event, AuditActivity activity) {
		Instance instance = getInstance(event);
		if (instance != null && activity != null && instance.getId() != null) {
			activity.setObjectURL(createBookmarkLink(instance));
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
		return expressionsManager.evaluateRule("${link(currentInstance)}", String.class, context,
				instance);
	}
}
