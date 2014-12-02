package com.sirma.cmf.web.workflow.transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.Pair;

/**
 * The Class OperationsBuilder.
 *
 * @author svelikov
 */
public class TransitionsBuilder implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2720502546387937369L;

	/**
	 * Builds a list with task transition action objects that will be used in the facelets template
	 * to create a list with action buttons.
	 *
	 * @param transitionDefinitions
	 *            the transition definitions
	 * @return the list
	 */
	public List<TaskTransitionAction> build(List<TransitionDefinition> transitionDefinitions) {

		List<TaskTransitionAction> taskTransitionActions = new ArrayList<TaskTransitionAction>();

		for (TransitionDefinition transitionDefinition : transitionDefinitions) {

			String transitionId = transitionDefinition.getIdentifier();

			// for cancel operation we should require the user to provide cancel
			// reason
			boolean isCancelOperation = false;
			if ("cancel".equals(transitionId)) {
				isCancelOperation = true;
			}
			Pair<String,ControlDefinition> picklistControl = null;
			for (PropertyDefinition nextField : transitionDefinition.getFields()) {
				ControlDefinition controlDefinition = nextField.getControlDefinition();
				if (controlDefinition != null
						&& "PICKLIST".equals(controlDefinition.getIdentifier())) {
					picklistControl =new Pair<String, ControlDefinition>(nextField.getIdentifier(),  controlDefinition);
					break;
				}
			}
			taskTransitionActions.add(new TaskTransitionAction(
					transitionDefinition.getIdentifier(), transitionDefinition.getLabel(),
					transitionDefinition.getTooltip(), isCancelOperation, picklistControl));

		}

		return taskTransitionActions;
	}
}
