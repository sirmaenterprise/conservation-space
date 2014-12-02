package com.sirma.itt.emf.web.action.event;

import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.web.event.AbstractWebEvent;

/**
 * The event is fired when executing an immediate action event started from the automatically
 * generated operations through the user interface. If the event is handled and nothing more is
 * needed to be performed then the property handled provided via {@link HandledEvent} should be set
 * to <code>true</code>. Otherwise the event will trigger the default functionality to save the
 * payload instance. If the method {@link #isHandled()} returns <code>true</code> then the
 * operations completes.
 * 
 * @author BBonev
 */
@Documentation("The event is fired when executing an immediate action event started from the automatically "
		+ "generated operations through the user interface. If the event is handled and nothing more is needed"
		+ " to be performed then the property handled provided via {@link HandledEvent} should be set to <code>true</code>."
		+ " Otherwise the event will trigger the default functionality to save the payload instance. If the method {@link #isHandled()} "
		+ "returns <code>true</code> then the operations completes.")
public class EmfImmediateActionEvent extends AbstractWebEvent<Instance> implements HandledEvent {

	/** The action id. */
	private String actionId;
	/** The handled. */
	private boolean handled;

	/**
	 * Instantiates a new EMF immediate action event.
	 * 
	 * @param instance
	 *            the instance
	 * @param navigation
	 *            the navigation
	 * @param actionId
	 *            the action id
	 */
	public EmfImmediateActionEvent(Instance instance, String navigation, String actionId) {
		super(instance, navigation);
		this.setActionId(actionId);
	}

	/**
	 * Getter method for actionId.
	 * 
	 * @return the actionId
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Setter method for actionId.
	 * 
	 * @param actionId
	 *            the actionId to set
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	@Override
	public boolean isHandled() {
		return handled;
	}

	@Override
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

}
