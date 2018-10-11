package com.sirma.itt.seip.domain.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class ItemsFilterBinding.
 *
 * @author svelikov
 */
public class ItemsFilterBinding extends AnnotationLiteral<ItemsFilter>implements ItemsFilter {

	private static final long serialVersionUID = 3178545446195664815L;

	/** The filter. */
	private final String filterName;

	/**
	 * Instantiates a new items filter binding.
	 *
	 * @param filterName
	 *            the filter
	 */
	public ItemsFilterBinding(String filterName) {
		this.filterName = filterName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return filterName;
	}

}
