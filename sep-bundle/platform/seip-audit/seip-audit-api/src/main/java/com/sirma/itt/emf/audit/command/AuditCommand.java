package com.sirma.itt.emf.audit.command;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.plugin.Plugin;

/**
 * Describes a plugable command for collecting audit information.
 * 
 * @author Mihail Radkov
 */
public interface AuditCommand extends Plugin {

	/** Target name for the Plugin API */
	String TARGET_NAME = "AuditCommand";

	/**
	 * Executes a command upon the event and stores the result in an audit activity.
	 * 
	 * @param event
	 *            the event
	 * @param activity
	 *            the activity
	 */
	void execute(EmfEvent event, AuditActivity activity);

}
