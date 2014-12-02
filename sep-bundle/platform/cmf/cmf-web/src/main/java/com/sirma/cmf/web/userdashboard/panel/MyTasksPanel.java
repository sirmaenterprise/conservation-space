/*
 *
 */
package com.sirma.cmf.web.userdashboard.panel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.standaloneTask.StandaloneTaskAction;
import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.cmf.web.workflow.task.TaskListTableAction;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * <b>MyTasksPanel</b> manage functionality for dashlet, located in personal/user dashboard. The
 * content is represented as task records, actions and filters.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyTasksPanel extends DashboardPanelActionBase<AbstractTaskInstance> implements
		Serializable, DashboardPanelController {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5181022575609543468L;

	private static final String USERDASHBOARD_DASHLET_TASKS = "userdashboard_dashlet_tasks";

	/** The task list table action. */
	@Inject
	private TaskListTableAction taskListTableAction;

	/** The standalnoe task action */
	@Inject
	private StandaloneTaskAction standaloneTaskAction;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Open selected task.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the string
	 */
	public String openTask(AbstractTaskInstance taskInstance) {
		if (taskInstance instanceof TaskInstance) {
			TaskInstance instance = (TaskInstance) taskInstance;
			Instance owningInstance = instance.getOwningInstance();
			if (owningInstance instanceof CaseInstance) {
				getDocumentContext().addInstance(owningInstance);
			}
			return taskListTableAction.open(instance);
		}

		StandaloneTaskInstance instance = (StandaloneTaskInstance) taskInstance;
		Instance owningInstance = TypeConverterUtil.getConverter().convert(Instance.class,
				instance.getOwningReference());
		if (owningInstance instanceof CaseInstance) {
			getDocumentContext().addInstance(owningInstance);
		} else if (owningInstance instanceof WorkflowInstanceContext) {
			getDocumentContext().addInstance(owningInstance);
		}
		return standaloneTaskAction.open(instance);
	}

	@Override
	public Set<String> dashletActionIds() {
		return Collections.emptySet();

	}

	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_TASKS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<AbstractTaskInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// auto-generated method stub

	}

}
