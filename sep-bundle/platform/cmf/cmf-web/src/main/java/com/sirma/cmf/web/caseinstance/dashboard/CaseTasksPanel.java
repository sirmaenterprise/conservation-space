package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * <b>CaseTasksPanel</b> manage functionality for dashlet, located in case dashboard. The content is
 * represented as task records, actions and filters.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseTasksPanel extends DashboardPanelActionBase<AbstractTaskInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 1553948189077034150L;

	private static final String CASEDASHBOARD_DASHLET_TASKS = "casedashboard_dashlet_tasks";

	/** The dashlet action located in the toolbar. */
	private final Set<String> dashletActions = new HashSet<String>(
			Arrays.asList(ActionTypeConstants.CREATE_TASK));

	@Inject
	private AuthorityService authorityService;

	/** The case instance, represent current context. */
	private CaseInstance context;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	protected void initializeForAsynchronousInvocation() {
		context = getDocumentContext().getInstance(CaseInstance.class);
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Verify button visibility based on instance and operation.
	 * 
	 * @param instance
	 *            current instance
	 * @param operation
	 *            current operation
	 * @return true - allowed/false - not allowed
	 */
	public boolean isAllowedButton(CaseInstance instance, String operation) {
		return authorityService.isActionAllowed(instance, operation, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> dashletActionIds() {
		return dashletActions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String targetDashletName() {
		// return "case-tasks-dashlet";
		return CASEDASHBOARD_DASHLET_TASKS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
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
