package com.sirma.itt.emf.security;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Defines a selector for the {@link RoleEvaluatorType}.
 *
 * @author BBonev
 */
public final class RoleEvaluatorBinding extends AnnotationLiteral<RoleEvaluatorType> implements
		RoleEvaluatorType {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 9039473520380991092L;

	/** The value. */
	private final String value;

	/** The scope. */
	private final EvaluatorScope scope;

	/**
	 * Instantiates a new role evaluator binding.
	 *
	 * @param value
	 *            the value
	 * @param scope
	 *            the scope
	 */
	public RoleEvaluatorBinding(String value, EvaluatorScope scope) {
		this.value = value;
		this.scope = scope;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return value;
	}

	@Override
	public EvaluatorScope scope() {
		return scope;
	}

}
