package com.sirma.cmf.web.form.action.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.web.notification.NotificationMessage;

/**
 * DynamicFormActionEvent fired from action event button placed in generated forms. If the event is
 * handled successfully then the property {@link DynamicFormActionEvent#setHandled(boolean)} should
 * be set to <code>true</code>.
 * 
 * @author svelikov
 */
@Documentation("The Class DynamicFormActionEvent fired from action event button placed in generated forms. If the event is handled successfully then the property {@link DynamicFormActionEvent#setHandled(boolean)} should be set to <code>true</code>.")
public class DynamicFormActionEvent extends AbstractInstanceEvent<Instance> {

	/** The navigation. */
	private String navigation;

	/** The handled. */
	private boolean handled = false;

	/** The arguments. */
	private Instance arguments;

	/**
	 * The notification message to be displayed to the user after request triggered this action is
	 * completed.
	 */
	private NotificationMessage notificationMessage;

	/**
	 * Instantiates a new dynamic form action event.
	 * 
	 * @param navigation
	 *            the navigation
	 * @param target
	 *            the target instance
	 * @param arguments
	 *            the button arguments for the given event
	 */
	public DynamicFormActionEvent(String navigation, Instance target, Instance arguments) {
		super(target);
		this.navigation = navigation;
		this.arguments = arguments;
	}

	/**
	 * Getter method for navigation.
	 * 
	 * @return the navigation
	 */
	public String getNavigation() {
		return navigation;
	}

	/**
	 * Setter method for navigation.
	 * 
	 * @param navigation
	 *            the navigation to set
	 */
	public void setNavigation(String navigation) {
		this.navigation = navigation;
	}

	/**
	 * Getter method for handled.
	 * 
	 * @return the handled
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * Setter method for handled.
	 * 
	 * @param handled
	 *            the handled to set
	 */
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	/**
	 * Getter method for arguments.
	 * 
	 * @return the arguments
	 */
	public Instance getArguments() {
		return arguments;
	}

	/**
	 * Setter method for arguments.
	 * 
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(Instance arguments) {
		this.arguments = arguments;
	}

	/**
	 * Getter method for notificationMessage.
	 * 
	 * @return the notificationMessage
	 */
	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

	/**
	 * Setter method for notificationMessage.
	 * 
	 * @param notificationMessage
	 *            the notificationMessage to set
	 */
	public void setNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
