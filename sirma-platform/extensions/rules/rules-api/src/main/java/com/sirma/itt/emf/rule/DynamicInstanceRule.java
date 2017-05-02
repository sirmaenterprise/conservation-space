package com.sirma.itt.emf.rule;

import com.sirma.itt.seip.context.Configurable;

/**
 * Rule that support dynamic configurations. All dynamic rules are loaded via {@link javax.inject.Named} bean loading.
 * The implementation of this object should NOT be annotated with stateful scope and defined in any scope different than
 * {@link javax.enterprise.inject.Default}.
 *
 * @author BBonev
 */
public interface DynamicInstanceRule extends InstanceRule, Configurable, DynamicSupportable {

	/** Configuration property of type boolean to define async support. */
	String ASYNC_SUPPORT = "asyncSupport";
	/** Configuration property to add additional configurations to the rules. */
	String CONFIG = "ruleConfig";
	/**
	 * Configuration property to add a filtering on which instance types this rule should be executed.
	 */
	String INSTANCE_TYPES = "instanceTypes";
	/** Configuration property to add a filtering on which operations this rule should be executed. */
	String OPERATIONS = "onOperations";
	/** Configuration property to add a filtering on which definitions this rule should be executed. */
	String ON_DEFINITIONS = "onDefinitions";
	/** Context property that holds the path to the definition rule configuration. */
	String DEFINED_IN = "definedIn";
}
