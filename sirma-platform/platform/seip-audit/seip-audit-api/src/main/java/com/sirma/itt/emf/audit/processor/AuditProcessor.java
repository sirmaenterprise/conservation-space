package com.sirma.itt.emf.audit.processor;

import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.resources.User;

/**
 * Processor for audit activities.
 *
 * @author Mihail Radkov
 */
public interface AuditProcessor {

	/**
	 * Processes user operation.
	 *
	 * @param user
	 *            - the user
	 * @param operationId
	 *            - the operation's ID
	 * @param triggeredBy
	 *            - the event that triggered the auditing
	 */
	void auditUserOperation(User user, String operationId, EmfEvent triggeredBy);

	/**
	 * Audits the provided {@link AuditablePayload}.
	 *
	 * @param payload
	 *            - the provided payload. <b>CANNOT</b> be null
	 */
	void process(AuditablePayload payload);

	/**
	 * Audits the provided {@link AuditablePayload} with extra context.
	 *
	 * @param payload
	 *            - the provided payload. <b>CANNOT</b> be null
	 * @param context
	 *            - any additional context for the activity. <b>CAN</b> be null
	 */
	void process(AuditablePayload payload, String context);

	/**
	 * Audits the provided {@link Instance}, operation id and the event which triggered the auditing. Finally the
	 * processed payload is saved into the audit DB.
	 *
	 * @param instance
	 *            - the instance to be audited. <b>CAN</b> be null
	 * @param operationId
	 *            - the operation id. <b>CANNOT</b> be null or empty
	 * @param triggeredBy
	 *            - the event that originally triggered the auditing. <b>CAN</b> be null
	 */
	void process(Instance instance, String operationId, EmfEvent triggeredBy);

	/**
	 * Audits the provided {@link Instance}, operation id and the event which triggered the auditing. Finally the
	 * processed payload is saved into the audit DB.
	 *
	 * @param instance
	 *            - the instance to be audited. <b>CAN</b> be null
	 * @param operationId
	 *            - the operation id. <b>CANNOT</b> be null or empty
	 * @param triggeredBy
	 *            - the event that originally triggered the auditing. <b>CAN</b> be null
	 * @param context
	 *            - any additional context for the activity. <b>CAN</b> be null
	 */
	void process(Instance instance, String operationId, EmfEvent triggeredBy, String context);

	/**
	 * Audits the provided {@link Instance}, operation id and the event which triggered the auditing. Finally the
	 * processed payload is saved into the audit DB.
	 *
	 * @param instance
	 *            - the instance to be audited. <b>CAN</b> be null
	 * @param operationId
	 *            - the operation id. <b>CANNOT</b> be null or empty
	 * @param triggeredBy
	 *            - the event that originally triggered the auditing. <b>CAN</b> be null
	 * @param context
	 *            - any additional context for the activity. <b>CAN</b> be null
	 * @param showParentPath
	 *            - show instance parent path if true. Hide it otherwise
	 */
	void process(Instance instance, String operationId, EmfEvent triggeredBy, String context, boolean showParentPath);

	/**
	 * Checks if given instance is applicable for observing or not.
	 *
	 * @param instance
	 *            the instance
	 * @return true if yes or false otherwise
	 */
	boolean isInstanceApplicable(Instance instance);

}
