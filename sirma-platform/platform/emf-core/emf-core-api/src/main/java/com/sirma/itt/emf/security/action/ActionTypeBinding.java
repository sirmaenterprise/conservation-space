package com.sirma.itt.emf.security.action;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Extension class used to select appropriate event qualifier for allowed actions events.
 *
 * @author svelikov
 */
public class ActionTypeBinding extends AnnotationLiteral<EMFAction>implements EMFAction {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6569414663818091578L;

	/** The type. */
	private final String type;

	/** The target. */
	private final Class<?> target;

	/**
	 * Instantiates a new action type binding.
	 *
	 * @param type
	 *            the type
	 * @param target
	 *            the target
	 */
	public ActionTypeBinding(String type, Class<?> target) {
		this.type = type;
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return type;
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	@Override
	public Class<?> target() {
		return target;
	}

}
