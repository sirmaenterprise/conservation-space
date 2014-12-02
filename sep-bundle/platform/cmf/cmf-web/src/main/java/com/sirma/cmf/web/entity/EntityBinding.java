package com.sirma.cmf.web.entity;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class EntityBinding.
 * 
 * @author svelikov
 */
public class EntityBinding extends AnnotationLiteral<EntityType> implements EntityType {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3178545446195664815L;

	/** The type. */
	private final Class<?> type;

	/**
	 * Instantiates a new entity binding.
	 * 
	 * @param type
	 *            the type
	 */
	public EntityBinding(Class<?> type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> value() {
		return type;
	}

}
