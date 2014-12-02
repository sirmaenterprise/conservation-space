package com.sirma.cmf.web.userdashboard.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Narrows the observer selection for DashletToolbarActionEvent's.
 * 
 * @author svelikov
 */
public class DashletToolbarActionBinding extends AnnotationLiteral<SelectedDashletToolbarAction>
		implements SelectedDashletToolbarAction {

	private static final long serialVersionUID = -115318244776968604L;

	private final String dashlet;
	private final String action;

	/**
	 * Instantiates a new dashlet toolbar action binding.
	 * 
	 * @param dashlet
	 *            the dashlet
	 * @param action
	 *            the action
	 */
	public DashletToolbarActionBinding(String dashlet, String action) {
		this.action = action;
		this.dashlet = dashlet;
	}

	@Override
	public String dashlet() {
		return dashlet;
	}

	@Override
	public String action() {
		return action;
	}

}
