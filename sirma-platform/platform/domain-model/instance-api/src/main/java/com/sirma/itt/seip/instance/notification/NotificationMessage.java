package com.sirma.itt.seip.instance.notification;

import java.util.UUID;

/**
 * DTO for user notification message
 *
 * @author svelikov
 */
public class NotificationMessage {

	/**
	 * The message id will be used as id for an event that will be thrown in case some action is required in response of
	 * this message.
	 */
	private String messageId;

	/** The message is actual string to be displayed inside the notification popup. */
	private String message;

	/** The level is for visual representation only. */
	private MessageLevel level;

	/**
	 * The action button label will be used as label for button in notification popup if responseRequired is true.
	 */
	private String actionButtonLabel;

	/** Whether this message should be rendered in modal dialog or not. */
	private boolean modal;

	/** Represents the timeout after the notification will be closed automatically. */
	private int autoCloseTimeout;

	/**
	 * Used to hold the unique identifier for the notification, later will be used to find the correct notification that
	 * should be auto closed.
	 */
	private String notificationIdentifier;

	/** Contains parameter as string for OK button which will be passed to js handler. */
	private String customOk;

	/** Contains parameter as string for Cancel button which will be passed to js handler. */
	private String customCancel;

	/**
	 * Instantiates a new user message. Also sets default value '-1' to autoCloseTimeout.
	 *
	 * @param message
	 *            the message
	 * @param level
	 *            the level
	 */
	public NotificationMessage(String message, MessageLevel level) {
		this.message = message;
		this.level = level;
		actionButtonLabel = "OK";
		modal = true;
		autoCloseTimeout = -1;
		notificationIdentifier = UUID.randomUUID().toString();
	}

	/**
	 * Instantiates a new user message with auto close option.
	 *
	 * @param message
	 *            the message that will be shown
	 * @param level
	 *            the level of the notification
	 * @param autoCloseTimeout
	 *            the timeout before the auto close of the message (positive, in milliseconds)
	 */
	public NotificationMessage(String message, MessageLevel level, int autoCloseTimeout) {
		this.message = message;
		this.level = level;
		actionButtonLabel = "OK";
		modal = true;
		this.autoCloseTimeout = autoCloseTimeout;
		notificationIdentifier = UUID.randomUUID().toString();
	}

	/**
	 * Overloaded constructor used to pass properties that are processed by java script. This properties are used in the
	 * notification pop-up to execute some custom logic.
	 *
	 * @param message
	 *            the message that will be shown
	 * @param level
	 *            the level of the notification
	 * @param autoCloseTimeout
	 *            the timeout before the auto close of the message (positive, in milliseconds), can be set to (-1) to
	 *            stop auto close function
	 * @param customOk
	 *            the name of java script function that have to be executed, when the customOk button is clicked
	 *            <b>(Example: customOkBtn)</b>. This function should exist in <b>EMF.notificationButtons</b>.
	 * @param customCancel
	 *            the name of java script function that have to be executed, when the customCancel button is clicked
	 *            <b>(Example: customCancelBtn)</b>. This function should exist in <b>EMF.notificationButtons</b>.
	 */
	public NotificationMessage(String message, MessageLevel level, int autoCloseTimeout, String customOk,
			String customCancel) {
		this.message = message;
		this.level = level;
		actionButtonLabel = "OK";
		modal = true;
		this.autoCloseTimeout = autoCloseTimeout;
		this.customOk = customOk;
		this.customCancel = customCancel;
		notificationIdentifier = UUID.randomUUID().toString();
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

	/**
	 * Getter method for autoCloseTimeout.
	 *
	 * @return the autoCloseTimeout
	 */
	public long getAutoCloseTimeout() {
		return autoCloseTimeout;
	}

	/**
	 * Setter method for autoCloseTimeout.
	 *
	 * @param autoCloseTimeout
	 *            the autoCloseTimeout to set
	 */
	public void setAutoCloseTimeout(int autoCloseTimeout) {
		this.autoCloseTimeout = autoCloseTimeout;
	}

	/**
	 * @return the customOk
	 */
	public String getCustomOk() {
		return customOk;
	}

	/**
	 * @param customOk
	 *            the customOk to set
	 */
	public void setCustomOk(String customOk) {
		this.customOk = customOk;
	}

	/**
	 * @return the customCancel
	 */
	public String getCustomCancel() {
		return customCancel;
	}

	/**
	 * @param customCancel
	 *            the customCancel to set
	 */
	public void setCustomCancel(String customCancel) {
		this.customCancel = customCancel;
	}

	/**
	 * @return the notificationIdentifier
	 */
	public String getNotificationIdentifier() {
		return notificationIdentifier;
	}

	/**
	 * @param notificationIdentifier
	 *            the notificationIdentifier to set
	 */
	public void setNotificationIdentifier(String notificationIdentifier) {
		this.notificationIdentifier = notificationIdentifier;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotificationMessage [level=").append(level).append(", message=").append(message).append("]");
		return builder.toString();
	}

}
