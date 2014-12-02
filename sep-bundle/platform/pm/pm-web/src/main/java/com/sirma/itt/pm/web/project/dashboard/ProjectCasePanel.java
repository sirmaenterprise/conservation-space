package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;

/**
 * <b>ProjectCasePanel</b> manage functionality for dashlet, located in project dashboard. The
 * content is represented as case records, actions and filters.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectCasePanel extends DashboardPanelActionBase<CaseInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 4701197889327503706L;

	private static final String PROJECTDASHBOARD_DASHLET_CASES = "projectdashboard_dashlet_cases";

	private static final Set<String> dashletActions = new HashSet<String>(
			Arrays.asList(PmActionTypeConstants.CREATE_CASE));

	/** The context instance(project) */
	private ProjectInstance context;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	protected void initializeForAsynchronousInvocation() {
		context = getDocumentContext().getInstance(ProjectInstance.class);
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	@Override
	public Set<String> dashletActionIds() {
		return dashletActions;
	}

	@Override
	public String targetDashletName() {
		// return "project-cases-dashlet";
		return PROJECTDASHBOARD_DASHLET_CASES;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	@Override
	public void updateSearchArguments(SearchArguments<CaseInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
