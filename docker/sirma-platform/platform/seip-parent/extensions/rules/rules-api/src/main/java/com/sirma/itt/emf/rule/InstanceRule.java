package com.sirma.itt.emf.rule;

import com.sirma.itt.seip.Applicable;
import com.sirma.itt.seip.AsyncSupportable;
import com.sirma.itt.seip.DefinitionSupportable;
import com.sirma.itt.seip.OperationSupportable;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * The InstanceRule represents interface for Rule. A rule implementation should be stateless in terms of processed data.
 * The rule could store only it's configuration if any but not the processing data.
 *
 * @author Hristo Lungov
 * @author BBonev
 */
public interface InstanceRule extends SupportablePlugin<String>, AsyncSupportable, DefinitionSupportable<String>,
		OperationSupportable<String>, Applicable {

	/**
	 * Plugin name
	 */
	String TARGET_NAME = "instanceRule";

	/**
	 * Gets the rule instance name that identifies the rule instance uniquely
	 *
	 * @return the rule name
	 */
	String getRuleInstanceName();

	/**
	 * Execute the rule using the given context.
	 *
	 * @param context
	 *            current execution context
	 */
	void execute(RuleContext context);

}
