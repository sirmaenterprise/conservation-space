package com.sirma.itt.emf.web.notification;

/**
 * The Class UserMessage.
 * 
 * @author svelikov
 */
public class NotificationMessage {

	/**
	 * The message id will be used as id for an event that will be thrown in case some action is
	 * required in response of this message.
	 */
	private String messageId;

	/** The message is actual string to be displayed inside the notification popup. */
	private String message;

	/** The level is for visual representation only. */
	private MessageLevel level;

	/**
	 * The action button label will be used as label for button in notification popup if
	 * responseRequired is true.
	 */
	private String actionButtonLabel;

	/** Whether this message should be rendered in modal dialog or not. */
	private boolean modal;

	/**
	 * Instantiates a new user message.
	 * 
	 * @param message
	 *            the message
	 * @param level
	 *            the level
	 */
	public NotificationMessage(String message, MessageLevel level) {
		this.message = message;
		this.level = level;
		this.actionButtonLabel = "OK";
		this.modal = true;
	}

	/**
	 * Getter method for messageId.
	 * 
	 * @return the messageId
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Setter method for messageId.
	 * 
	 * @param messageId
	 *            the messageId to set
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * Getter method for message.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Setter method for message.
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Getter method for level.
	 * 
	 * @return the level
	 */
	public MessageLevel getLevel() {
		return level;
	}

	/**
	 * Setter method for level.
	 * 
	 * @param level
	 *            the level to set
	 */
	public void setLevel(MessageLevel level) {
		this.level = level;
	}

	/**
	 * Getter method for actionButtonLabel.
	 * 
	 * @return the actionButtonLabel
	 */
	public String getActionButtonLabel() {
		return actionButtonLabel;
	}

	/**
	 * Setter method for actionButtonLabel.
	 * 
	 * @param actionButtonLabel
	 *            the actionButtonLabel to set
	 */
	public void setActionButtonLabel(String actionButtonLabel) {
		this.actionButtonLabel = actionButtonLabel;
	}

	/**
	 * Getter method for modal.
	 * 
	 * @return the modal
	 */
	public boolean isModal() {
		return modal;
	}

	/**
	 * Setter method for modal.
	 * 
	 * @param modal
	 *            the modal to set
	 */
	public void setModal(boolean modal) {
		this.modal = modal;
	}
}
