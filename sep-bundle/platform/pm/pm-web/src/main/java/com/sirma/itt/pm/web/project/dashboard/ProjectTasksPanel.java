package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;

/**
 * <b>ProjectTasksPanel</b> manage functionality for dashlet, located in project dashboard. The
 * content is represented as task records, actions and filters.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectTasksPanel extends DashboardPanelActionBase<AbstractTaskInstance> implements
		Serializable, DashboardPanelController {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2264175323860334L;

	private static final String PROJECTDASHBOARD_DASHLET_TASKS = "projectdashboard_dashlet_tasks";

	private static final Set<String> DASHLET_ACTIONS = new HashSet<String>(Arrays.asList(
			PmActionTypeConstants.CREATE_TASK, PmActionTypeConstants.START_WORKFLOW));

	private ProjectInstance projectInstance;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	protected void initializeForAsynchronousInvocation() {
		projectInstance = getDocumentContext().getInstance(ProjectInstance.class);
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_TASKS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	@Override
	public Set<String> dashletActionIds() {
		return DASHLET_ACTIONS;
	}

	@Override
	public void updateSearchArguments(SearchArguments<AbstractTaskInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
