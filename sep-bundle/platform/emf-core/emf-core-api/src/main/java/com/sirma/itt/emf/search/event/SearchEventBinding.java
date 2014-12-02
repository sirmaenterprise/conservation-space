package com.sirma.itt.emf.search.event;

import javax.enterprise.util.AnnotationLiteral;


/**
 * Binding class for {@link SearchEvent} qualifier.
 *
 * @author BBonev
 */
public class SearchEventBinding extends AnnotationLiteral<SearchEvent> implements
		SearchEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2976653753808063778L;

	/** The type. */
	private final String type;

	/** The when. */
	private final SearchEventType when;

	/**
	 * Instantiates a new search event binding.
	 *
	 * @param type
	 *            the type
	 * @param when
	 *            the when
	 */
	public SearchEventBinding(String type, SearchEventType when) {
		this.type = type;
		this.when = when;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String type() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchEventType when() {
		return when;
	}

}
