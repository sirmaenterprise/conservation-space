package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.cmf.web.userdashboard.panel.WorkflowUtil;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * This class manage project workflows panel. The workflows will be retrieved from the semantic
 * repository. Here we can define filters and actions for the panel.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectWorkflowsPanel extends DashboardPanelActionBase<Instance> implements
		Serializable, DashboardPanelController {

	/** The constant serial version UID. */
	private static final long serialVersionUID = 1L;

	private static final String PROJECTDASHBOARD_DASHLET_WORKFLOWS = "projectdashboard_dashlet_workflows";

	/** The workflow util. */
	@Inject
	private WorkflowUtil workflowUtil;

	private ProjectInstance context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeForAsynchronousInvocation() {
		context = getDocumentContext().getInstance(ProjectInstance.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initData() {
		onOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	@Override
	public Set<String> dashletActionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_WORKFLOWS;
	}

	@Override
	public Instance dashletActionsTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<Instance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
