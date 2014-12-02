package com.sirma.itt.emf.web.tab;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class SelectedTabBinding.
 * 
 * @author svelikov
 */
public class SelectedTabBinding extends AnnotationLiteral<SelectedTab> implements SelectedTab {

	private static final long serialVersionUID = 3178545446195664815L;

	/** The tab id. */
	private final String tabId;

	/**
	 * Instantiates a new selected tab binding.
	 * 
	 * @param tabId
	 *            the tab id
	 */
	public SelectedTabBinding(String tabId) {
		this.tabId = tabId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return tabId;
	}

}
