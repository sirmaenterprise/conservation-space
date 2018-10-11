package com.sirma.itt.emf.rule;

import com.sirma.itt.seip.Applicable;
import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.context.Configurable;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Operation to be executed on an instance that has been matched in a complex {@link InstanceRule}.
 *
 * @author BBonev
 */
public interface RuleOperation extends Named, SupportablePlugin<String>, Configurable, DynamicSupportable, Applicable {

	/**
	 * Configuration property to define what operation should be logged in the audit log if present. If not defined
	 * nothing should be logged in the audit log.
	 */
	String EVENT_ID = "eventId";

	/**
	 * Processing started. Method called before calling the execute method for the first time on a recognition rule
	 * execution. The method is called once after the first instance is matched successfully and before calling
	 * {@link #execute(Context, Instance, Context)} method. The context object passed here will be passed on the
	 * {@link #execute(Context, Instance, Context)} method and {@link #processingEnded(Context, Context)} and could be
	 * used to store/collect information during the rule processing.
	 *
	 * @param context
	 *            the context global execution context that contains the currently processed instance and other
	 *            information.
	 * @param processingContext
	 *            the processing context. <b>Note</b> that this context could be shared between multiple threads.
	 */
	void processingStarted(Context<String, Object> context, Context<String, Object> processingContext);

	/**
	 * Execute the current operation on the provided instances.
	 *
	 * @param context
	 *            the context that hold information for the processing instance and other information
	 * @param matchedInstance
	 *            a non <code>null</code> instance that has passed the filtering stage. The instance may not be fully
	 *            loaded.
	 * @param processingContext
	 *            the processing context. <b>Note</b> that this context could be shared between multiple threads.
	 */
	void execute(Context<String, Object> context, Instance matchedInstance, Context<String, Object> processingContext);

	/**
	 * Processing ended. Method called after calling the execute method for the last time on a recognition rule
	 * execution. The method is called once after the last instance is processed successfully and before rule
	 * completion.
	 *
	 * @param context
	 *            the context that hold information for the processing instance and other information
	 * @param processingContext
	 *            the processing context. <b>Note</b> that this context could be shared between multiple threads.
	 */
	void processingEnded(Context<String, Object> context, Context<String, Object> processingContext);

}
