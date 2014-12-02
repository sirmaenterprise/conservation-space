package com.sirma.itt.pm.web.userdashboard.panel;

import java.io.Serializable;
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
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * <b>MyProjectsPanel</b> manage functionality for dashlet, located in personal/user dashboard. The
 * content is represented as project records, actions and filters.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyProjectsPanel extends DashboardPanelActionBase<ProjectInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 7487956194152421786L;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Search projects.
	 * 
	 * @return the string
	 */
	public String searchProjects() {
		return executeToolbarAction(USERDASHBOARD_DASHLET_PROJECTS,
				PmNavigationConstants.PROJECT_SEARCH);
	}

	@Override
	public Set<String> dashletActionIds() {
		return null;
	}

	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_PROJECTS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<ProjectInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// nothing to do

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// nothing to do

	}

}
