/*
 *
 */
package com.sirma.cmf.web.standaloneTask;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The Class StandaloneTaskTransitionExecutor.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class StandaloneTaskTransitionExecutor extends Action implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3520657387420410323L;

	@Inject
	private TaskService taskService;

	@Inject
	private StandaloneTaskService standaloneTaskService;

	/**
	 * Execute transition.
	 * 
	 * @param transitionId
	 *            the transition id
	 * @return Navigation string
	 */
	public String executeTransition(String transitionId) {

		log.debug("CMFWeb: StandaloneTaskTransitionExecutor.executeTransition with transitionId ["
				+ transitionId + "]");

		String navigationString = NavigationConstants.RELOAD_PAGE;

		StandaloneTaskInstance taskInstance = getDocumentContext().getInstance(
				StandaloneTaskInstance.class);

		if ("cancelstart".equals(transitionId.toLowerCase())
				|| "cancel".equals(transitionId.toLowerCase())) {
			taskService.cancel(taskInstance);
			navigationString = NavigationConstants.RELOAD_PAGE;

		} else {

			standaloneTaskService.complete(taskInstance, new Operation(transitionId));
			List<AbstractTaskInstance> subTasks = taskService.getSubTasks(taskInstance,
					TaskState.IN_PROGRESS, false);
			for (AbstractTaskInstance abstractTaskInstance : subTasks) {
				standaloneTaskService.complete((StandaloneTaskInstance) abstractTaskInstance,
						new Operation(transitionId));
			}
			//
			navigationString = NavigationConstants.RELOAD_PAGE;

		}

		return navigationString;
	}

}
