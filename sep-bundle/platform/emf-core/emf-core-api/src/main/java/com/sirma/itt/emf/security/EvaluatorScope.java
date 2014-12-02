package com.sirma.itt.emf.security;

/**
 * Enum that defines the role evaluator scopes
 *
 * @author BBonev
 */
public enum EvaluatorScope {

	/**
	 * The internal role evaluator. This defines the default implementation for
	 * the role evaluators.
	 */
	INTERNAL,
	/**
	 * The external role evaluator. This is the extension point for extending
	 * the role evaluator capabilities. This evaluator is called if present to
	 * determine the role when the internal one cannot determine it.
	 */
	EXTERNAL;
}
