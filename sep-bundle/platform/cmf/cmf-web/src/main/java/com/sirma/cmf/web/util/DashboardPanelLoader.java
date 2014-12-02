package com.sirma.cmf.web.util;

import java.util.concurrent.Callable;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Callable instance to execute dashboard panel loading in asynchronously.
 * 
 * @author BBonev
 */
public class DashboardPanelLoader implements Callable<Void> {

	/** The dashboard. */
	private final DashboardPanelActionBase<? extends Instance> dashboard;

	/**
	 * Instantiates a new dashboard panel loader.
	 * 
	 * @param dashboard
	 *            the dashboard
	 */
	public DashboardPanelLoader(DashboardPanelActionBase<? extends Instance> dashboard) {
		this.dashboard = dashboard;
		if (dashboard == null) {
			throw new EmfConfigurationException(
					"Cannot initialize DashboardPanelLoader for null instance");
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void call() throws Exception {
		dashboard.executeDefaultFilter();
		return null;
	}

}
