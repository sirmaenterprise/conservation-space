package com.sirma.cmf.web.workflow.task;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.picklist.ItemsConverter;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;

/**
 * The class is backend handler for operation regarding transition execution and user selection on
 * transition start.
 * 
 * @author bbanchev
 */
@Named
@ViewAccessScoped
public class TaskAssigneesActions extends EntityAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1600465033376699669L;

	/** The assignee id used from reassign operation. */
	private String newAssignees;

	@Inject
	private WorkflowTransitionExecutor transitionExecutor;
	/** the mode for the picker - multy, single, etc. */
	private String mode;

	/**
	 * Sets the assignees in the provided property and executes the transition.
	 * 
	 * @param task
	 *            current abstract task
	 * @param property
	 *            - the proerty to set selected users in ( {@link #newAssignees} )
	 * @param transitionId
	 *            the transition to execute at the end.
	 * @return navigation string returned from
	 *         {@link WorkflowTransitionExecutor#executeTransition(String)}
	 */
	public String setAssigneesAndNavigate(AbstractTaskInstance task, String property,
			String transitionId) {

		String navigation = NavigationConstants.RELOAD_PAGE;

		if ((newAssignees != null) && !newAssignees.toString().isEmpty()) {
			// multi should be split by ItemsConverter.DELIMITER
			Serializable convertObjectToItems = ItemsConverter.convertObjectToItems(newAssignees,
					"multy".equals(mode));
			// update the field
			task.getProperties().put(property, convertObjectToItems);
			if (task instanceof TaskInstance) {
				// #{transitionExecutor.executeTransition(transitionId)}
				return transitionExecutor.executeTransition(transitionId);
			}

		}
		return navigation;
	}

	/**
	 * Initialize the picker selector with the users, groups depending on the itemType
	 * 
	 * @param itemType
	 *            is the type to init picker with
	 * @param mode
	 *            is the selection mode
	 * @param filterName
	 *            is the filter to execute
	 */
	public void initialize(String itemType, String mode, String filterName) {
		this.mode = mode;
	}

	/**
	 * Getter method for newAssignees.
	 * 
	 * @return the newAssignees
	 */
	public String getNewAssignees() {
		return newAssignees;
	}

	/**
	 * Setter method for newAssignees.
	 * 
	 * @param newAssignees
	 *            the newAssignees to set
	 */
	public void setNewAssignees(String newAssignees) {
		this.newAssignees = newAssignees;
	}

}
