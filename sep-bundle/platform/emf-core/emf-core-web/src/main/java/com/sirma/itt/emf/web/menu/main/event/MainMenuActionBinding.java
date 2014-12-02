package com.sirma.itt.emf.web.menu.main.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class MainMenuActionBinding.
 * 
 * @author svelikov
 */
public class MainMenuActionBinding extends AnnotationLiteral<SelectedMainMenu> implements
		SelectedMainMenu {

	private static final long serialVersionUID = -115318244776968604L;

	/** The menu action. */
	private final String action;

	/**
	 * Instantiates a new menu action binding.
	 * 
	 * @param action
	 *            the action
	 */
	public MainMenuActionBinding(String action) {
		this.action = action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value() {
		return action;
	}

}
