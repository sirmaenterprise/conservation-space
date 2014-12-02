package com.sirma.cmf.web.menu;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class MenuActionBinding.
 * 
 * @author svelikov
 */
public class MenuActionBinding extends AnnotationLiteral<NavigationMenu> implements NavigationMenu {

	private static final long serialVersionUID = -8506187076461257407L;

	private final String menuAction;

	/**
	 * Instantiates a new menu action binding.
	 * 
	 * @param menuAction
	 *            the menu action
	 */
	public MenuActionBinding(String menuAction) {
		this.menuAction = menuAction;
	}

	@Override
	public String value() {
		return menuAction;
	}

}
