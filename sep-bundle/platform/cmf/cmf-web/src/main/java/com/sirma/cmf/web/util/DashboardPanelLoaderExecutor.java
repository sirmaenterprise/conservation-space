package com.sirma.cmf.web.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.scheduler.SchedulerActionAdapter;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Scheduler action to load a dashboard element.
 * 
 * @author BBonev
 */
@Named(DashboardPanelLoaderExecutor.NAME)
public class DashboardPanelLoaderExecutor extends SchedulerActionAdapter implements Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4015747317453202852L;
	public static final String NAME = "dashboardPanelLoader";
	/** The Constant DASHBOARD. */
	public static final String DASHBOARD = "dashboard";

	/** The Constant arguments. */
	private static final List<Pair<String, Class<?>>> arguments = new ArrayList<Pair<String, Class<?>>>(
			Arrays.asList(new Pair<String, Class<?>>(DASHBOARD, DashboardPanelActionBase.class)));

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(SchedulerContext context) throws Exception {
		DashboardPanelActionBase<Instance> dashboardPanel = (DashboardPanelActionBase<Instance>) context
				.get(DASHBOARD);
		dashboardPanel.executeDefaultFilter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return arguments;
	}
}
