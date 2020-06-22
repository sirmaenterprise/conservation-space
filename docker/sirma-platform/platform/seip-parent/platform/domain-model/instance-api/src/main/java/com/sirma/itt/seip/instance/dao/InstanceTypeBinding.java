package com.sirma.itt.seip.instance.dao;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link InstanceType} annotation.
 *
 * @author BBonev
 */
public class InstanceTypeBinding extends AnnotationLiteral<InstanceType>implements InstanceType {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1239461246009617118L;
	/** The type. */
	private final String type;

	/**
	 * Instantiates a new instance type binding.
	 *
	 * @param type
	 *            the type
	 */
	public InstanceTypeBinding(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String type() {
		return type;

	}
}
