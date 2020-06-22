package com.sirma.itt.emf.audit.command;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.seip.plugin.Plugin;

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
	 * @param payload
	 *            the payload from which to collect auditable information. <b>CANNOT</b> be null
	 * @param activity
	 *            the activity. <b>CANNOT</b> be null
	 */
	void execute(AuditablePayload payload, AuditActivity activity);

	/**
	 * Assigns a label to the provided activity based on the command implementation. It is best if the provided activity
	 * is a copy of the original, because the original values will be changed. For cases where the information in the
	 * activity is not enough, the second parameter {@link AuditContext} comes into play.
	 *
	 * @param activity
	 *            - the provided activity. <b>CANNOT</b> be null
	 * @param context
	 *            - the provided context. <b>CAN</b> be null <b>BUT</b> depends on the command implementation!
	 */
	void assignLabel(AuditActivity activity, AuditContext context);
}
