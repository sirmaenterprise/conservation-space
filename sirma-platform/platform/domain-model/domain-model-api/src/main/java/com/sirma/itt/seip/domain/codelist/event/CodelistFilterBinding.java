package com.sirma.itt.seip.domain.codelist.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class CodelistFilterBinding.
 *
 * @author BBonev
 */
public class CodelistFilterBinding extends AnnotationLiteral<CodelistFilter>implements CodelistFilter {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 5167253690127407351L;

	/** The codelist. */
	private int codelist;

	/** The filter event. */
	private String filterEvent;

	/**
	 * Instantiates a new codelist filter binding.
	 *
	 * @param codelist
	 *            the codelist
	 * @param filterEvent
	 *            the filter event
	 */
	public CodelistFilterBinding(int codelist, String filterEvent) {
		this.codelist = codelist;
		this.filterEvent = filterEvent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int codelist() {
		return codelist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String filterEvent() {
		return filterEvent;
	}

}
