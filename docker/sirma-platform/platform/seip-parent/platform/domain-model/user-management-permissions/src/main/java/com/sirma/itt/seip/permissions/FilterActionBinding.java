package com.sirma.itt.seip.permissions;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Binding object used for selecting {@link FilterAction} qualifier.
 *
 * @author BBonev
 */
public final class FilterActionBinding extends AnnotationLiteral<FilterAction>implements FilterAction {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4585972159737275192L;

	/** The value. */
	private final String value;

	/** The placeholder. */
	private final String placeholder;

	/**
	 * Instantiates a new filter action binding.
	 *
	 * @param value
	 *            the value
	 * @param placeholder
	 *            the placeholder
	 */
	public FilterActionBinding(String value, String placeholder) {
		this.value = value;
		this.placeholder = placeholder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String placeholder() {
		return placeholder;
	}

}
