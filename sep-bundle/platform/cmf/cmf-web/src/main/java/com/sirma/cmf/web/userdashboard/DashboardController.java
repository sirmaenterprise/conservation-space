package com.sirma.cmf.web.userdashboard;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * Backing bean for dashboards initializing functionality.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class DashboardController extends Action implements Serializable {

	private static final long serialVersionUID = -7997479344611610525L;

	/** User dashboard panel controllers. */
	@Inject
	@InstanceType(type = "UserDashboard")
	private Instance<DashboardPanelController> userDashboardPanelControllers;

	/** Case dashboard panel controllers. */
	@Inject
	@InstanceType(type = "CaseDashboard")
	private Instance<DashboardPanelController> caseDashboardPanelControllers;

	/**
	 * Inits the user dashboard.
	 */
	public void initUserDashboard() {
		TimeTracker timer = null;
		if (log.isDebugEnabled()) {
			timer = TimeTracker.createAndStart();
			log.debug("Initializing user dashboard");
		}
		invokeControllersInit(userDashboardPanelControllers);
		if (timer != null) {
			log.debug("User dashboard initialization took " + timer.stopInSeconds() + " s");
		}
	}

	/**
	 * Inits the case dashboard.
	 */
	public void initCaseDashboard() {
		TimeTracker timer = null;
		if (log.isDebugEnabled()) {
			timer = TimeTracker.createAndStart();
			log.debug("Initializing case dashboard");
		}
		invokeControllersInit(caseDashboardPanelControllers);
		if (timer != null) {
			log.debug("Case dashboard initialization took " + timer.stopInSeconds() + " s");
		}
	}

	/**
	 * Invoke controllers init.
	 * 
	 * @param <C>
	 *            the generic type
	 * @param dashboardPanelControllers
	 *            the dashboard panel controllers
	 */
	protected <C extends DashboardPanelController> void invokeControllersInit(
			final Instance<C> dashboardPanelControllers) {
		User currentUser = authenticationService.getCurrentUser();
		SecurityContextManager.callAs(currentUser, currentUser, new Callable<Void>() {
			@Override
			public Void call() {
				for (Iterator<C> iterator = dashboardPanelControllers.iterator(); iterator
						.hasNext();) {
					C panelController = iterator.next();
					panelController.initData();
				}
				return null;
			}
		});
	}
}
