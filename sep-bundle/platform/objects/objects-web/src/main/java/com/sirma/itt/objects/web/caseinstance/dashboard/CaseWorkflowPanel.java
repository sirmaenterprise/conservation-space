package com.sirma.itt.objects.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * This class manage case workflows panel. The workflows will be retrieved from the semantic
 * repository. Here we can define filters and actions for the panel.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseWorkflowPanel extends DashboardPanelActionBase<Instance> implements Serializable,
		DashboardPanelController {

	private static final long serialVersionUID = -4843233057114961324L;

	private static final String CASEDASHBOARD_DASHLET_WORKFLOWS = "casedashboard_dashlet_workflows";

	/** The panel filter actions located in the toolbar. */
	private final Set<String> dashletActions = new HashSet<String>(
			Arrays.asList(ActionTypeConstants.START_WORKFLOW));

	/** The current context instance(case instance). */
	private CaseInstance context;

	@Override
	protected void initializeForAsynchronousInvocation() {
		context = getDocumentContext().getInstance(CaseInstance.class);
	}

	@Override
	public void initData() {
		onOpen();
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
		// return "case-workflow-dashlet";
		return CASEDASHBOARD_DASHLET_WORKFLOWS;
	}

	@Override
	public Instance dashletActionsTarget() {
		if (context == null) {
			context = getDocumentContext().getInstance(CaseInstance.class);
		}
		return context;
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
