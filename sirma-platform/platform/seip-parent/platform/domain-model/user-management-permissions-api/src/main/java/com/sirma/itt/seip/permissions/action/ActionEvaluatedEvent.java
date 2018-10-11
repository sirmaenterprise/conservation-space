package com.sirma.itt.seip.permissions.action;

import java.util.Set;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event object fired after evaluating the instance actions before sending them to the caller.
 *
 * @author BBonev
 */
@Documentation("Event object fired after evaluating the instance actions before sending them to the caller.")
public class ActionEvaluatedEvent extends AbstractInstanceEvent<Instance> {

	/** The actions. */
	private Set<Action> actions;

	/** The placeholder html element from where the actions are requested. */
	private String placeholder;

	/**
	 * Instantiates a new evaluated actions.
	 *
	 * @param instance
	 *            the instance
	 * @param actions
	 *            the actions
	 * @param placeholder
	 *            the placeholder html element from where the actions are requested
	 */
	public ActionEvaluatedEvent(Instance instance, Set<Action> actions, String placeholder) {
		super(instance);
		this.actions = actions;
		this.placeholder = placeholder;
	}

	/**
	 * Getter method for actions.
	 *
	 * @return the actions
	 */
	public Set<Action> getActions() {
		return actions;
	}

	/**
	 * Setter method for actions.
	 *
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	/**
	 * Getter method for placeholder.
	 *
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * Setter method for placeholder.
	 *
	 * @param placeholder
	 *            the placeholder to set
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

}
