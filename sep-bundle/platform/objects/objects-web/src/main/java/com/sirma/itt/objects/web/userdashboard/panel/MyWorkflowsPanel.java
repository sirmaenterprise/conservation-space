package com.sirma.itt.objects.web.userdashboard.panel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * This class manage project workflows dashlet. The workflows will be retrieved from the semantic
 * repository. Here we can define filters and actions for the panel.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyWorkflowsPanel extends DashboardPanelActionBase<Instance> implements Serializable,
		DashboardPanelController {

	/** The serial version identifier. */
	private static final long serialVersionUID = 1L;

	private static final String USERDASHBOARD_DASHLET_WORKFLOWS = "userdashboard_dashlet_workflows";

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> dashletActionIds() {
		// Auto-generated method stub
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_WORKFLOWS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance dashletActionsTarget() {
		// Auto-generated method stub
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<Instance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// auto-generated method stub

	}

}
